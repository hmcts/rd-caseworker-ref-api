package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import feign.FeignException;
import feign.Response;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.Set.copyOf;
import static java.util.stream.Collectors.toList;
import static net.logstash.logback.encoder.org.apache.commons.lang3.BooleanUtils.isNotTrue;

@Service
@Slf4j
@Setter
public class CaseWorkerServiceImpl implements CaseWorkerService {


    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepo;

    @Autowired
    RoleTypeRepository roleTypeRepository;

    @Autowired
    UserTypeRepository userTypeRepository;

    @Autowired
    CaseWorkerIdamRoleAssociationRepository cwIdamRoleAssocRepository;

    @Autowired
    private UserProfileFeignClient userProfileFeignClient;

    List<RoleType> roleTypes = new ArrayList<>();

    List<UserType> userTypes = new ArrayList<>();


    @Override
    public ResponseEntity<Object> processCaseWorkerProfiles(List<CaseWorkersProfileCreationRequest>
                                                                cwrsProfilesCreationRequest) {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        try {
            getRolesAndUserTypes();
            cwrsProfilesCreationRequest.forEach(cwrProfileCreationRequest -> {
                CaseWorkerProfile caseWorkerProfile = caseWorkerProfileRepo
                    .findByEmailId(cwrProfileCreationRequest.getEmailId().toLowerCase());
                if (Objects.isNull(caseWorkerProfile)) {

                    caseWorkerProfile = createCaseWorkerProfile(cwrProfileCreationRequest);
                    if (Objects.nonNull(caseWorkerProfile)) {
                        // collecting all the successfully case worker profiles to save in caseworker db.
                        caseWorkerProfiles.add(caseWorkerProfile);

                    }


                } else if (Objects.nonNull(caseWorkerProfile) && !caseWorkerProfile.getDeleteFlag()) {
                    //update the existing case worker profile logic

                } else if (Objects.nonNull(caseWorkerProfile) && caseWorkerProfile.getDeleteFlag()) {

                    UserProfileUpdatedData usrProfileStatusUpdate = UserProfileUpdatedData.builder()
                        .idamStatus("SUSPENDED").build();
                    // updating the status in idam to suspend.
                    modifyCaseWorkerUser(usrProfileStatusUpdate, caseWorkerProfile.getCaseWorkerId(), "EXUI");
                }
            });


            /**  caseworker profile bacth save
             in caseWorkerProfile request contains all the associated entities information and while
             saving it automatically saves data in the sub entities like cwlocation, cwWorkArea,cwRole and
             caseworker profile and no need to explicitly invoke the save method for each entities.
             */
            if (!CollectionUtils.isEmpty(caseWorkerProfiles)) {
                caseWorkerProfileRepo.saveAll(caseWorkerProfiles);
            }
            log.info("{}::case worker profiles inserted::{}", loggingComponentName, caseWorkerProfiles.size());

        } catch (Exception exp) {

            log.error("{}:: createCaseWorkerUserProfiles failed ::{}", loggingComponentName, exp);
        }
        return ResponseEntity
            .status(201)
            .body(new CaseWorkerProfileCreationResponse("Case Worker Profiles Created."));
    }

