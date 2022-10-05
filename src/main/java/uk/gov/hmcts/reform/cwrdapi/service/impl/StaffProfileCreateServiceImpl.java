package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.FeignException;
import feign.Response;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.NewUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleName;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerSkillRepository;
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
import uk.gov.hmcts.reform.cwrdapi.util.StaffProfileCreateUpdateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Set.copyOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.springframework.http.HttpStatus.CONFLICT;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ALREADY_SUSPENDED_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_SUSPENDED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_USER_TO_SUSPEND;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ORIGIN_EXUI;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_ALREADY_CREATED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_NOT_PRESENT_IN_DB;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RESPONSE_BODY_MISSING_FROM_UP;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_CWD_USER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SRD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STATUS_ACTIVE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UP_CREATION_FAILED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UP_FAILURE_ROLES;
import static uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil.toResponseEntity;


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
//Added Extra
    @Autowired
    CaseWorkerStaticValueRepositoryAccessor caseWorkerStaticValueRepositoryAccessor;
    @Autowired
    CaseWorkerIdamRoleAssociationRepository roleAssocRepository;


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
        //StaffProfileCreationResponse response = null;

        validateStaffProfile.validateStaffProfile(profileRequest);

        checkStaffProfileForUpdate(profileRequest);
        //TODO processExistingCaseWorkers
        List<StaffProfileCreationRequest> cwUiRequests = Collections.singletonList(profileRequest);



        List<CaseWorkerProfile> caseWorkerProfiles =  updateStaffProfiles(cwUiRequests);
        StaffProfileCreationResponse response = new StaffProfileCreationResponse();
        CaseWorkerProfile caseWorkerProfile = caseWorkerProfiles.get(0);

        response.setCaseWorkerId(caseWorkerProfile.getCaseWorkerId());

        // processExistingCaseWorkers End

        //newCaseWorkerProfiles = updateCaseWorkerProfile(profileRequest);

        //processedCwProfiles = staffProfileCreateUpdateUtil.persistStaffProfile(newCaseWorkerProfiles);

        if (null != caseWorkerProfile) {

            validationServiceFacade.saveStaffAudit(AuditStatus.SUCCESS,null,
                    caseWorkerProfile.getCaseWorkerId(),profileRequest);
            response = StaffProfileCreationResponse.builder()
                    .caseWorkerId(caseWorkerProfile.getCaseWorkerId())
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

 //TODO Added temporary chenge need to remove

    public List<CaseWorkerProfile> updateStaffProfiles(List<StaffProfileCreationRequest> cwUiRequests) {

        List<CaseWorkerProfile> processedCwProfiles;
        List<CaseWorkerProfile> newCaseWorkerProfiles;
        Map<String, StaffProfileCreationRequest> emailToRequestMap = new HashMap<>();

        //create map for input request to email
        for (StaffProfileCreationRequest request : cwUiRequests) {
            emailToRequestMap.put(request.getEmailId().toLowerCase(), request);
        }

        try {
            // get all existing profiles from db (used IN clause)
            List<CaseWorkerProfile> cwDbProfiles = caseWorkerProfileRepo.findByEmailIdIn(emailToRequestMap.keySet());

            //remove all existing profiles requests from cwUiRequests to separate out new and update/suspend profiles
            for (CaseWorkerProfile dbProfile : cwDbProfiles) {
               // cwUiRequests.remove(emailToRequestMap.get(dbProfile.getEmailId().toLowerCase()));
            }

            //process new CW profiles
            //newCaseWorkerProfiles = processNewCaseWorkers(cwUiRequests);
            //process update and suspend CW profiles
            Pair<List<CaseWorkerProfile>, List<CaseWorkerProfile>> updateAndSuspendedLists = processExistingCaseWorkers(
                    emailToRequestMap, cwDbProfiles);
            // persist in db
            processedCwProfiles = persistCaseWorkerInBatch(updateAndSuspendedLists.getLeft(),
                    updateAndSuspendedLists.getRight(), emailToRequestMap);
        } catch (Exception exp) {
            log.error("{}:: createCaseWorkerUserProfiles failed :: Job Id {} ::{}", loggingComponentName,
                    validationServiceFacade.getAuditJobId(), exp);
            throw exp;
        }
        return processedCwProfiles;
    }

    //TODO remove method
    private List<CaseWorkerProfile> processNewCaseWorkers(List<StaffProfileCreationRequest> cwUiRequests) {
        List<CaseWorkerProfile> newCaseWorkerProfiles = new ArrayList<>();
        for (StaffProfileCreationRequest cwUiRequest : cwUiRequests) {
            if (cwUiRequest.isSuspended()) {
                //when suspending an user who does not exist in CW DB then log in exception table
                validationServiceFacade.logFailures(format(NO_USER_TO_SUSPEND, cwUiRequest.getRowId()),
                        cwUiRequest.getRowId());
                continue;
            }
            //when profile is new then create new user profile
            newCaseWorkerProfiles.add(createCaseWorkerProfileToRemove(cwUiRequest));
        }
        return newCaseWorkerProfiles;
    }

    public CaseWorkerProfile createCaseWorkerProfileToRemove(StaffProfileCreationRequest cwrdProfileRequest) {
        CaseWorkerProfile caseWorkerProfile = null;
        //User Profile Call
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUPToRemove(cwrdProfileRequest);
        if (nonNull(responseEntity) && (responseEntity.getStatusCode().is2xxSuccessful()
                || responseEntity.getStatusCode() == CONFLICT) && nonNull(responseEntity.getBody())) {

            UserProfileCreationResponse upResponse = (UserProfileCreationResponse) (responseEntity.getBody());
            if (nonNull(upResponse)) {
                caseWorkerProfile = new CaseWorkerProfile();
                //populateCaseWorkerProfile(cwrdProfileRequest, caseWorkerProfile, upResponse.getIdamId());

                staffProfileCreateUpdateUtil.populateStaffProfile(cwrdProfileRequest, caseWorkerProfile,
                        upResponse.getIdamId());
                if (responseEntity.getStatusCode() == CONFLICT) {
                    UserProfileCreationResponse userProfileCreationResponse
                            = (UserProfileCreationResponse) requireNonNull(responseEntity.getBody());
                    updateUserRolesInIdam(cwrdProfileRequest, userProfileCreationResponse.getIdamId());
                }
                //Avoid select Insert for new Inserts
                setNewCaseWorkerProfileFlag(caseWorkerProfile);
            } else {
                validationServiceFacade.logFailures(RESPONSE_BODY_MISSING_FROM_UP, cwrdProfileRequest.getRowId());
                log.error("{}:: {}:: Job Id {}:: Row Id {}", RESPONSE_BODY_MISSING_FROM_UP, loggingComponentName,
                        validationServiceFacade.getAuditJobId(), cwrdProfileRequest.getRowId());
            }
        }
        return caseWorkerProfile;
    }


    // Idam_UP call.
    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> createUserProfileInIdamUPToRemove(StaffProfileCreationRequest cwrdProfileRequest) {
        ResponseEntity<Object> responseEntity;
        Response response = null;
        Object clazz;
        try {
            response = userProfileFeignClient.createUserProfile(createUserProfileRequestToRemove(cwrdProfileRequest), SRD);

            clazz = (response.status() == 201 || response.status() == 409)
                    ? UserProfileCreationResponse.class : ErrorResponse.class;

            responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);
            if (clazz == ErrorResponse.class) {
                Object responseBody = responseEntity.getBody();
                if (nonNull(responseBody) && responseBody instanceof ErrorResponse) {
                    Optional<ErrorResponse> optional = Optional.ofNullable((ErrorResponse) responseBody);
                    validationServiceFacade.logFailures(
                            optional.map(ErrorResponse::getErrorDescription).orElse(UP_CREATION_FAILED),
                            cwrdProfileRequest.getRowId());
                } else {
                    validationServiceFacade.logFailures(UP_CREATION_FAILED, cwrdProfileRequest.getRowId());
                }
            }
            return responseEntity;
        } catch (Exception ex) {
            log.error("{}:: UserProfile api failed:: message {}:: Job Id {}:: Row Id {}", loggingComponentName,
                    ex.getMessage(), validationServiceFacade.getAuditJobId(), cwrdProfileRequest.getRowId());
            //Log UP failures
            validationServiceFacade.logFailures(ex.getMessage(), cwrdProfileRequest.getRowId());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            if (nonNull(response)) {
                response.close();
            }
        }
    }

    // creating user profile request
    public UserProfileCreationRequest createUserProfileRequestToRemove(StaffProfileCreationRequest cwrdProfileRequest) {

        Set<String> userRoles = new HashSet<>();
        userRoles.add(ROLE_CWD_USER);

        Set<String> idamRoles = staffProfileCreateUpdateUtil.getUserRolesByRoleId(cwrdProfileRequest);
        if (isNotEmpty(idamRoles)) {
            userRoles.addAll(idamRoles);
        }
        //Creating user profile request
        return new UserProfileCreationRequest(
                cwrdProfileRequest.getEmailId(),
                cwrdProfileRequest.getFirstName(),
                cwrdProfileRequest.getLastName(),
                LanguagePreference.EN,
                UserCategory.CASEWORKER,
                UserTypeRequest.INTERNAL,
                userRoles,
                false);
    }

    public Pair<List<CaseWorkerProfile>, List<CaseWorkerProfile>> processExistingCaseWorkers(Map<String,
            StaffProfileCreationRequest> emailToRequestMap, List<CaseWorkerProfile> caseWorkerProfiles) {

        List<CaseWorkerProfile> updateCaseWorkerProfiles = new ArrayList<>();
        List<CaseWorkerProfile> suspendedProfiles = new ArrayList<>();

        for (CaseWorkerProfile dbProfile : caseWorkerProfiles) {
            StaffProfileCreationRequest cwUiRequest = emailToRequestMap.get(dbProfile.getEmailId());
            if (isTrue(dbProfile.getSuspended())) {
                //when existing profile with delete flag is true then log exception add entry in exception table
                validationServiceFacade.logFailures(ALREADY_SUSPENDED_ERROR_MESSAGE, cwUiRequest.getRowId());
            } else if (cwUiRequest.isSuspended()) {
                //when existing profile with delete flag is true in request then suspend user
                if (isUserSuspended(UserProfileUpdatedData.builder().idamStatus(IDAM_STATUS_SUSPENDED).build(),
                        dbProfile.getCaseWorkerId(), ORIGIN_EXUI, cwUiRequest.getRowId())) {
                    dbProfile.setSuspended(true);
                    suspendedProfiles.add(dbProfile);
                }
            } else {
                //when existing profile with delete flag is false then update user in CRD db and roles in SIDAM
                updateCaseWorkerProfiles.add(dbProfile);
            }
        }
        //add user roles in user profile and filter out UP failed records
        List<CaseWorkerProfile> filteredProfiles = updateSidamRoles(updateCaseWorkerProfiles, emailToRequestMap);
        return Pair.of(filteredProfiles, suspendedProfiles);
    }

    public List<CaseWorkerProfile> persistCaseWorkerInBatch(
                                                            List<CaseWorkerProfile> updateCaseWorkerProfiles,
                                                            List<CaseWorkerProfile> suspendedCaseWorkerProfiles,
                                                            Map<String, StaffProfileCreationRequest>
                                                                    emailToRequestMap) {
        List<CaseWorkerProfile> processedCwProfiles = null;
        List<CaseWorkerProfile> profilesToBePersisted = new ArrayList<>();

        //profilesToBePersisted.addAll(newCaseWorkerProfiles);
        profilesToBePersisted.addAll(deleteChildrenAndUpdateCwProfiles(updateCaseWorkerProfiles, emailToRequestMap));
        profilesToBePersisted.addAll(suspendedCaseWorkerProfiles);
        profilesToBePersisted = profilesToBePersisted.stream().filter(Objects::nonNull).collect(toList());

        if (isNotEmpty(profilesToBePersisted)) {
            processedCwProfiles = caseWorkerProfileRepo.saveAll(profilesToBePersisted);
            log.info("{}:: {} case worker profiles inserted :: Job Id {}", loggingComponentName,
                    processedCwProfiles.size(), validationServiceFacade.getAuditJobId());
        }
        return processedCwProfiles;
    }

    // deletes children and updates caseworker profile
    private List<CaseWorkerProfile> deleteChildrenAndUpdateCwProfiles(List<CaseWorkerProfile> updateCaseWorkerProfiles,
                                                                      Map<String, StaffProfileCreationRequest>
                                                                              emailToRequestMap) {
        List<CaseWorkerProfile> updatedProfiles = new ArrayList<>();
        if (isNotEmpty(updateCaseWorkerProfiles)) {
            //TODO Need to discuss with Nayeem
            caseWorkerLocationRepository.deleteByCaseWorkerProfileIn(updateCaseWorkerProfiles);
            caseWorkerWorkAreaRepository.deleteByCaseWorkerProfileIn(updateCaseWorkerProfiles);
            caseWorkerRoleRepository.deleteByCaseWorkerProfileIn(updateCaseWorkerProfiles);
            caseWorkerSkillRepository.deleteByCaseWorkerProfileIn(updateCaseWorkerProfiles);
            cwrCommonRepository.flush();
            for (CaseWorkerProfile dbProfile : updateCaseWorkerProfiles) {
                updatedProfiles.add(updateUserProfile(emailToRequestMap.get(dbProfile.getEmailId()), dbProfile));
            }
        }
        return updatedProfiles;
    }

    public CaseWorkerProfile updateUserProfile(StaffProfileCreationRequest cwrdProfileRequest,
                                               CaseWorkerProfile caseWorkerProfile) {
        caseWorkerProfile.getCaseWorkerLocations().clear();
        caseWorkerProfile.getCaseWorkerWorkAreas().clear();
        caseWorkerProfile.getCaseWorkerRoles().clear();
        //update existing profile with file values
        staffProfileCreateUpdateUtil.populateStaffProfile(cwrdProfileRequest, caseWorkerProfile, caseWorkerProfile.getCaseWorkerId());
        return caseWorkerProfile;
    }



    public CaseWorkerProfile mapCaseWorkerProfileRequest(String idamId,
                                                         CaseWorkersProfileCreationRequest cwrdProfileRequest,
                                                         CaseWorkerProfile caseWorkerProfile) {
        caseWorkerProfile.setCaseWorkerId(idamId);
        caseWorkerProfile.setFirstName(cwrdProfileRequest.getFirstName());
        caseWorkerProfile.setLastName(cwrdProfileRequest.getLastName());
        caseWorkerProfile.setEmailId(cwrdProfileRequest.getEmailId().toLowerCase());
        caseWorkerProfile.setSuspended(false);
        caseWorkerProfile.setUserTypeId(getUserTypeIdByDesc(cwrdProfileRequest.getUserType()));
        caseWorkerProfile.setRegionId(cwrdProfileRequest.getRegionId());
        caseWorkerProfile.setRegion(cwrdProfileRequest.getRegion());
        caseWorkerProfile.setCaseAllocator(cwrdProfileRequest.isCaseAllocator());
        caseWorkerProfile.setTaskSupervisor(cwrdProfileRequest.isTaskSupervisor());
        return caseWorkerProfile;
    }

    public List<CaseWorkerLocation> mapCaseWorkerLocationRequest(String idamId,
                                                                 CaseWorkersProfileCreationRequest cwRequest) {
        List<CaseWorkerLocation> cwLocations = new ArrayList<>();
        cwRequest.getBaseLocations().forEach(location -> {

            CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation(idamId,
                    location.getLocationId(), location.getLocation(), location.isPrimaryFlag());
            cwLocations.add(caseWorkerLocation);
        });
        return cwLocations;
    }

    public List<CaseWorkerRole> mapCaseWorkerRoleRequestMapping(String idamId,
                                                                CaseWorkersProfileCreationRequest cwrdProfileRequest) {
        List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();
        cwrdProfileRequest.getRoles().forEach(role -> caseWorkerStaticValueRepositoryAccessor
                .getRoleTypes()
                .stream().filter(roleType ->
                        role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                .forEach(roleType -> {
                    CaseWorkerRole workerRole = new CaseWorkerRole(idamId, roleType.getRoleId(), role.isPrimaryFlag());
                    caseWorkerRoles.add(workerRole);
                }));
        return caseWorkerRoles;
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

    public List<CaseWorkerWorkArea> mapCwAreaOfWork(CaseWorkersProfileCreationRequest cwrdProfileRequest,
                                                    String idamId) {
        List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();
        cwrdProfileRequest.getWorkerWorkAreaRequests().forEach(caseWorkerWorkAreaRequest -> {
            CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea(idamId,
                    caseWorkerWorkAreaRequest.getAreaOfWork(), caseWorkerWorkAreaRequest.getServiceCode());
            caseWorkerWorkAreas.add(caseWorkerWorkArea);
        });
        return caseWorkerWorkAreas;
    }

    public boolean isUserSuspended(UserProfileUpdatedData userProfileUpdatedData, String userId,
                                   String origin, long rowId) {

        boolean status = true;
        try {
            Optional<Object> resultResponse = getUserProfileUpdateResponse(userProfileUpdatedData, userId, origin);

            if (!resultResponse.isPresent()
                    || (isNull(((UserProfileRolesResponse) resultResponse.get())
                    .getAttributeResponse()))
                    || (!(((UserProfileRolesResponse) resultResponse.get())
                    .getAttributeResponse().getIdamStatusCode().equals(HttpStatus.OK.value())))) {
                validationServiceFacade.logFailures(UP_FAILURE_ROLES, rowId);
                status = false;
            }

        } catch (Exception ex) {
            log.error("{}:: UserProfile modify api failed for row ID {} with error :: {}:: Job Id {}",
                    loggingComponentName, rowId, ex.getMessage(), validationServiceFacade.getAuditJobId());
            validationServiceFacade.logFailures(UP_FAILURE_ROLES, rowId);
            status = false;
        }
        return status;
    }


    private Optional<Object> getUserProfileUpdateResponse(UserProfileUpdatedData userProfileUpdatedData,
                                                          String userId, String origin) {
        Response response = userProfileFeignClient.modifyUserRoles(userProfileUpdatedData, userId, origin);
        log.info("{}:: UserProfile update roles :: status code {}:: Job Id {}", loggingComponentName,
                response.status(), validationServiceFacade.getAuditJobId());

        ResponseEntity<Object> responseEntity = toResponseEntity(response, UserProfileRolesResponse.class);

        return validateAndGetResponseEntity(responseEntity);
    }

    private Optional<Object> validateAndGetResponseEntity(ResponseEntity<Object> responseEntity) {
        if (nonNull(responseEntity)) {
            return Optional.ofNullable(responseEntity.getBody());
        }
        return Optional.empty();
    }

    // update roles in sidam and filter if failed in User profile
    public List<CaseWorkerProfile> updateSidamRoles(List<CaseWorkerProfile> updateCaseWorkerProfiles,
                                                    Map<String, StaffProfileCreationRequest> requestMap) {
        List<CaseWorkerProfile> filteredUpdateCwProfiles = new ArrayList<>();
        for (CaseWorkerProfile dbProfile : updateCaseWorkerProfiles) {
            boolean isAddRoleSuccess = updateUserRolesInIdam(requestMap.get(dbProfile.getEmailId()),
                    dbProfile.getCaseWorkerId());
            if (isAddRoleSuccess) {
                filteredUpdateCwProfiles.add(dbProfile);
            }
        }
        return filteredUpdateCwProfiles;
    }

    public boolean updateUserRolesInIdam(StaffProfileCreationRequest cwrProfileRequest, String idamId) {

        try {
            Response response = userProfileFeignClient.getUserProfileWithRolesById(idamId);
            ResponseEntity<Object> responseEntity = toResponseEntity(response, UserProfileResponse.class);

            Optional<Object> resultResponse = validateAndGetResponseEntity(responseEntity);
            if (resultResponse.isPresent() && resultResponse.get() instanceof UserProfileResponse profileResponse) {
                if (isNotTrue(profileResponse.getIdamStatus().equals(STATUS_ACTIVE))) {
                    validationServiceFacade.logFailures(String.format(IDAM_STATUS, profileResponse.getIdamStatus()),
                            cwrProfileRequest.getRowId());
                    return false;
                }

            } else {
                validationServiceFacade.logFailures(UP_FAILURE_ROLES, cwrProfileRequest.getRowId());
                return false;
            }
            UserProfileResponse userProfileResponse = (UserProfileResponse) requireNonNull(responseEntity.getBody());
            Set<String> mappedRoles = getUserRolesByRoleId(cwrProfileRequest);

            Set<String> userProfileRoles = copyOf(userProfileResponse.getRoles());
            Set<String> idamRolesCwr = isNotEmpty(cwrProfileRequest.getIdamRoles()) ? cwrProfileRequest.getIdamRoles() :
                    new HashSet<>();
            idamRolesCwr.addAll(mappedRoles);
            Set<RoleName> mergedRoles = new HashSet<>();
            if ((isNotTrue(userProfileRoles.equals(idamRolesCwr)) && isNotEmpty(idamRolesCwr))) {
                mergedRoles = idamRolesCwr.stream()
                        .filter(s -> !(userProfileRoles.contains(s)))
                        .map(RoleName::new)
                        .collect(toSet());
            }
            var hasNameChanged = !cwrProfileRequest.getFirstName().equals(userProfileResponse.getFirstName())
                    || !cwrProfileRequest.getLastName().equals(userProfileResponse.getLastName());
            if (isNotEmpty(mergedRoles) || hasNameChanged) {
                return updateMismatchedDatatoUP(cwrProfileRequest, idamId, mergedRoles, hasNameChanged);
            }
        } catch (Exception exception) {
            log.error("{}:: Update Users api failed:: message {}:: Job Id {}:: Row Id {}", loggingComponentName,
                    exception.getMessage(), validationServiceFacade.getAuditJobId(), cwrProfileRequest.getRowId());

            validationServiceFacade.logFailures(UP_FAILURE_ROLES, cwrProfileRequest.getRowId());
            return false;
        }
        return true;
    }

    private boolean updateMismatchedDatatoUP(StaffProfileCreationRequest cwrProfileRequest, String idamId,
                                             Set<RoleName> mergedRoles,
                                             boolean hasNameChanged) {
        UserProfileUpdatedData.UserProfileUpdatedDataBuilder builder = UserProfileUpdatedData.builder();

        if (isNotEmpty(mergedRoles)) {
            builder
                    .rolesAdd(mergedRoles);
        }

        if (hasNameChanged) {

            builder
                    .firstName(cwrProfileRequest.getFirstName())
                    .lastName(cwrProfileRequest.getLastName())
                    .idamStatus(STATUS_ACTIVE);

        }
        return isEachRoleUpdated(builder.build(), idamId, "EXUI",
                cwrProfileRequest.getRowId());
    }

    public boolean isEachRoleUpdated(UserProfileUpdatedData userProfileUpdatedData, String userId,
                                     String origin, long rowId) {
        boolean isEachRoleUpdated;
        try {
            Optional<Object> resultResponse = getUserProfileUpdateResponse(userProfileUpdatedData, userId, origin);

            if (resultResponse.isPresent() && resultResponse.get() instanceof UserProfileRolesResponse
                    userProfileRolesResponse) {
                if (nonNull(userProfileRolesResponse.getRoleAdditionResponse())
                        || nonNull(userProfileRolesResponse.getAttributeResponse())) {
                    isEachRoleUpdated = isRecordupdatedinUP(userProfileRolesResponse,rowId);

                } else {
                    validationServiceFacade.logFailures(UP_FAILURE_ROLES, rowId);
                    isEachRoleUpdated = false;
                }
            } else {
                validationServiceFacade.logFailures(UP_FAILURE_ROLES, rowId);
                isEachRoleUpdated = false;
            }

        } catch (Exception ex) {
            log.error("{}:: UserProfile modify api failed for row ID {} with error :: {}:: Job Id {}",
                    loggingComponentName, rowId, ex.getMessage(), validationServiceFacade.getAuditJobId());
            validationServiceFacade.logFailures(UP_FAILURE_ROLES, rowId);
            isEachRoleUpdated = false;
        }
        return isEachRoleUpdated;
    }

    private boolean isRecordupdatedinUP(UserProfileRolesResponse userProfileRolesResponse,long rowId) {

        boolean isRecordUpdate = true;
        if (nonNull(userProfileRolesResponse.getRoleAdditionResponse())
                && isNotTrue(userProfileRolesResponse.getRoleAdditionResponse().getIdamStatusCode()
                .equals(valueOf(HttpStatus.CREATED.value())))) {

            validationServiceFacade.logFailures(
                    userProfileRolesResponse.getRoleAdditionResponse().getIdamMessage(), rowId);
            isRecordUpdate = false;
        }
        if (nonNull(userProfileRolesResponse.getAttributeResponse())
                && !(userProfileRolesResponse.getAttributeResponse().getIdamStatusCode()
                .equals(Integer.valueOf(HttpStatus.OK.value())))) {

            validationServiceFacade.logFailures(
                    userProfileRolesResponse.getAttributeResponse().getIdamMessage(), rowId);
            isRecordUpdate = false;
        }
        return isRecordUpdate;
    }

    // get the roles that needs to send to idam based on the roleType in the request.
    Set<String> getUserRolesByRoleId(StaffProfileCreationRequest cwProfileRequest) {

        // get Roles Types
        List<RoleType> roleTypeList = new ArrayList<>();
        cwProfileRequest.getRoles().forEach(role -> roleTypeList.addAll(
                caseWorkerStaticValueRepositoryAccessor
                        .getRoleTypes()
                        .stream()
                        .filter(roleType -> role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                        .toList())
        );

        // get work area codes TODO Check for Data
//        List<String> serviceCodes = cwProfileRequest.getWorkerWorkAreaRequests()
//                .stream()
//                .map(CaseWorkerWorkAreaRequest::getServiceCode)
//                .toList();

        List<String> serviceCodes = cwProfileRequest.getServices()
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

    public void setNewCaseWorkerProfileFlag(CaseWorkerProfile caseWorkerProfile) {
        if (nonNull(caseWorkerProfile)) {
            caseWorkerProfile.setNew(true);
        }
    }

}