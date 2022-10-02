package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.Response;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.ICwrdCommonRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.service.StaffProfileService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil;
import uk.gov.hmcts.reform.cwrdapi.util.StaffProfileCreateUpdateUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_ALREADY_CREATED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_CWD_USER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SRD;



@Service
@Slf4j
@Setter
public class StaffProfileCreateServiceImpl implements StaffProfileService {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Value("${crd.publisher.caseWorkerDataPerMessage}")
    private int caseWorkerDataPerMessage;

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepo;

    @Autowired
    RoleTypeRepository roleTypeRepository;

    @Autowired
    UserTypeRepository userTypeRepository;

    @Autowired
    CaseWorkerLocationRepository caseWorkerLocationRepository;

    @Autowired
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @Autowired
    CaseWorkerRoleRepository caseWorkerRoleRepository;

    @Autowired
    ICwrdCommonRepository cwrCommonRepository;

    @Autowired
    IdamRoleMappingService idamRoleMappingService;

    @Autowired
    private UserProfileFeignClient userProfileFeignClient;

    @Autowired
    private TopicPublisher topicPublisher;

    @Autowired
    IValidationService validationServiceFacade;

    List<ExceptionCaseWorker> upExceptionCaseWorkers;

    @Autowired
    SkillRepository skillRepository;

    @Autowired
    IJsrValidatorInitializer validateStaffProfile;

    @Autowired
    StaffProfileCreateUpdateUtil staffProfileCreateUpdateUtil;

    @Override
    @SuppressWarnings("unchecked")
    public StaffProfileCreationResponse processStaffProfileCreation(StaffProfileCreationRequest profileRequest) {

        final CaseWorkerProfile newCaseWorkerProfiles;
        final CaseWorkerProfile processedCwProfiles;
        StaffProfileCreationResponse response = null;

        validateStaffProfile.validateStaffProfile(profileRequest);

        checkStaffProfileEmail(profileRequest);
        newCaseWorkerProfiles = createCaseWorkerProfile(profileRequest);

        processedCwProfiles = staffProfileCreateUpdateUtil.persistStaffProfile(newCaseWorkerProfiles);

        if (null != processedCwProfiles) {

            validationServiceFacade.saveStaffAudit(AuditStatus.SUCCESS,null,
                    processedCwProfiles.getCaseWorkerId(),profileRequest);
            response = StaffProfileCreationResponse.builder()
                    .caseWorkerId(processedCwProfiles.getCaseWorkerId())
                    .build();
        }
        return response;
    }



    private void checkStaffProfileEmail(StaffProfileCreationRequest profileRequest) {

        // get all existing profile from db (used IN clause)
        CaseWorkerProfile caseWorkerProfile = caseWorkerProfileRepo.findByEmailId(profileRequest.getEmailId());

        if (caseWorkerProfile != null) {
            validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE,PROFILE_ALREADY_CREATED,
                    null,profileRequest);
            throw new InvalidRequestException(PROFILE_ALREADY_CREATED);
        }
    }

    public CaseWorkerProfile createCaseWorkerProfile(StaffProfileCreationRequest profileRequest) {
        CaseWorkerProfile caseWorkerProfile = null;
        //User Profile Call
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(profileRequest);
        if (nonNull(responseEntity) && (responseEntity.getStatusCode().is2xxSuccessful())) {

            UserProfileCreationResponse upResponse = (UserProfileCreationResponse) (responseEntity.getBody());
            if (nonNull(upResponse)) {
                caseWorkerProfile = new CaseWorkerProfile();
                staffProfileCreateUpdateUtil.populateStaffProfile(profileRequest, caseWorkerProfile,
                        upResponse.getIdamId());
            }
        }
        return caseWorkerProfile;
    }

    public ResponseEntity<Object> createUserProfileInIdamUP(StaffProfileCreationRequest staffProfileRequest) {

        ResponseEntity<Object> responseEntity;
        Response response = null;
        Object clazz;
        try {
            response = userProfileFeignClient.createUserProfile(createUserProfileRequest(staffProfileRequest), SRD);

            clazz = (response.status() == 201 || response.status() == 409)
                    ? UserProfileCreationResponse.class : ErrorResponse.class;

            responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);

            return responseEntity;
        } catch (Exception ex) {
            validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE,ex.getMessage(),
                    null,staffProfileRequest);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            if (nonNull(response)) {
                response.close();
            }
        }
    }

    // creating user profile request
    public UserProfileCreationRequest createUserProfileRequest(StaffProfileCreationRequest profileRequest) {

        Set<String> userRoles = new HashSet<>();
        userRoles.add(ROLE_CWD_USER);

        Set<String> idamRoles = staffProfileCreateUpdateUtil.getUserRolesByRoleId(profileRequest);
        if (isNotEmpty(idamRoles)) {
            userRoles.addAll(idamRoles);
        }
        //Creating user profile request
        return new UserProfileCreationRequest(
                profileRequest.getEmailId(),
                profileRequest.getFirstName(),
                profileRequest.getLastName(),
                LanguagePreference.EN,
                UserCategory.CASEWORKER,
                UserTypeRequest.INTERNAL,
                userRoles,
                false);
    }



    @Override
    public void publishStaffProfileToTopic(StaffProfileCreationResponse staffProfileCreationResponse) {

        topicPublisher.sendMessage(List.of(staffProfileCreationResponse.getCaseWorkerId()));
    }

}