    public CaseWorkerProfile createCaseWorkerProfile(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();
        CaseWorkerProfile caseWorkerProfile = null;

        //User Profile Call
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(cwrdProfileRequest);
        if (Objects.nonNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful()
            && Objects.nonNull(responseEntity.getBody())) {

            UserProfileCreationResponse userProfileCreationResponse
                = (UserProfileCreationResponse) requireNonNull(responseEntity.getBody());

            // case worker profile request mapping
            caseWorkerProfile = mapCaseWorkerProfileRequest(userProfileCreationResponse, cwrdProfileRequest);

            //Locations data request mapping and setting to case worker profile
            caseWorkerProfile.setCaseWorkerLocations(mapCaseWorkerLocationRequest(userProfileCreationResponse,
                cwrdProfileRequest));
            //caseWorkerRoles roles request mapping and data setting to case worker profile
            caseWorkerProfile.setCaseWorkerRoles(mapCaseWorkerRoleRequestMapping(userProfileCreationResponse,
                cwrdProfileRequest));

            //caseworkerworkarea request mapping
            cwrdProfileRequest.getWorkerWorkAreaRequests().forEach(caseWorkerWorkAreaRequest -> {
                CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea(userProfileCreationResponse.getIdamId(),
                    caseWorkerWorkAreaRequest.getAreaOfWork(), caseWorkerWorkAreaRequest.getServiceCode());
                caseWorkerWorkAreas.add(caseWorkerWorkArea);
            });

            //caseWorkerWorkAreas setting to case worker profile
            caseWorkerProfile.setCaseWorkerWorkAreas(caseWorkerWorkAreas);
        } else if(Objects.nonNull(responseEntity) && responseEntity.getStatusCode().equals(HttpStatus.CONFLICT)
            && Objects.nonNull(responseEntity.getBody())) {

            updateUserRolesWhenUserExistsInUpAndIdam(cwrdProfileRequest, responseEntity);

        } else {
            //Add failed request to list and save to the exception table
            log.error("{}::Idam register user failed", loggingComponentName);

        }
        return caseWorkerProfile;
    }

    private void updateUserRolesWhenUserExistsInUpAndIdam(CaseWorkersProfileCreationRequest cwrProfileRequest,
                                                          ResponseEntity<Object> responseEntity) {
        UserProfileCreationResponse userProfileCreationResponse
            = (UserProfileCreationResponse) requireNonNull(responseEntity.getBody());

        String sidamId = userProfileCreationResponse.getIdamId();

        Response response = userProfileFeignClient.getUserProfileWithRolesById(sidamId);

        ResponseEntity<Object> responseResponseEntity = JsonFeignResponseUtil.toResponseEntity(response,
            UserProfileResponse.class);


        UserProfileResponse userProfileResponse =
            (UserProfileResponse) requireNonNull(responseResponseEntity.getBody());
        Set<String>  userProfileRoles = copyOf(userProfileResponse.getRoles());
        Set<String> idamRolesCwr = cwrProfileRequest.getIdamRoles();
        if(isNotTrue(userProfileRoles.equals(idamRolesCwr))) {
            List<String> mergedRoles = userProfileRoles.stream().filter(s ->
                !idamRolesCwr.contains(s)).collect(toList());
            UserProfileUpdatedData usrProfileStatusUpdate = UserProfileUpdatedData.builder()
                .roles(mergedRoles).build();
            modifyCaseWorkerUser(usrProfileStatusUpdate, sidamId, "EXUI");
        }
    }

    public CaseWorkerProfile mapCaseWorkerProfileRequest(UserProfileCreationResponse userProfileCreationResponse,
                                                         CaseWorkersProfileCreationRequest cwrdProfileRequest) {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId(userProfileCreationResponse.getIdamId());
        caseWorkerProfile.setFirstName(cwrdProfileRequest.getFirstName());
        caseWorkerProfile.setLastName(cwrdProfileRequest.getLastName());
        caseWorkerProfile.setEmailId(cwrdProfileRequest.getEmailId().toLowerCase());
        caseWorkerProfile.setDeleteFlag(false);
        caseWorkerProfile.setUserTypeId(getUserTypeIdByDesc(cwrdProfileRequest.getUserType()));
        caseWorkerProfile.setRegionId(cwrdProfileRequest.getRegionId());
        caseWorkerProfile.setRegion(cwrdProfileRequest.getRegion());
        return caseWorkerProfile;
    }

    public List<CaseWorkerLocation> mapCaseWorkerLocationRequest(UserProfileCreationResponse userProfileCreationResp,
                                                                 CaseWorkersProfileCreationRequest cwrdProfileRequest) {
        List<CaseWorkerLocation> cwLocations = new ArrayList<>();
        cwrdProfileRequest.getBaseLocations().forEach(location -> {

            CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation(userProfileCreationResp.getIdamId(),
                location.getLocationId(), location.getLocation(), location.isPrimaryFlag());
            cwLocations.add(caseWorkerLocation);
        });
        return cwLocations;
    }

