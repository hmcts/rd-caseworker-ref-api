package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.FeignException;
import feign.Response;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.PublishCaseWorkerData;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.IdamRolesMappingException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRolesMappingResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleName;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Set.copyOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static net.logstash.logback.encoder.org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static net.logstash.logback.encoder.org.apache.commons.lang3.BooleanUtils.negate;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@Slf4j
@Setter
public class CaseWorkerServiceImpl implements CaseWorkerService {

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
    CaseWorkerIdamRoleAssociationRepository cwIdamRoleAssocRepository;

    @Autowired
    IdamRoleMappingService idamRoleMappingService;

    @Autowired
    private UserProfileFeignClient userProfileFeignClient;

    @Autowired
    private TopicPublisher topicPublisher;

    List<RoleType> roleTypes = new ArrayList<>();

    List<UserType> userTypes = new ArrayList<>();


    @Override
    public List<CaseWorkerProfile> processCaseWorkerProfiles(List<CaseWorkersProfileCreationRequest>
                                                                           cwrsProfilesCreationRequest) {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        List<CaseWorkerProfile> processedCwProfiles = new ArrayList<>();
        try {
            getRolesAndUserTypes();
            cwrsProfilesCreationRequest.forEach(cwrProfileCreationRequest -> {
                CaseWorkerProfile caseWorkerProfile = caseWorkerProfileRepo
                    .findByEmailId(cwrProfileCreationRequest.getEmailId().toLowerCase());

                //when profile is new then create new user profile
                //or
                //when existing profile with deleteflag is false in request then update roles with 409 scenario
                if (isNull(caseWorkerProfile) || (nonNull(caseWorkerProfile)
                        && negate(cwrProfileCreationRequest.isDeleteFlag()))) {

                    caseWorkerProfile = createCaseWorkerProfile(cwrProfileCreationRequest);
                    if (nonNull(caseWorkerProfile)) {
                        // collecting all the successfully case worker profiles to save in caseworker db.
                        caseWorkerProfiles.add(caseWorkerProfile);

                    }
                    //when existing profile with deleteflag is true in request then suspend user
                } else if (nonNull(caseWorkerProfile) && cwrProfileCreationRequest.isDeleteFlag()) {

                    UserProfileUpdatedData usrProfileStatusUpdate = UserProfileUpdatedData.builder()
                            .idamStatus("SUSPENDED").build();
                    // updating the status in idam to suspend.
                    modifyCaseWorkerUser(usrProfileStatusUpdate, caseWorkerProfile.getCaseWorkerId(), "EXUI");
                }
            });
            //  caseworker profile batch save
            if (negate(isEmpty(caseWorkerProfiles))) {
                processedCwProfiles = caseWorkerProfileRepo.saveAll(caseWorkerProfiles);
                log.info("{}:: case worker profiles inserted ::{}", loggingComponentName, processedCwProfiles.size());
            }
        } catch (Exception exp) {
            log.error("{}:: createCaseWorkerUserProfiles failed ::{}", loggingComponentName, exp);
        }
        return processedCwProfiles;
    }

    /**
     * Builds the idam role mappings for case worker roles.
     * @param serviceRoleMappings list of ServiceRoleMapping
     * @return list of CaseWorkerIdamRoleAssociation
     */
    @Override
    public IdamRolesMappingResponse buildIdamRoleMappings(List<ServiceRoleMapping> serviceRoleMappings) {
        List<CaseWorkerIdamRoleAssociation> caseWorkerIdamRoleAssociations = new ArrayList<>();
        Set<String> serviceCodes = new HashSet<>();
        serviceRoleMappings.forEach(serviceRoleMapping -> {
            CaseWorkerIdamRoleAssociation caseWorkerIdamRoleAssociation = new CaseWorkerIdamRoleAssociation();
            caseWorkerIdamRoleAssociation.setRoleId((long) serviceRoleMapping.getRoleId());
            caseWorkerIdamRoleAssociation.setIdamRole(serviceRoleMapping.getIdamRoles());
            caseWorkerIdamRoleAssociation.setServiceCode(serviceRoleMapping.getSerivceId());
            serviceCodes.add(serviceRoleMapping.getSerivceId());
            caseWorkerIdamRoleAssociations.add(caseWorkerIdamRoleAssociation);
        });
        try {
            idamRoleMappingService.deleteExistingRecordForServiceCode(serviceCodes);
            log.info("{}::" + CaseWorkerConstants.DELETE_RECORD_FOR_SERVICE_ID + " ::{}", loggingComponentName,
                    serviceCodes.toString());

            idamRoleMappingService.buildIdamRoleAssociation(caseWorkerIdamRoleAssociations);
            log.info("{}::" + CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS + "::{}", loggingComponentName,
                    serviceCodes.toString());

            return IdamRolesMappingResponse.builder()
                    .message(CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS + serviceCodes.toString())
                    .statusCode(HttpStatus.CREATED.value())
                    .build();

        } catch (Exception e) {
            log.error("{}::" + CaseWorkerConstants.IDAM_ROLE_MAPPINGS_FAILURE + " ::{}. Reason:: {}",
                    loggingComponentName, serviceCodes.toString(), e.getMessage());
            throw new IdamRolesMappingException(e.getMessage());
        }
    }

