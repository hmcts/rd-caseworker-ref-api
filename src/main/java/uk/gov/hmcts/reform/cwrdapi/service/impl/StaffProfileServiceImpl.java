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
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.LocationReferenceDataFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerStaticValueRepositoryAccessor;
import uk.gov.hmcts.reform.cwrdapi.service.ICwrdCommonRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.service.StaffProfileService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.springframework.http.HttpStatus.CONFLICT;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_ALREADY_CREATED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_CWD_USER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SRD;



@Service
@Slf4j
@Setter
public class StaffProfileServiceImpl implements StaffProfileService {

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
    CaseWorkerIdamRoleAssociationRepository roleAssocRepository;

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
    private LocationReferenceDataFeignClient locationReferenceDataFeignClient;

    @Autowired
    private TopicPublisher topicPublisher;

    @Autowired
    IValidationService validationServiceFacade;

    @Autowired
    CaseWorkerStaticValueRepositoryAccessor caseWorkerStaticValueRepositoryAccessor;

    List<ExceptionCaseWorker> upExceptionCaseWorkers;

    @Autowired
    SkillRepository skillRepository;

    @Autowired
    IJsrValidatorInitializer validateStaffProfile;


    @SuppressWarnings("unchecked")
    @Override
    public StaffProfileCreationResponse processStaffProfileCreation(StaffProfileCreationRequest profileRequest) {

        final CaseWorkerProfile newCaseWorkerProfiles;
        final CaseWorkerProfile processedCwProfiles;
        StaffProfileCreationResponse response = null;

        validateStaffProfile.validateStaffProfile(profileRequest);

        checkStaffProfileEmail(profileRequest);
        newCaseWorkerProfiles = createCaseWorkerProfile(profileRequest);

        processedCwProfiles = persistCaseWorker(newCaseWorkerProfiles);

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
        if (nonNull(responseEntity) && (responseEntity.getStatusCode().is2xxSuccessful()
                || responseEntity.getStatusCode() == CONFLICT) && nonNull(responseEntity.getBody())) {

            UserProfileCreationResponse upResponse = (UserProfileCreationResponse) (responseEntity.getBody());
            if (nonNull(upResponse)) {
                caseWorkerProfile = new CaseWorkerProfile();
                populateCaseWorkerProfile(profileRequest, caseWorkerProfile, upResponse.getIdamId());
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

    private CaseWorkerProfile persistCaseWorker(CaseWorkerProfile caseWorkerProfile) {
        CaseWorkerProfile processedCwProfiles = null;

        if (null != caseWorkerProfile) {
            caseWorkerProfile.setNew(true);
            processedCwProfiles = caseWorkerProfileRepo.save(caseWorkerProfile);
            log.info("{}:: {} case worker profiles inserted ::", loggingComponentName,
                    processedCwProfiles, validationServiceFacade.getAuditJobId());
        }

        return processedCwProfiles;
    }

    // creating user profile request
    public UserProfileCreationRequest createUserProfileRequest(StaffProfileCreationRequest profileRequest) {

        Set<String> userRoles = new HashSet<>();
        userRoles.add(ROLE_CWD_USER);

        Set<String> idamRoles = getUserRolesByRoleId(profileRequest);
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

    // get the roles that needs to send to idam based on the roleType in the request.
    Set<String> getUserRolesByRoleId(StaffProfileCreationRequest staffProfileRequest) {

        // get Roles Types
        List<RoleType> roleTypeList = new ArrayList<>();
        staffProfileRequest.getRoles().forEach(role -> roleTypeList.addAll(
                caseWorkerStaticValueRepositoryAccessor
                        .getRoleTypes()
                        .stream()
                        .filter(roleType -> role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                        .toList())
        );

        // get work area codes
        List<String> serviceCodes = staffProfileRequest.getServices()
                .stream()
                .map(CaseWorkerServicesRequest::getServiceCode)
                .toList();


        // get all assoc records matching role id and service code, finally return idam roles associated
        Set<String> matchedRoles = roleAssocRepository.findByRoleTypeInAndServiceCodeIn(roleTypeList, serviceCodes)
                .stream()
                .map(CaseWorkerIdamRoleAssociation::getIdamRole)
                .collect(Collectors.toSet());
        log.info("{}:: roles matched from assoc :: {}", loggingComponentName, matchedRoles);
        return matchedRoles;
    }

    public void populateCaseWorkerProfile(StaffProfileCreationRequest staffProfileRequest,
                                          CaseWorkerProfile caseWorkerProfile, String idamId) {
        //case worker profile request mapping
        mapCaseWorkerProfileRequest(idamId, staffProfileRequest, caseWorkerProfile);
        //Locations data request mapping and setting to case worker profile
        caseWorkerProfile.getCaseWorkerLocations().addAll(mapCaseWorkerLocationRequest(idamId, staffProfileRequest));
        //caseWorkerRoles roles request mapping and data setting to case worker profile
        caseWorkerProfile.getCaseWorkerRoles().addAll(mapCaseWorkerRoleRequestMapping(idamId, staffProfileRequest));
        //caseWorkerWorkAreas setting to case worker profile
        caseWorkerProfile.getCaseWorkerWorkAreas().addAll(mapCwAreaOfWork(staffProfileRequest, idamId));

        caseWorkerProfile.getCaseWorkerSkills().addAll(mapCaseWorkerSkillRequestMapping(idamId, staffProfileRequest));
    }


    public List<CaseWorkerWorkArea> mapCwAreaOfWork(StaffProfileCreationRequest profileRequest,
                                                    String idamId) {
        List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();
        profileRequest.getServices().forEach(caseWorkerWorkAreaRequest -> {
            CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea(idamId,
                    caseWorkerWorkAreaRequest.getService(), caseWorkerWorkAreaRequest.getServiceCode());
            caseWorkerWorkAreas.add(caseWorkerWorkArea);
        });
        return caseWorkerWorkAreas;
    }

    public CaseWorkerProfile mapCaseWorkerProfileRequest(String idamId,
                                                         StaffProfileCreationRequest staffProfileRequest,
                                                         CaseWorkerProfile caseWorkerProfile) {
        caseWorkerProfile.setCaseWorkerId(idamId);
        caseWorkerProfile.setFirstName(staffProfileRequest.getFirstName());
        caseWorkerProfile.setLastName(staffProfileRequest.getLastName());
        caseWorkerProfile.setEmailId(staffProfileRequest.getEmailId().toLowerCase());
        caseWorkerProfile.setSuspended(staffProfileRequest.isSuspended());
        caseWorkerProfile.setUserTypeId(getUserTypeIdByDesc(staffProfileRequest.getUserType()));
        caseWorkerProfile.setRegionId(staffProfileRequest.getRegionId());
        caseWorkerProfile.setRegion(staffProfileRequest.getRegion());
        caseWorkerProfile.setCaseAllocator(staffProfileRequest.isCaseAllocator());
        caseWorkerProfile.setTaskSupervisor(staffProfileRequest.isTaskSupervisor());
        caseWorkerProfile.setUserAdmin(staffProfileRequest.isStaffAdmin());
        return caseWorkerProfile;
    }

    // get the userTypeId by description.
    public Long getUserTypeIdByDesc(String userTypeReq) {
        Optional<Long> userTypeId = caseWorkerStaticValueRepositoryAccessor
                .getUserTypes()
                .stream().filter(userType ->
                        userType.getDescription().equalsIgnoreCase(userTypeReq.trim()))
                .map(UserType::getUserTypeId).findFirst();
        return userTypeId.orElse(0L);
    }

    public List<CaseWorkerLocation> mapCaseWorkerLocationRequest(String idamId,
                                                                 StaffProfileCreationRequest staffProfileRequest) {
        List<CaseWorkerLocation> cwLocations = new ArrayList<>();
        staffProfileRequest.getBaseLocations().forEach(location -> {

            CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation(idamId,
                    location.getLocationId(), location.getLocation(), location.isPrimaryFlag());
            cwLocations.add(caseWorkerLocation);
        });
        return cwLocations;
    }

    public List<CaseWorkerRole> mapCaseWorkerRoleRequestMapping(String idamId,
                                                                StaffProfileCreationRequest staffProfileRequest) {
        List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();
        staffProfileRequest.getRoles().forEach(role -> caseWorkerStaticValueRepositoryAccessor
                .getRoleTypes()
                .stream().filter(roleType ->
                        role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                .forEach(roleType -> {
                    CaseWorkerRole workerRole = new CaseWorkerRole(idamId, roleType.getRoleId(), role.isPrimaryFlag());
                    caseWorkerRoles.add(workerRole);
                }));
        return caseWorkerRoles;
    }


    private List<CaseWorkerSkill> mapCaseWorkerSkillRequestMapping(String idamId,
                                                                 StaffProfileCreationRequest staffProfileRequest) {
        List<CaseWorkerSkill> caseWorkerSkills = new ArrayList<>();
        staffProfileRequest.getSkills().forEach(skill -> caseWorkerStaticValueRepositoryAccessor
                .getSkills()
                .stream().filter(skills ->
                        skill.getDescription().equalsIgnoreCase(skills.getDescription().trim()))
                .forEach(skills -> {
                    CaseWorkerSkill workerSkill = new CaseWorkerSkill(idamId, skills.getSkillId());
                    caseWorkerSkills.add(workerSkill);
                }));
        return caseWorkerSkills;

    }
    /**
     * Prepare caseworker data to be published as a message to topic.
     *
     * @param caseWorkerData list containing caseworker data
     */

    @Override
    public void publishStaffProfileToTopic(StaffProfileCreationResponse staffProfileCreationResponse) {

        topicPublisher.sendMessage(List.of(staffProfileCreationResponse.getCaseWorkerId()));
    }

}