    public List<CaseWorkerRole> mapCaseWorkerRoleRequestMapping(UserProfileCreationResponse userProfileCreationResp,
                                                                CaseWorkersProfileCreationRequest cwrdProfileRequest) {
        List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();
        cwrdProfileRequest.getRoles().forEach(role -> {
            roleTypes.stream().filter(roleType ->
                role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                .forEach(roleType -> {
                    CaseWorkerRole workerRole = new CaseWorkerRole(userProfileCreationResp.getIdamId(),
                        roleType.getRoleId(), role.isPrimaryFlag());
                    caseWorkerRoles.add(workerRole);
                });
        });
        return caseWorkerRoles;
    }

    /**
     * Idam_UP call.
     */
    private ResponseEntity<Object> createUserProfileInIdamUP(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        Response response = null;
        Object clazz = null;
        try {
            response = userProfileFeignClient.createUserProfile(createUserProfileRequest(cwrdProfileRequest));
            if (response.status() == 201 || response.status() == 409) {
                clazz = UserProfileCreationResponse.class;
            } else {
                clazz = ErrorResponse.class;
            }

        } catch (FeignException ex) {
            log.error("{}:: UserProfile api failed:: status code {} & message {}",
                loggingComponentName,
                ex.status(), ex.getMessage());
            clazz = ErrorResponse.class;
        }
        return JsonFeignResponseUtil.toResponseEntity(response, clazz);
    }


    public ResponseEntity<Object> modifyCaseWorkerUser(UserProfileUpdatedData userProfileUpdatedData,
                                                       String userId, String origin) {
        Response response = null;
        Object clazz = null;
        try {

            response = userProfileFeignClient.modifyUserRoles(userProfileUpdatedData, userId, origin);
            if (response.status() == 200) {
                clazz = UserProfileCreationResponse.class;
            } else {
                clazz = ErrorResponse.class;
            }
        } catch (FeignException ex) {
            log.error("{}:: UserProfile api failed:: status code {}", loggingComponentName, ex.status());
            clazz = ErrorResponse.class;
        }
        return JsonFeignResponseUtil.toResponseEntity(response, clazz);
    }

    /**
     * creating user profile request.
     */
    public UserProfileCreationRequest createUserProfileRequest(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        Set<String> userRoles = cwrdProfileRequest.getIdamRoles() != null ? cwrdProfileRequest.getIdamRoles() :
            new HashSet<String>();
        userRoles.add("cwd-user");
        Set<String> idamRoles = getUserRolesByRoleId(cwrdProfileRequest);
        if (idamRoles.size() > 0) {
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

    /**
     * get the roleTypes and userTypes.
     */
    public void getRolesAndUserTypes() {

        if (roleTypes.isEmpty()) {
            roleTypes = roleTypeRepository.findAll();
        }

        if (userTypes.isEmpty()) {
            userTypes = userTypeRepository.findAll();
        }
    }

    /**
     * get the roles that needs to send to idam based on the roleType in the request.
     */
    Set<String> getUserRolesByRoleId(CaseWorkersProfileCreationRequest cwrdProfileRequest) {
        List<CaseWorkerIdamRoleAssociation> idamRolesInRequest = new ArrayList<>();
        cwrdProfileRequest.getRoles().forEach(role -> {
            roleTypes.stream().filter(roleType -> role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                .map(roleType -> {
                    List<CaseWorkerIdamRoleAssociation> caseWorkerIdamRoles = cwIdamRoleAssocRepository
                        .findByRoleType(roleType);
                    return idamRolesInRequest.addAll(caseWorkerIdamRoles);
                }).collect(toList());
        });

        Set<String> idamRoles = idamRolesInRequest.stream().map(idamRole -> {
            return idamRole.getIdamRole();
        }).collect(Collectors.toSet());

        return idamRoles;
    }

    /**
     * get the userTypeId by description.
     */
    public Long getUserTypeIdByDesc(String userTypeReq) {
        Optional<Long> userTypeId = userTypes.stream().filter(userType ->
            userType.getDescription().equalsIgnoreCase(userTypeReq.trim()))
            .map(userType -> userType.getUserTypeId()).findFirst();
        return userTypeId.orElse(0L);
    }

}

