package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

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
                if (Objects.isNull(caseWorkerProfile)) {

                    caseWorkerProfile = createCaseWorkerProfile(cwrProfileCreationRequest);
                    if (Objects.nonNull(caseWorkerProfile)) {
                        // collecting all the successfully case worker profiles to save in caseworker db.
                        caseWorkerProfiles.add(caseWorkerProfile);

                    }
                } else if (Objects.nonNull(caseWorkerProfile) && !caseWorkerProfile.getSuspended()) {

                    //update the existing case worker profile logic
                } else if (Objects.nonNull(caseWorkerProfile) && caseWorkerProfile.getSuspended()) {

                    UserProfileUpdatedData usrProfileStatusUpdate = new UserProfileUpdatedData("SUSPENDED");
                    // updating the status in idam to suspend.
                    modifyCaseWorkerUserStatus(usrProfileStatusUpdate,caseWorkerProfile.getCaseWorkerId(),"EXUI");
                }
            });


            /**  caseworker profile batch save
             in caseWorkerProfile request contains all the associated entities information and while
             saving it automatically saves data in the sub entities like cwlocation, cwWorkArea,cwRole and
             caseworker profile and no need to explicitly invoke the save method for each entities.
             */
            if (! CollectionUtils.isEmpty(caseWorkerProfiles)) {
                long time1 = System.currentTimeMillis();
                processedCwProfiles = caseWorkerProfileRepo.saveAll(caseWorkerProfiles);
                log.info("{}::Time taken to save caseworker data in CRD is {}", loggingComponentName,
                        (System.currentTimeMillis() - time1));
            }
            log.info("{}::case worker profiles inserted::{}", loggingComponentName, caseWorkerProfiles.size());

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
            caseWorkerIdamRoleAssociation.setRoleId(serviceRoleMapping.getRoleId().longValue());
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
        if (CollectionUtils.isEmpty(caseWorkerProfileList)) {
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

        List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();
        CaseWorkerProfile caseWorkerProfile = null;

        //User Profile Call
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(cwrdProfileRequest);
        if (Objects.nonNull(responseEntity) && responseEntity.getStatusCode().is2xxSuccessful()
                && Objects.nonNull(responseEntity.getBody())) {
            UserProfileCreationResponse userProfileCreationResponse
                    = (UserProfileCreationResponse) requireNonNull(responseEntity.getBody());

            // case worker profile request mapping
            caseWorkerProfile = mapCaseWorkerProfileRequest(userProfileCreationResponse,cwrdProfileRequest);

            //Locations data request mapping and setting to case worker profile
            caseWorkerProfile.setCaseWorkerLocations(mapCaseWorkerLocationRequest(userProfileCreationResponse,
                    cwrdProfileRequest));
            //caseWorkerRoles roles request mapping and data setting to case worker profile
            caseWorkerProfile.setCaseWorkerRoles(mapCaseWorkerRoleRequestMapping(userProfileCreationResponse,
                    cwrdProfileRequest));

            //caseworkerworkarea request mapping
            cwrdProfileRequest.getWorkerWorkAreaRequests().forEach(caseWorkerWorkAreaRequest -> {
                CaseWorkerWorkArea caseWorkerWorkArea =  new CaseWorkerWorkArea(userProfileCreationResponse.getIdamId(),
                        caseWorkerWorkAreaRequest.getAreaOfWork(), caseWorkerWorkAreaRequest.getServiceCode());
                caseWorkerWorkAreas.add(caseWorkerWorkArea);
            });

            //caseWorkerWorkAreas setting to case worker profile
            caseWorkerProfile.setCaseWorkerWorkAreas(caseWorkerWorkAreas);
        } else {
            //Add failed request to list and save to the exception table
            log.error("{}::Idam register user failed", loggingComponentName);

        }
        return caseWorkerProfile;
    }

    public CaseWorkerProfile mapCaseWorkerProfileRequest(UserProfileCreationResponse userProfileCreationResponse,
                                            CaseWorkersProfileCreationRequest cwrdProfileRequest) {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId(userProfileCreationResponse.getIdamId());
        caseWorkerProfile.setFirstName(cwrdProfileRequest.getFirstName());
        caseWorkerProfile.setLastName(cwrdProfileRequest.getLastName());
        caseWorkerProfile.setEmailId(cwrdProfileRequest.getEmailId().toLowerCase());
        caseWorkerProfile.setSuspended(false);
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
        cwrdProfileRequest.getRoles().forEach(role -> roleTypes.forEach(roleType -> {
            if (roleType.getDescription().trim().equalsIgnoreCase(role.getRole())) {
                CaseWorkerRole workerRole = new CaseWorkerRole(userProfileCreationResp.getIdamId(),
                        roleType.getRoleId(), role.isPrimaryFlag());
                caseWorkerRoles.add(workerRole);
            }
        }));
        return caseWorkerRoles;
    }

    /**
     *  Idam_UP call.
     *
     */
    private ResponseEntity<Object> createUserProfileInIdamUP(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        Response response = null;
        Object clazz = null;
        try {
            long time1 = System.currentTimeMillis();
            response = userProfileFeignClient.createUserProfile(createUserProfileRequest(cwrdProfileRequest));
            log.info("{}:: Time taken to call UP is {}", loggingComponentName, (System.currentTimeMillis() - time1));
            if (response.status() == 201 || response.status() == 409) {
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


    public ResponseEntity<Object> modifyCaseWorkerUserStatus(UserProfileUpdatedData userProfileUpdatedData,
                                                     String userId, String origin) {
        Response response = null;
        Object clazz = null;
        try {

            response = userProfileFeignClient.modifyUserRoles(userProfileUpdatedData, userId,origin);
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
     *
     * */
    public UserProfileCreationRequest createUserProfileRequest(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        Set<String> userRoles = cwrdProfileRequest.getIdamRoles() != null ? cwrdProfileRequest.getIdamRoles() :
                new HashSet<String>();
        userRoles.add("cwd-user");
        Set<String> idamRoles = getUserRolesByRoleId(cwrdProfileRequest);
        if (idamRoles.size() > 0) {
            userRoles.addAll(idamRoles);
        }
        //Creating user profile request
        return  new UserProfileCreationRequest(
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
     *  get the roleTypes and userTypes.
     */
    public  void getRolesAndUserTypes() {

        if (roleTypes.isEmpty()) {
            roleTypes = roleTypeRepository.findAll();
        }

        if (userTypes.isEmpty()) {
            userTypes = userTypeRepository.findAll();
        }
    }

    /**
     *  get the roles that needs to send to idam based on the roleType in the request.
     */
    Set<String> getUserRolesByRoleId(CaseWorkersProfileCreationRequest cwrdProfileRequest) {
        List<CaseWorkerIdamRoleAssociation>  idamRolesInRequest = new ArrayList<>();
        cwrdProfileRequest.getRoles().forEach(role -> {
            roleTypes.stream().filter(roleType -> role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                .map(roleType -> {
                    List<CaseWorkerIdamRoleAssociation>  caseWorkerIdamRoles = cwIdamRoleAssocRepository
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
    public Long getUserTypeIdByDesc(String  userTypeReq) {
        Optional<Long> userTypeId = userTypes.stream().filter(userType ->
                userType.getDescription().equalsIgnoreCase(userTypeReq.trim()))
                .map(userType -> userType.getUserTypeId()).findFirst();
        return userTypeId.orElse(0L);
    }

}

