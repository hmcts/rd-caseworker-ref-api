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
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.PublishCaseWorkerData;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.IdamRolesMappingException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
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
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Set.copyOf;
import static java.util.stream.Collectors.toSet;
import static net.logstash.logback.encoder.org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_SUSPENDED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ORIGIN_EXUI;
import static uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil.toResponseEntity;

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
    CaseWorkerLocationRepository caseWorkerLocationRepository;

    @Autowired
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @Autowired
    CaseWorkerRoleRepository caseWorkerRoleRepository;

    @Autowired
    IdamRoleMappingService idamRoleMappingService;

    @Autowired
    private UserProfileFeignClient userProfileFeignClient;

    @Autowired
    private TopicPublisher topicPublisher;

    List<RoleType> roleTypes = new ArrayList<>();

    List<UserType> userTypes = new ArrayList<>();


    @Override
    public List<CaseWorkerProfile> processCaseWorkerProfiles(List<CaseWorkersProfileCreationRequest> cwRequests) {
        List<CaseWorkerProfile> newCaseWorkerProfiles = new ArrayList<>();
        List<CaseWorkerProfile> updateCaseWorkerProfiles = new ArrayList<>();
        Map<String, CaseWorkersProfileCreationRequest> requestMap = new HashMap<>();
        List<CaseWorkerProfile> processedCwProfiles = new ArrayList<>();
        try {
            getRolesAndUserTypes();
            for (CaseWorkersProfileCreationRequest cwrRequest : cwRequests) {
                CaseWorkerProfile caseWorkerProfile = caseWorkerProfileRepo
                    .findByEmailId(cwrRequest.getEmailId().toLowerCase());
                if (isNull(caseWorkerProfile)) {
                    //when profile is new then create new user profile
                    caseWorkerProfile = createCaseWorkerProfile(cwrRequest);
                    newCaseWorkerProfiles.add(caseWorkerProfile);
                } else if (caseWorkerProfile.getSuspended()) {
                    //when existing profile with delete flag is true then log exception
                    // add entry in exception table
                } else if (cwrRequest.isSuspended()) {
                    //when existing profile with delete flag is true in request then suspend user
                    UserProfileUpdatedData usrProfileStatusUpdate = UserProfileUpdatedData.builder()
                            .idamStatus(IDAM_STATUS_SUSPENDED).build();
                    modifyCaseWorkerUser(usrProfileStatusUpdate, caseWorkerProfile.getCaseWorkerId(), ORIGIN_EXUI);
                    caseWorkerProfile.setSuspended(true);
                    newCaseWorkerProfiles.add(caseWorkerProfile);
                } else if (!caseWorkerProfile.getSuspended()) {
                    //when existing profile with delete flag is false then update user in CRD db and roles in SIDAM
                    requestMap.put(caseWorkerProfile.getEmailId(), cwrRequest);
                    updateCaseWorkerProfiles.add(caseWorkerProfile);
                }
            }
            processedCwProfiles = persistCaseWorkerInBatch(newCaseWorkerProfiles, updateCaseWorkerProfiles, requestMap);
        } catch (Exception exp) {
            log.error("{}:: createCaseWorkerUserProfiles failed ::{}", loggingComponentName, exp);
        }
        return processedCwProfiles;
    }

    public List<CaseWorkerProfile> persistCaseWorkerInBatch(List<CaseWorkerProfile> newCaseWorkerProfiles,
                                                            List<CaseWorkerProfile> updateCaseWorkerProfiles,
                                                            Map<String, CaseWorkersProfileCreationRequest> requestMap) {
        List<CaseWorkerProfile> processedCwProfiles = null;
        if (isNotEmpty(updateCaseWorkerProfiles)) {
            caseWorkerLocationRepository.deleteByCaseWorkerProfileIn(updateCaseWorkerProfiles);
            caseWorkerWorkAreaRepository.deleteByCaseWorkerProfileIn(updateCaseWorkerProfiles);
            caseWorkerRoleRepository.deleteByCaseWorkerProfileIn(updateCaseWorkerProfiles);
            updateCaseWorkerProfiles.forEach(updatedProfile ->
                updateUserProfile(requestMap.get(updatedProfile.getEmailId()), updatedProfile)
            );
            newCaseWorkerProfiles.addAll(updateCaseWorkerProfiles);
        }
        if (isNotEmpty(newCaseWorkerProfiles)) {
            processedCwProfiles = caseWorkerProfileRepo.saveAll(newCaseWorkerProfiles);
            log.info("{}:: case worker profiles inserted ::{}", loggingComponentName, processedCwProfiles.size());
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

    /**
     * Returns the caseworker details based on the id's.
     * @param caseWorkerIds list
     * @return CaseWorkerProfile
     */
    @Override
    public ResponseEntity<Object> fetchCaseworkersById(List<String> caseWorkerIds) {
        List<CaseWorkerProfile> caseWorkerProfileList = caseWorkerProfileRepo.findByCaseWorkerIdIn(caseWorkerIds);
        if (isEmpty(caseWorkerProfileList)) {
            throw new ResourceNotFoundException(CaseWorkerConstants.NO_DATA_FOUND);
        }

        return ResponseEntity.ok().body(mapCaseWorkerProfileToDto(caseWorkerProfileList));
    }

    private List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> mapCaseWorkerProfileToDto(
            List<CaseWorkerProfile> caseWorkerProfileList) {
        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> caseWorkerProfilesDto =
                new ArrayList<>();
        for (CaseWorkerProfile profile : caseWorkerProfileList) {

            caseWorkerProfilesDto.add(uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile.builder()
                    .id(profile.getCaseWorkerId())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .officialEmail(profile.getEmailId())
                    .regionId(profile.getRegionId())
                    .regionName(profile.getRegion())
                    .userType(profile.getUserType().getDescription())
                    .userId(profile.getUserTypeId())
                    .suspended(profile.getSuspended().toString())
                    .createdTime(profile.getCreatedDate())
                    .lastUpdatedTime(profile.getLastUpdate())
                    .roles(mapRolesToDto(profile.getCaseWorkerRoles()))
                    .locations(mapLocationsToDto(profile.getCaseWorkerLocations()))
                    .workAreas(mapWorkAreasToDto(profile.getCaseWorkerWorkAreas()))
                    .build());
        }
        return caseWorkerProfilesDto;
    }

    private List<WorkArea> mapWorkAreasToDto(List<CaseWorkerWorkArea> caseWorkerWorkAreas) {
        List<WorkArea> workAreasDtoList = new ArrayList<>();
        for (CaseWorkerWorkArea area : caseWorkerWorkAreas) {
            WorkArea workAreaDto = WorkArea.builder()
                    .areaOfWork(area.getAreaOfWork())
                    .serviceCode(area.getServiceCode())
                    .createdTime(area.getCreatedDate())
                    .lastUpdatedTime(area.getLastUpdate())
                    .build();

            workAreasDtoList.add(workAreaDto);
        }
        return workAreasDtoList;
    }

    private List<Location> mapLocationsToDto(List<CaseWorkerLocation> caseWorkerLocations) {
        List<Location> locationsDto = new ArrayList<>();
        for (CaseWorkerLocation location : caseWorkerLocations) {
            Location locationDto = Location.builder()
                    .baseLocationId(location.getLocationId())
                    .locationName(location.getLocation())
                    .createdTime(location.getCreatedDate())
                    .lastUpdatedTime(location.getLastUpdate())
                    .isPrimary(location.getPrimaryFlag())
                    .build();

            locationsDto.add(locationDto);
        }
        return locationsDto;
    }

    private List<Role> mapRolesToDto(List<CaseWorkerRole> caseWorkerRoles) {
        List<Role> rolesDto = new ArrayList<>();
        for (CaseWorkerRole caseWorkerRole : caseWorkerRoles) {
            Role roleDto = Role.builder()
                    .roleId(caseWorkerRole.getRoleId().toString())
                    .roleName(caseWorkerRole.getRoleType().getDescription())
                    .isPrimary(caseWorkerRole.getPrimaryFlag())
                    .createdTime(caseWorkerRole.getCreatedDate())
                    .lastUpdatedTime(caseWorkerRole.getLastUpdate()).build();
            rolesDto.add(roleDto);

        }
        return rolesDto;
    }

    public CaseWorkerProfile createCaseWorkerProfile(CaseWorkersProfileCreationRequest cwrdProfileRequest) {
        CaseWorkerProfile caseWorkerProfile = null;
        //User Profile Call
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(cwrdProfileRequest);
        if (nonNull(responseEntity) && (responseEntity.getStatusCode().is2xxSuccessful()
                || responseEntity.getStatusCode() == CONFLICT) && nonNull(responseEntity.getBody())) {

            UserProfileCreationResponse upResponse = (UserProfileCreationResponse) (responseEntity.getBody());
            if (nonNull(upResponse)) {
                caseWorkerProfile = new CaseWorkerProfile();
                populateCaseWorkerProfile(cwrdProfileRequest, caseWorkerProfile, upResponse.getIdamId());
                if (responseEntity.getStatusCode() == CONFLICT) {
                    UserProfileCreationResponse userProfileCreationResponse
                            = (UserProfileCreationResponse) requireNonNull(responseEntity.getBody());
                    updateUserRolesInIdam(cwrdProfileRequest, userProfileCreationResponse.getIdamId());
                }
            } else {
                log.error("{}::UP response missing body", loggingComponentName);
            }
        } else {
            //Add failed request to list and save to the exception table
            log.error("{}::Idam register user failed", loggingComponentName);
        }
        return caseWorkerProfile;
    }

    public void populateCaseWorkerProfile(CaseWorkersProfileCreationRequest cwrdProfileRequest,
                                                CaseWorkerProfile caseWorkerProfile, String idamId) {
        //case worker profile request mapping
        mapCaseWorkerProfileRequest(idamId, cwrdProfileRequest, caseWorkerProfile);
        //Locations data request mapping and setting to case worker profile
        caseWorkerProfile.getCaseWorkerLocations().addAll(mapCaseWorkerLocationRequest(idamId, cwrdProfileRequest));
        //caseWorkerRoles roles request mapping and data setting to case worker profile
        caseWorkerProfile.getCaseWorkerRoles().addAll(mapCaseWorkerRoleRequestMapping(idamId, cwrdProfileRequest));
        //caseWorkerWorkAreas setting to case worker profile
        caseWorkerProfile.getCaseWorkerWorkAreas().addAll(mapCwAreaOfWork(cwrdProfileRequest, idamId));
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

    public void updateUserProfile(CaseWorkersProfileCreationRequest cwrdProfileRequest,
                                  CaseWorkerProfile caseWorkerProfile) {
        caseWorkerProfile.getCaseWorkerLocations().clear();
        caseWorkerProfile.getCaseWorkerWorkAreas().clear();
        caseWorkerProfile.getCaseWorkerRoles().clear();
        //update existing profile with file values
        populateCaseWorkerProfile(cwrdProfileRequest, caseWorkerProfile, caseWorkerProfile.getCaseWorkerId());
        // update roles in sidam
        updateUserRolesInIdam(cwrdProfileRequest, caseWorkerProfile.getCaseWorkerId());
    }

    public void updateUserRolesInIdam(CaseWorkersProfileCreationRequest cwrProfileRequest, String idamId) {

        Response response = userProfileFeignClient.getUserProfileWithRolesById(idamId);

        ResponseEntity<Object> responseEntity = toResponseEntity(response, UserProfileResponse.class);

        UserProfileResponse userProfileResponse = (UserProfileResponse) requireNonNull(responseEntity.getBody());
        Set<String>  userProfileRoles = copyOf(userProfileResponse.getRoles());
        Set<String> idamRolesCwr = cwrProfileRequest.getIdamRoles();
        if (isNotTrue(userProfileRoles.equals(idamRolesCwr)) && isNotEmpty(idamRolesCwr)) {
            Set<RoleName> mergedRoles = idamRolesCwr.stream()
                    .filter(s -> !(userProfileRoles.contains(s)))
                    .map(RoleName::new)
                    .collect(toSet());
            if (isNotEmpty(mergedRoles)) {
                UserProfileUpdatedData usrProfileStatusUpdate = UserProfileUpdatedData.builder()
                        .rolesAdd(mergedRoles).build();
                modifyCaseWorkerUser(usrProfileStatusUpdate, idamId, "EXUI");
            }
        }
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
        cwrdProfileRequest.getRoles().forEach(role -> roleTypes.stream().filter(roleType ->
            role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
            .forEach(roleType -> {
                CaseWorkerRole workerRole = new CaseWorkerRole(idamId, roleType.getRoleId(), role.isPrimaryFlag());
                caseWorkerRoles.add(workerRole);
            }));
        return caseWorkerRoles;
    }

    /**
     * Idam_UP call.
     */
    private ResponseEntity<Object> createUserProfileInIdamUP(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        Response response = null;
        Object clazz;
        try {
            response = userProfileFeignClient.createUserProfile(createUserProfileRequest(cwrdProfileRequest));
            if (response.status() == 201 || response.status() == 409) {
                clazz = UserProfileCreationResponse.class;
            } else {
                clazz = ErrorResponse.class;
            }

        } catch (FeignException ex) {
            log.error("{}:: UserProfile api failed:: status code {} & message {}",
                loggingComponentName, ex.status(), ex.getMessage());
            clazz = ErrorResponse.class;
        }
        return toResponseEntity(response, clazz);
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
        if (!(idamRoles.isEmpty())) {
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
        );

        return idamRolesInRequest.stream()
                .map(CaseWorkerIdamRoleAssociation::getIdamRole)
                .collect(Collectors.toSet());
    }

    /**
     * get the userTypeId by description.
     */
    public Long getUserTypeIdByDesc(String userTypeReq) {
        Optional<Long> userTypeId = userTypes.stream().filter(userType ->
            userType.getDescription().equalsIgnoreCase(userTypeReq.trim()))
            .map(UserType::getUserTypeId).findFirst();
        return userTypeId.orElse(0L);
    }

}