    /**
     * Prepare caseworker data to be published as a message to topic.
     * @param caseWorkerData list containing caseworker data
     */
    @Override
    public void publishCaseWorkerDataToTopic(List<CaseWorkerProfile> caseWorkerData) {
        List<String> caseWorkerIds = caseWorkerData.stream()
                .map(CaseWorkerProfile::getCaseWorkerId)
                .collect(Collectors.toUnmodifiableList());

        PublishCaseWorkerData publishCaseWorkerData = new PublishCaseWorkerData();
        ListUtils.partition(caseWorkerIds, caseWorkerDataPerMessage)
                .forEach(data -> {
                    publishCaseWorkerData.setUserIds(data);
                    topicPublisher.sendMessage(publishCaseWorkerData);
                });
    }

    public CaseWorkerProfile createCaseWorkerProfile(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();
        CaseWorkerProfile caseWorkerProfile = null;

        //User Profile Call
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(cwrdProfileRequest);
        if (nonNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful()
            && nonNull(responseEntity.getBody())) {

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
        } else if (nonNull(responseEntity) && responseEntity.getStatusCode().equals(HttpStatus.CONFLICT)
            && nonNull(responseEntity.getBody())) {

            updateUserRolesWhenUserExistsInUpAndIdam(cwrdProfileRequest, responseEntity);

        } else {
            //Add failed request to list and save to the exception table
            log.error("{}::Idam register user failed", loggingComponentName);

        }
        return caseWorkerProfile;
    }

    public void updateUserRolesWhenUserExistsInUpAndIdam(CaseWorkersProfileCreationRequest cwrProfileRequest,
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
        if (isNotTrue(userProfileRoles.equals(idamRolesCwr)) && negate(isEmpty(idamRolesCwr))) {
            Set<RoleName> mergedRoles = idamRolesCwr.stream()
                    .filter(s -> negate(userProfileRoles.contains(s)))
                    .map(uniqueRole -> new RoleName(uniqueRole))
                    .collect(toSet());
            if (negate(isEmpty(mergedRoles))) {
                UserProfileUpdatedData usrProfileStatusUpdate = UserProfileUpdatedData.builder()
                        .rolesAdd(mergedRoles).build();
                modifyCaseWorkerUser(usrProfileStatusUpdate, sidamId, "EXUI");
            }
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


    public void modifyCaseWorkerUser(UserProfileUpdatedData userProfileUpdatedData, String userId, String origin) {
        try {
            Response response = userProfileFeignClient.modifyUserRoles(userProfileUpdatedData, userId, origin);
            log.info("{}:: UserProfile update roles :: status code {} ", loggingComponentName, response.status());
        } catch (FeignException ex) {
            log.error("{}:: UserProfile api failed:: status code {}", loggingComponentName, ex.status());
        }
    }

    /**
     * creating user profile request.
     */
    public UserProfileCreationRequest createUserProfileRequest(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        Set<String> userRoles = cwrdProfileRequest.getIdamRoles() != null ? cwrdProfileRequest.getIdamRoles() :
            new HashSet<>();
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
        cwrdProfileRequest.getRoles().forEach(role ->
            roleTypes.stream()
                    .filter(roleType -> role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                    .map(roleType -> idamRolesInRequest.addAll(cwIdamRoleAssocRepository.findByRoleType(roleType)))
                    .collect(toList())
        );

        Set<String> idamRoles = idamRolesInRequest.stream()
                .map(idamRole -> idamRole.getIdamRole())
                .collect(Collectors.toSet());

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

