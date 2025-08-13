package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.Response;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.client.domain.StaffProfileWithServiceName;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.IdamRolesMappingException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.StaffReferenceException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.LocationReferenceDataFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRolesMappingResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.LrdOrgInfoServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
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
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerStaticValueRepositoryAccessor;
import uk.gov.hmcts.reform.cwrdapi.service.ICwrdCommonRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ALREADY_SUSPENDED_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.BAD_REQUEST;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_SUSPENDED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LRD_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_USER_TO_SUSPEND;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ORIGIN_EXUI;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RESPONSE_BODY_MISSING_FROM_UP;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_CWD_USER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SRD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STATUS_ACTIVE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UP_CREATION_FAILED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UP_FAILURE_ROLES;
import static uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil.toResponseEntity;
import static uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil.toResponseEntityWithListBody;

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


    public List<CaseWorkerProfile> processCaseWorkerProfiles(List<CaseWorkersProfileCreationRequest> cwUiRequests) {

        List<CaseWorkerProfile> processedCwProfiles;
        List<CaseWorkerProfile> newCaseWorkerProfiles;
        Map<String, CaseWorkersProfileCreationRequest> emailToRequestMap = new HashMap<>();

        //create map for input request to email
        for (CaseWorkersProfileCreationRequest request : cwUiRequests) {
            emailToRequestMap.put(request.getEmailId().toLowerCase(), request);
        }

        try {
            // get all existing profiles from db (used IN clause)
            List<CaseWorkerProfile> cwDbProfiles = caseWorkerProfileRepo.findByEmailIdIgnoreCaseIn(
                    emailToRequestMap.keySet());

            //remove all existing profiles requests from cwUiRequests to separate out new and update/suspend profiles
            for (CaseWorkerProfile dbProfile : cwDbProfiles) {
                cwUiRequests.remove(emailToRequestMap.get(dbProfile.getEmailId().toLowerCase()));
            }

            //process new CW profiles
            newCaseWorkerProfiles = processNewCaseWorkers(cwUiRequests);
            //process update and suspend CW profiles
            Pair<List<CaseWorkerProfile>, List<CaseWorkerProfile>> updateAndSuspendedLists = processExistingCaseWorkers(
                    emailToRequestMap, cwDbProfiles);
            // persist in db
            processedCwProfiles = persistCaseWorkerInBatch(newCaseWorkerProfiles, updateAndSuspendedLists.getLeft(),
                    updateAndSuspendedLists.getRight(), emailToRequestMap);
        } catch (Exception exp) {
            log.error("{}:: createCaseWorkerUserProfiles failed :: Job Id {} ::{}", loggingComponentName,
                    validationServiceFacade.getAuditJobId(), exp);
            throw exp;
        }
        return processedCwProfiles;
    }

    public void setNewCaseWorkerProfileFlag(CaseWorkerProfile caseWorkerProfile) {
        if (nonNull(caseWorkerProfile)) {
            caseWorkerProfile.setNew(true);
        }
    }

    public Pair<List<CaseWorkerProfile>, List<CaseWorkerProfile>> processExistingCaseWorkers(Map<String,
            CaseWorkersProfileCreationRequest> emailToRequestMap, List<CaseWorkerProfile> caseWorkerProfiles) {

        List<CaseWorkerProfile> updateCaseWorkerProfiles = new ArrayList<>();
        List<CaseWorkerProfile> suspendedProfiles = new ArrayList<>();

        for (CaseWorkerProfile dbProfile : caseWorkerProfiles) {
            CaseWorkersProfileCreationRequest cwUiRequest = emailToRequestMap.get(dbProfile.getEmailId());
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

    public List<CaseWorkerProfile> processNewCaseWorkers(List<CaseWorkersProfileCreationRequest> cwUiRequests) {
        List<CaseWorkerProfile> newCaseWorkerProfiles = new ArrayList<>();
        for (CaseWorkersProfileCreationRequest cwUiRequest : cwUiRequests) {
            if (cwUiRequest.isSuspended()) {
                //when suspending an user who does not exist in CW DB then log in exception table
                validationServiceFacade.logFailures(format(NO_USER_TO_SUSPEND, cwUiRequest.getRowId()),
                        cwUiRequest.getRowId());
                continue;
            }
            //when profile is new then create new user profile
            newCaseWorkerProfiles.add(createCaseWorkerProfile(cwUiRequest));
        }
        return newCaseWorkerProfiles;
    }

    public List<CaseWorkerProfile> persistCaseWorkerInBatch(List<CaseWorkerProfile> newCaseWorkerProfiles,
                                                            List<CaseWorkerProfile> updateCaseWorkerProfiles,
                                                            List<CaseWorkerProfile> suspendedCaseWorkerProfiles,
                                                            Map<String, CaseWorkersProfileCreationRequest>
                                                                    emailToRequestMap) {
        List<CaseWorkerProfile> processedCwProfiles = null;
        List<CaseWorkerProfile> profilesToBePersisted = new ArrayList<>();

        profilesToBePersisted.addAll(newCaseWorkerProfiles);
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
                                                                      Map<String, CaseWorkersProfileCreationRequest>
                                                                              emailToRequestMap) {
        List<CaseWorkerProfile> updatedProfiles = new ArrayList<>();
        if (isNotEmpty(updateCaseWorkerProfiles)) {
            caseWorkerLocationRepository.deleteByCaseWorkerProfileIn(updateCaseWorkerProfiles);
            caseWorkerWorkAreaRepository.deleteByCaseWorkerProfileIn(updateCaseWorkerProfiles);
            caseWorkerRoleRepository.deleteByCaseWorkerProfileIn(updateCaseWorkerProfiles);
            cwrCommonRepository.flush();
            for (CaseWorkerProfile dbProfile : updateCaseWorkerProfiles) {
                updatedProfiles.add(updateUserProfile(emailToRequestMap.get(dbProfile.getEmailId()), dbProfile));
            }
        }
        return updatedProfiles;
    }

    // update roles in sidam and filter if failed in User profile
    public List<CaseWorkerProfile> updateSidamRoles(List<CaseWorkerProfile> updateCaseWorkerProfiles,
                                                    Map<String, CaseWorkersProfileCreationRequest> requestMap) {
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

    /**
     * Builds the idam role mappings for case worker roles.
     *
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
            caseWorkerIdamRoleAssociation.setServiceCode(serviceRoleMapping.getServiceId());
            serviceCodes.add(serviceRoleMapping.getServiceId());
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
            log.error("{}::" + CaseWorkerConstants.IDAM_ROLE_MAPPINGS_FAILURE + " ::{}:: Job Id {}::Reason:: {}",
                    loggingComponentName, serviceCodes.toString(), validationServiceFacade.getAuditJobId(),
                    e.getMessage());
            throw new IdamRolesMappingException(e.getMessage());
        }
    }

    /**
     * Prepare caseworker data to be published as a message to topic.
     *
     * @param caseWorkerData list containing caseworker data
     */
    @Override
    public void publishCaseWorkerDataToTopic(List<CaseWorkerProfile> caseWorkerData) {
        List<String> caseWorkerIds = caseWorkerData.stream()
                .map(CaseWorkerProfile::getCaseWorkerId)
                .toList();

        topicPublisher.sendMessage(caseWorkerIds);
    }

    /**
     * Returns the caseworker details based on the id's.
     *
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

    /**
     * Returns the staff details for Refresh Assignments.
     */
    @Override
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> fetchStaffProfilesForRoleRefresh(String ccdServiceNames, PageRequest pageRequest) {
        Response lrdOrgInfoServiceResponse =
                locationReferenceDataFeignClient.getLocationRefServiceMapping(ccdServiceNames);
        HttpStatus httpStatus = HttpStatus.valueOf(lrdOrgInfoServiceResponse.status());
        if (httpStatus.is2xxSuccessful()) {
            ResponseEntity<Object> responseEntity = toResponseEntityWithListBody(lrdOrgInfoServiceResponse,
                    LrdOrgInfoServiceResponse.class);

            List<LrdOrgInfoServiceResponse> listLrdServiceMapping =
                    (List<LrdOrgInfoServiceResponse>) responseEntity.getBody();
            if (CollectionUtils.isNotEmpty(listLrdServiceMapping)) {
                Map<String, String> serviceNameToCodeMapping =
                        listLrdServiceMapping
                                .stream()
                                .filter(r -> StringUtils.isNotBlank(r.getServiceCode())
                                        && StringUtils.isNotBlank(r.getCcdServiceName()))
                                .collect(Collectors.toMap(LrdOrgInfoServiceResponse::getServiceCode,
                                        LrdOrgInfoServiceResponse::getCcdServiceName));

                Page<CaseWorkerProfile> staffProfilePage = caseWorkerProfileRepo.findByServiceCodeIn(
                        serviceNameToCodeMapping.keySet(), pageRequest);

                log.info("{}:: No of Pages {} No of Records returned from DB {}", loggingComponentName,
                        staffProfilePage.getTotalPages(),
                        staffProfilePage.getTotalElements());

                if (staffProfilePage.isEmpty()) {
                    log.error("{}:: No data found in CRD for the ccd service name {}",
                            loggingComponentName, ccdServiceNames);
                    throw new ResourceNotFoundException(CaseWorkerConstants.NO_DATA_FOUND);
                }

                List<StaffProfileWithServiceName> staffProfileList = new ArrayList<>();
                Set<CaseWorkerWorkArea> caseWorkerWorkArea = new LinkedHashSet<>();

                staffProfilePage.forEach(caseWorkerProfile -> caseWorkerProfile.getCaseWorkerWorkAreas()
                        .stream()
                        .filter(cw -> serviceNameToCodeMapping.containsKey(cw.getServiceCode()))
                        .forEach(caseWorkerWorkArea::add));

                caseWorkerWorkArea.forEach(workArea -> staffProfileList.add(
                        StaffProfileWithServiceName.builder()
                                .ccdServiceName(serviceNameToCodeMapping.get(workArea.getServiceCode()))
                                .staffProfile(buildCaseWorkerProfileDto(workArea.getCaseWorkerProfile()))
                                .build()));
                logMessages(ccdServiceNames, serviceNameToCodeMapping, staffProfileList);
                return ResponseEntity
                        .ok()
                        .header("total_records", String.valueOf(staffProfilePage.getTotalElements()))
                        .body(staffProfileList);
            }
        }
        log.error("{}:: Error in getting the data from LRD for the ccd service name {} :: Status code {}",
                loggingComponentName, ccdServiceNames, httpStatus);

        ResponseEntity<Object> responseEntity = JsonFeignResponseUtil.toResponseEntity(lrdOrgInfoServiceResponse,
                ErrorResponse.class);
        Object responseBody = responseEntity.getBody();
        if (nonNull(responseBody) && responseBody instanceof ErrorResponse errorResponse) {

            throw new StaffReferenceException(httpStatus, errorResponse.getErrorMessage(),
                    errorResponse.getErrorDescription());
        } else {
            throw new StaffReferenceException(httpStatus, LRD_ERROR, LRD_ERROR);
        }

    }

    private void logMessages(String ccdServiceNames,
                             Map<String, String> serviceNameToCodeMapping,
                             List<StaffProfileWithServiceName> staffProfileList) {
        log.info("{}:: Successfully fetched the staff details to refresh role assignment "
                + "for ccd service names {}", loggingComponentName, ccdServiceNames);
        log.info("{}:: Service Codes {} and CCD Service names {} ",
                loggingComponentName,
                serviceNameToCodeMapping.keySet(),
                serviceNameToCodeMapping.values());

        log.info("{}:: No of Records returned in Response {}", loggingComponentName,
                staffProfileList.size());
    }

    private List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> mapCaseWorkerProfileToDto(
            List<CaseWorkerProfile> caseWorkerProfileList) {
        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> caseWorkerProfilesDto =
                new ArrayList<>();
        for (CaseWorkerProfile profile : caseWorkerProfileList) {
            caseWorkerProfilesDto.add(buildCaseWorkerProfileDto(profile));
        }

        return caseWorkerProfilesDto;
    }

    private uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile buildCaseWorkerProfileDto(
            CaseWorkerProfile profile) {
        return uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile.builder()
                .id(profile.getCaseWorkerId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .officialEmail(profile.getEmailId())
                .regionId(profile.getRegionId())
                .regionName(profile.getRegion())
                .userType(profile.getUserType().getDescription())
                .userId(profile.getUserTypeId())
                .suspended(Boolean.TRUE.equals(profile.getSuspended())
                        ? Boolean.TRUE.toString() : Boolean.FALSE.toString())
                .staffAdmin(Boolean.TRUE.equals(profile.getUserAdmin()) ? "Y" : "N")
                .createdTime(profile.getCreatedDate())
                .lastUpdatedTime(profile.getLastUpdate())
                .roles(mapRolesToDto(profile.getCaseWorkerRoles()))
                .skills(mapSkillsToDto(profile.getCaseWorkerSkills()))
                .locations(mapLocationsToDto(profile.getCaseWorkerLocations()))
                .workAreas(mapWorkAreasToDto(profile.getCaseWorkerWorkAreas()))
                .taskSupervisor(Boolean.TRUE.equals(profile.getTaskSupervisor()) ? "Y" : "N")
                .caseAllocator(Boolean.TRUE.equals(profile.getCaseAllocator()) ? "Y" : "N")
                .build();
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

    private List<Skill> mapSkillsToDto(List<CaseWorkerSkill> caseWorkerSkills) {
        List<Skill> skillsDto = new ArrayList<>();
        for (CaseWorkerSkill caseWorkerSkill : caseWorkerSkills) {
            Skill skillDto = Skill.builder()
                    .skillId(caseWorkerSkill.getSkillId())
                    .skillCode(caseWorkerSkill.getSkill().getSkillCode())
                    .description(caseWorkerSkill.getSkill().getDescription()).build();
            skillsDto.add(skillDto);

        }
        return skillsDto;
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

    public CaseWorkerProfile updateUserProfile(CaseWorkersProfileCreationRequest cwrdProfileRequest,
                                               CaseWorkerProfile caseWorkerProfile) {
        caseWorkerProfile.getCaseWorkerLocations().clear();
        caseWorkerProfile.getCaseWorkerWorkAreas().clear();
        caseWorkerProfile.getCaseWorkerRoles().clear();
        //update existing profile with file values
        populateCaseWorkerProfile(cwrdProfileRequest, caseWorkerProfile, caseWorkerProfile.getCaseWorkerId());
        return caseWorkerProfile;
    }

    public boolean updateUserRolesInIdam(CaseWorkersProfileCreationRequest cwrProfileRequest, String idamId) {

        try {
            Response response = userProfileFeignClient.getUserProfileWithRolesById(idamId,null);
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


    // Idam_UP call.
    @SuppressWarnings("unchecked")
    private ResponseEntity<Object> createUserProfileInIdamUP(CaseWorkersProfileCreationRequest cwrdProfileRequest) {
        ResponseEntity<Object> responseEntity;
        Response response = null;
        Object clazz;
        try {
            response = userProfileFeignClient.createUserProfile(createUserProfileRequest(cwrdProfileRequest), SRD);

            clazz = (response.status() == 201 || response.status() == 409)
                    ? UserProfileCreationResponse.class : ErrorResponse.class;

            responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);
            if (clazz == ErrorResponse.class) {
                Object responseBody = responseEntity.getBody();
                if (nonNull(responseBody) && responseBody instanceof ErrorResponse errorResponse) {
                    Optional<ErrorResponse> optional = Optional.ofNullable(errorResponse);
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

    private Optional<Object> getUserProfileUpdateResponse(UserProfileUpdatedData userProfileUpdatedData,
                                                          String userId, String origin) {
        Response response = userProfileFeignClient.modifyUserRoles(userProfileUpdatedData, userId, origin);
        log.info("{}:: UserProfile update roles :: status code {}:: Job Id {}", loggingComponentName,
                response.status(), validationServiceFacade.getAuditJobId());

        ResponseEntity<Object> responseEntity = toResponseEntity(response, UserProfileRolesResponse.class);

        return validateAndGetResponseEntity(responseEntity);
    }

    // creating user profile request
    public UserProfileCreationRequest createUserProfileRequest(CaseWorkersProfileCreationRequest cwrdProfileRequest) {

        Set<String> userRoles = cwrdProfileRequest.getIdamRoles() != null ? cwrdProfileRequest.getIdamRoles() :
                new HashSet<>();
        userRoles.add(ROLE_CWD_USER);
        Set<String> idamRoles = getUserRolesByRoleId(cwrdProfileRequest);
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

    // get the roles that needs to send to idam based on the roleType in the request.
    Set<String> getUserRolesByRoleId(CaseWorkersProfileCreationRequest cwProfileRequest) {

        // get Roles Types
        List<RoleType> roleTypeList = new ArrayList<>();
        cwProfileRequest.getRoles().forEach(role -> roleTypeList.addAll(
                caseWorkerStaticValueRepositoryAccessor
                        .getRoleTypes()
                        .stream()
                        .filter(roleType -> role.getRole().equalsIgnoreCase(roleType.getDescription().trim()))
                        .toList())
        );

        // get work area codes
        List<String> serviceCodes = cwProfileRequest.getWorkerWorkAreaRequests()
                .stream()
                .map(CaseWorkerWorkAreaRequest::getServiceCode)
                .toList();


        // get all assoc records matching role id and service code, finally return idam roles associated
        Set<String> matchedRoles = roleAssocRepository.findByRoleTypeInAndServiceCodeIn(roleTypeList, serviceCodes)
                .stream()
                .map(CaseWorkerIdamRoleAssociation::getIdamRole)
                .collect(Collectors.toSet());
        log.info("{}:: roles matched from assoc :: {}", loggingComponentName, matchedRoles);
        return matchedRoles;
    }

    // get the userTypeId by description.
    public Long getUserTypeIdByDesc(String userTypeReq) {
        Optional<Long> userTypeId = caseWorkerStaticValueRepositoryAccessor
                .getUserTypes()
                .stream().filter(userType ->
                        userType.getDescription().equalsIgnoreCase(userTypeReq.trim()))
                .map(UserType::getUserTypeId).findFirst();
        return userTypeId.orElseThrow(() -> new InvalidRequestException(BAD_REQUEST));
    }


    private Optional<Object> validateAndGetResponseEntity(ResponseEntity<Object> responseEntity) {
        if (nonNull(responseEntity)) {
            return Optional.ofNullable(responseEntity.getBody());
        }
        return Optional.empty();
    }

    private boolean updateMismatchedDatatoUP(CaseWorkersProfileCreationRequest cwrProfileRequest, String idamId,
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
}

