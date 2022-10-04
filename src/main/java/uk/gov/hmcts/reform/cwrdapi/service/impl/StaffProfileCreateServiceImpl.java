package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.FeignException;
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
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.NewUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerSkillRepository;
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
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_ALREADY_CREATED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_NOT_PRESENT_IN_DB;
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
    CaseWorkerSkillRepository caseWorkerSkillRepository;

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

        log.info("{}:: processStaffProfileCreation starts::",
                loggingComponentName);
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

    @Override
    @SuppressWarnings("unchecked")
    public StaffProfileCreationResponse updateStaffProfile(StaffProfileCreationRequest profileRequest) {

        log.info("{}:: processStaffProfileCreation starts::",
                loggingComponentName);
        final CaseWorkerProfile newCaseWorkerProfiles;
        final CaseWorkerProfile processedCwProfiles;
        StaffProfileCreationResponse response = null;

        validateStaffProfile.validateStaffProfile(profileRequest);

        checkStaffProfileForUpdate(profileRequest);
        //TODO processExistingCaseWorkers
        newCaseWorkerProfiles = updateCaseWorkerProfile(profileRequest);

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

    private void checkStaffProfileForUpdate(StaffProfileCreationRequest profileRequest) {

        // get all existing profile from db (used IN clause)
        CaseWorkerProfile caseWorkerProfile = caseWorkerProfileRepo.findByEmailId(profileRequest.getEmailId());
        // TODO to update if profile is null throw exception
        if (caseWorkerProfile == null) {
            validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE,PROFILE_NOT_PRESENT_IN_DB,
                    null,profileRequest);
            throw new ResourceNotFoundException(PROFILE_NOT_PRESENT_IN_DB);
        }
    }

    public CaseWorkerProfile createCaseWorkerProfile(StaffProfileCreationRequest profileRequest) {
        CaseWorkerProfile caseWorkerProfile = null;
        //User Profile Call
        log.info("{}:: createCaseWorkerProfile UserProfile call starts::",
                loggingComponentName);
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(profileRequest);
        log.info("{}:: createCaseWorkerProfile UserProfile Received  response status {}::",
                loggingComponentName,responseEntity.getStatusCode());
        if (nonNull(responseEntity) && (responseEntity.getStatusCode().is2xxSuccessful())) {
            log.info("{}:: createCaseWorkerProfile UserProfile Received  successful response {}::",
                    loggingComponentName,responseEntity.getStatusCode());
            UserProfileCreationResponse upResponse = (UserProfileCreationResponse) (responseEntity.getBody());
            if (nonNull(upResponse)) {
                caseWorkerProfile = new CaseWorkerProfile();
                staffProfileCreateUpdateUtil.populateStaffProfile(profileRequest, caseWorkerProfile,
                        upResponse.getIdamId());
            }
        }

        log.info("{}:: createCaseWorkerProfile UserProfile ends here { }::",
                loggingComponentName,caseWorkerProfile.getCaseWorkerId());
        return caseWorkerProfile;
    }

    public CaseWorkerProfile updateCaseWorkerProfile(StaffProfileCreationRequest profileRequest) {
        CaseWorkerProfile caseWorkerProfile = null;
        //User Profile Call
        log.info("{}:: createCaseWorkerProfile UserProfile call starts::",
                loggingComponentName);
        //TODO Need to discuss and get userDetails from Idam and update data in db
        UserProfileCreationResponse upResponse = getUserProfileFromIdam(profileRequest);
        log.info("{}:: updateCaseWorkerProfile UserProfile ::",
                upResponse);

        //TODO Check with Nayeem
//        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(profileRequest);
//        log.info("{}:: createCaseWorkerProfile UserProfile Received  response status {}::",
//                loggingComponentName,responseEntity.getStatusCode());
//        if (nonNull(responseEntity) && (responseEntity.getStatusCode().is2xxSuccessful())) {
//            log.info("{}:: createCaseWorkerProfile UserProfile Received  successful response {}::",
//                    loggingComponentName,responseEntity.getStatusCode());
//            UserProfileCreationResponse upResponse = (UserProfileCreationResponse) (responseEntity.getBody());
//            if (nonNull(upResponse)) {
//                caseWorkerProfile = new CaseWorkerProfile();
//                staffProfileCreateUpdateUtil.populateStaffProfile(profileRequest, caseWorkerProfile,
//                        upResponse.getIdamId());
//            }
//        }

        if (nonNull(upResponse)) {
            caseWorkerProfile = new CaseWorkerProfile();
            staffProfileCreateUpdateUtil.populateStaffProfile(profileRequest, caseWorkerProfile,
                    upResponse.getIdamId());
        }
        log.info("{}:: createCaseWorkerProfile UserProfile ends here { }::",
                loggingComponentName,caseWorkerProfile.getCaseWorkerId());
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


    public  UserProfileCreationResponse getUserProfileFromIdam(StaffProfileCreationRequest staffProfileRequest) {

        NewUserResponse newUserResponse;
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        Response response = null;
        try  {
            String emailAddress = staffProfileRequest.getEmailId();
            response = userProfileFeignClient.getUserProfileByEmail(emailAddress);
            Object clazz = response.status() > 300 ? ErrorResponse.class : NewUserResponse.class;
            ResponseEntity<Object> responseResponseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);

            if (response.status() == 200) {

                newUserResponse = (NewUserResponse) requireNonNull(responseResponseEntity.getBody());
                userProfileCreationResponse.setIdamId(newUserResponse.getUserIdentifier());
                userProfileCreationResponse.setIdamStatus(newUserResponse.getIdamStatus());

            } else {
                ErrorResponse errorResponse = (ErrorResponse) responseResponseEntity.getBody();
                log.error("{}:: Response from UserProfileByEmail service call {}",
                        loggingComponentName, errorResponse.getErrorDescription());
                //newUserResponse = new NewUserResponse();
            }

        } catch (Exception ex) {
            validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE,ex.getMessage(),
                    null,staffProfileRequest);
            throw ex;
        } finally {
            if (nonNull(response)) {
                response.close();
            }
        }

        return userProfileCreationResponse;

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