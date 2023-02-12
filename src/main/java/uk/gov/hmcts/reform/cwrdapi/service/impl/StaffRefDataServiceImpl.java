package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.Response;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.SkillResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.StaffReferenceException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleName;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
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
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorStaffProfile;
import uk.gov.hmcts.reform.cwrdapi.service.IStaffProfileAuditService;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil;
import uk.gov.hmcts.reform.cwrdapi.util.StaffProfileCreateUpdateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Set.copyOf;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ALREADY_SUSPENDED_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CASE_ALLOCATOR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_NOT_ACTIVE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_SUSPENDED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_USER_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_USER_TO_SUSPEND_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ORIGIN_EXUI;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_ALREADY_CREATED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_NOT_PRESENT_IN_SRD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_NOT_PRESENT_IN_UP_OR_IDAM;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_CWD_USER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_STAFF_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SRD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_CREATE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_UPDATE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STATUS_ACTIVE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TASK_SUPERVISOR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UP_FAILURE_ROLES;
import static uk.gov.hmcts.reform.cwrdapi.util.JsonFeignResponseUtil.toResponseEntity;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.convertToList;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.getAsIntegerList;

/**
 * The type Staff ref data service.
 */
@Service
@Slf4j
@SuppressWarnings("AbbreviationAsWordInName")
public class StaffRefDataServiceImpl implements StaffRefDataService {


    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Value("${crd.publisher.caseWorkerDataPerMessage}")
    private int caseWorkerDataPerMessage;

    @Autowired
    private TopicPublisher topicPublisher;

    @Autowired
    CaseWorkerProfileRepository caseWorkerProfileRepo;

    @Autowired
    RoleTypeRepository roleTypeRepository;

    @Autowired
    UserTypeRepository userTypeRepository;

    @Autowired
    private UserProfileFeignClient userProfileFeignClient;

    @Autowired
    IStaffProfileAuditService staffProfileAuditService;

    @Autowired
    SkillRepository skillRepository;

    @Autowired
    StaffProfileCreateUpdateUtil staffProfileCreateUpdateUtil;

    @Autowired
    IJsrValidatorStaffProfile jsrValidatorStaffProfile;

    @Autowired
    CaseWorkerLocationRepository caseWorkerLocationRepository;

    @Autowired
    CaseWorkerRoleRepository caseWorkerRoleRepository;


    @Autowired
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    @Autowired
    CaseWorkerSkillRepository caseWorkerSkillRepository;
    //Added Extra
    @Autowired
    CaseWorkerStaticValueRepositoryAccessor caseWorkerStaticValueRepositoryAccessor;
    @Autowired
    CaseWorkerIdamRoleAssociationRepository roleAssocRepository;


    @Autowired
    ICwrdCommonRepository cwrCommonRepository;



    @SuppressWarnings("unchecked")
    public StaffProfileCreationResponse processStaffProfileCreation(StaffProfileCreationRequest staffProfileRequest) {

        log.info("{}:: processStaffProfileCreation starts::", loggingComponentName);

        final CaseWorkerProfile newStaffProfiles;
        final CaseWorkerProfile processedStaffProfiles;
        final StaffProfileCreationResponse response;

        jsrValidatorStaffProfile.validateStaffProfile(staffProfileRequest,STAFF_PROFILE_CREATE);

        checkStaffProfileEmailAndSuspendFlag(staffProfileRequest);
        newStaffProfiles = createCaseWorkerProfile(staffProfileRequest);

        processedStaffProfiles = persistStaffProfile(newStaffProfiles,staffProfileRequest);

        response = StaffProfileCreationResponse.builder()
                    .caseWorkerId(processedStaffProfiles.getCaseWorkerId())
                    .build();

        log.info("{}:: processStaffProfileCreation ends::", loggingComponentName);

        return response;
    }



    private void checkStaffProfileEmailAndSuspendFlag(StaffProfileCreationRequest profileRequest) {

        // get all existing profile from db (used IN clause)
        CaseWorkerProfile dbCaseWorker = caseWorkerProfileRepo.findByEmailId(profileRequest.getEmailId().toLowerCase());

        if (isNotEmpty(dbCaseWorker)) {
            invalidRequestError(profileRequest, PROFILE_ALREADY_CREATED);
        }

        if (profileRequest.isSuspended()) {
            invalidRequestError(profileRequest, NO_USER_TO_SUSPEND_PROFILE);
        }
    }

    private void invalidRequestError(StaffProfileCreationRequest profileRequest, String errorMessage) {
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE,errorMessage,
                StringUtil.EMPTY_STRING,profileRequest,STAFF_PROFILE_CREATE);

        throw new InvalidRequestException(errorMessage);
    }

    /**
     * Create case worker profile case worker profile.
     *
     * @param profileRequest the profile request
     * @return the case worker profile
     */
    public CaseWorkerProfile createCaseWorkerProfile(StaffProfileCreationRequest profileRequest) {
        CaseWorkerProfile finalCaseWorkerProfile = null;
        log.info("{}:: createCaseWorkerProfile UserProfile call starts::",
                loggingComponentName);
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(profileRequest);
        log.info("{}:: createCaseWorkerProfile UserProfile Received  response status {}::",
                loggingComponentName,responseEntity.getStatusCode());

        UserProfileCreationResponse upResponse = (UserProfileCreationResponse) (responseEntity.getBody());
        if (nonNull(upResponse)) {
            finalCaseWorkerProfile = new CaseWorkerProfile();
            populateStaffProfile(profileRequest,finalCaseWorkerProfile, upResponse.getIdamId());
        }

        return finalCaseWorkerProfile;
    }

    /**
     * Create user profile in idam up response entity.
     *
     * @param staffProfileRequest the staff profile request
     * @return the response entity
     */
    public ResponseEntity<Object> createUserProfileInIdamUP(StaffProfileCreationRequest staffProfileRequest) {

        ResponseEntity<Object> responseEntity;
        Response response = null;
        Object clazz;
        try {
            response = userProfileFeignClient.createUserProfile(createUserProfileRequest(staffProfileRequest), SRD);

            clazz = (response.status() == 201 || response.status() == 409)
                    ? UserProfileCreationResponse.class : ErrorResponse.class;

            responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);

            if (response.status() == 409) {
                //validate the request info with UserProfile response.
                UserProfileCreationResponse upResponse = (UserProfileCreationResponse) (responseEntity.getBody());
                if (nonNull(upResponse)) {
                    updateUserRolesInIdam(staffProfileRequest, upResponse.getIdamId(),STAFF_PROFILE_CREATE);
                }
            }

            if ((response.status() != 409 && isNotEmpty(responseEntity.getBody()))
                    && (responseEntity.getStatusCode().is4xxClientError()
                    || responseEntity.getStatusCode().is5xxServerError())) {

                ErrorResponse error = (ErrorResponse) responseEntity.getBody();

                String errorMessage = error != null ? error.getErrorMessage() : null;
                String errorDescription = error != null ? error.getErrorDescription() : null;

                staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, errorMessage,
                            null, staffProfileRequest,STAFF_PROFILE_CREATE);

                throw new StaffReferenceException(responseEntity.getStatusCode(), errorMessage,
                        errorDescription);
            }
            return responseEntity;
        } finally {
            if (nonNull(response)) {
                response.close();
            }
        }
    }

    /**
     * Create user profile request user profile creation request.
     *
     * @param profileRequest the profile request
     * @return the user profile creation request
     */
    public UserProfileCreationRequest createUserProfileRequest(StaffProfileCreationRequest profileRequest) {

        Set<String> userRoles = new HashSet<>();
        userRoles.add(ROLE_CWD_USER);

        if (profileRequest.isStaffAdmin()) {
            userRoles.add(ROLE_STAFF_ADMIN);
        }

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
                profileRequest.isResendInvite());
    }

    /**
     * Populate staff profile.
     *
     * @param staffProfileRequest    the staff profile request
     * @param finalCaseWorkerProfile the final case worker profile
     * @param idamId                 the idam id
     */
    public void populateStaffProfile(StaffProfileCreationRequest staffProfileRequest,
                                                  CaseWorkerProfile finalCaseWorkerProfile, String idamId) {
        //case worker profile request mapping

        finalCaseWorkerProfile.setCaseWorkerId(idamId);
        finalCaseWorkerProfile.setFirstName(staffProfileRequest.getFirstName());
        finalCaseWorkerProfile.setLastName(staffProfileRequest.getLastName());
        finalCaseWorkerProfile.setEmailId(staffProfileRequest.getEmailId().toLowerCase());
        finalCaseWorkerProfile.setSuspended(staffProfileRequest.isSuspended());
        finalCaseWorkerProfile.setUserTypeId(staffProfileCreateUpdateUtil.getUserTypeIdByDesc(
                staffProfileRequest.getUserType()));
        finalCaseWorkerProfile.setRegionId(staffProfileRequest.getRegionId());
        finalCaseWorkerProfile.setRegion(staffProfileRequest.getRegion());
        finalCaseWorkerProfile.setCaseAllocator(staffProfileRequest.isCaseAllocator());
        finalCaseWorkerProfile.setTaskSupervisor(staffProfileRequest.isTaskSupervisor());
        finalCaseWorkerProfile.setUserAdmin(staffProfileRequest.isStaffAdmin());
        //Locations data request mapping and setting to case worker profile
        finalCaseWorkerProfile.getCaseWorkerLocations().addAll(
                staffProfileCreateUpdateUtil.mapStaffLocationRequest(idamId, staffProfileRequest));
        //caseWorkerRoles roles request mapping and data setting to case worker profile
        finalCaseWorkerProfile.getCaseWorkerRoles().addAll(
                staffProfileCreateUpdateUtil.mapStaffRoleRequestMapping(idamId, staffProfileRequest));
        //caseWorkerWorkAreas setting to case worker profile
        finalCaseWorkerProfile.getCaseWorkerWorkAreas().addAll(
                staffProfileCreateUpdateUtil.mapStaffAreaOfWork(staffProfileRequest, idamId));
        if (isNotEmpty(staffProfileRequest.getSkills())) {
            finalCaseWorkerProfile.getCaseWorkerSkills().addAll(
                    staffProfileCreateUpdateUtil.mapStaffSkillRequestMapping(idamId, staffProfileRequest));
        }
    }

    /**
     * Persist staff profile case worker profile.
     *
     * @param caseWorkerProfile the case worker profile
     * @param request           the request
     * @return the case worker profile
     */
    public CaseWorkerProfile persistStaffProfile(CaseWorkerProfile caseWorkerProfile,
                                                 StaffProfileCreationRequest request) {

        log.info("{}:: persistStaffProfile starts::", loggingComponentName);
        CaseWorkerProfile savedStaffProfiles = null;

        if (isNotEmpty(caseWorkerProfile)) {
            caseWorkerProfile.setNew(true);
            savedStaffProfiles = caseWorkerProfileRepo.save(caseWorkerProfile);

            if (isNotEmpty(savedStaffProfiles)) {
                staffProfileAuditService.saveStaffAudit(AuditStatus.SUCCESS, StringUtil.EMPTY_STRING,
                        savedStaffProfiles.getCaseWorkerId(), request,STAFF_PROFILE_CREATE);
                log.info("{}::persistStaffProfile inserted {} ::",
                        loggingComponentName,caseWorkerProfile.getCaseWorkerId());
            } else {
                staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                        caseWorkerProfile.getCaseWorkerId(), request,STAFF_PROFILE_CREATE);
            }
        }
        return savedStaffProfiles;
    }

    public void publishStaffProfileToTopic(StaffProfileCreationResponse staffProfileCreationResponse) {

        log.info("{}:: publishStaffProfileToTopic starts::", loggingComponentName);
        topicPublisher.sendMessage(List.of(staffProfileCreationResponse.getCaseWorkerId()));

        log.info("{}:: publishStaffProfileToTopic ends::", loggingComponentName);
    }



    @Override
    public ResponseEntity<List<SearchStaffUserResponse>> retrieveStaffUserByName(String searchString,
                                                                                 PageRequest pageRequest) {

        Page<CaseWorkerProfile> pageable =
                caseWorkerProfileRepo.findByFirstNameOrLastName(searchString.toLowerCase(), pageRequest);
        long totalRecords = pageable.getTotalElements();

        List<CaseWorkerProfile> caseWorkerProfiles = pageable.getContent();

        List<SearchStaffUserResponse> searchResponse = new ArrayList<>();

        if (!caseWorkerProfiles.isEmpty()) {
            searchResponse = mapCaseWorkerProfilesToSearchResponse(caseWorkerProfiles);
        }

        return ResponseEntity
                .status(200)
                .header("total-records", String.valueOf(totalRecords))
                .body(searchResponse);
    }

    @Override
    public ResponseEntity<List<SearchStaffUserResponse>> retrieveStaffProfile(SearchRequest searchRequest,
                                                                              PageRequest pageRequest) {
        List<String> serviceCodes = null;
        List<Integer> locationId = null;

        if (searchRequest.getServiceCode() != null) {
            serviceCodes = convertToList(searchRequest.getServiceCode().toUpperCase());
        }
        if (searchRequest.getLocation() != null) {
            locationId = getAsIntegerList(searchRequest);
        }

        List<String> roles = convertToList(Objects.toString(searchRequest.getRole(), "").toLowerCase());

        Page<CaseWorkerProfile> pageable =
                caseWorkerProfileRepo.findByCaseWorkerProfiles(searchRequest, serviceCodes, locationId,
                        roles.contains(TASK_SUPERVISOR), roles.contains(CASE_ALLOCATOR), roles.contains(STAFF_ADMIN),
                        pageRequest);
        long totalRecords = pageable.getTotalElements();
        List<CaseWorkerProfile> caseWorkerProfiles = pageable.getContent();

        List<SearchStaffUserResponse> searchResponse = new ArrayList<>();

        if (!caseWorkerProfiles.isEmpty()) {
            searchResponse = mapCaseWorkerProfilesToSearchResponse(caseWorkerProfiles);
        }

        return ResponseEntity
                .status(200)
                .header("total-records",String.valueOf(totalRecords))
                .body(searchResponse);
    }

    private List<SearchStaffUserResponse> mapCaseWorkerProfilesToSearchResponse(List<CaseWorkerProfile>
                                                                                        caseWorkerProfiles) {
        List<SearchStaffUserResponse> searchStaffUserResponse = new ArrayList<>();
        caseWorkerProfiles.forEach(caseWorkerProfile -> {
            SearchStaffUserResponse searchStaffUserResponseValue =
                    SearchStaffUserResponse.builder()
                            .firstName(caseWorkerProfile.getFirstName())
                            .lastName(caseWorkerProfile.getLastName())
                            .emailId(caseWorkerProfile.getEmailId())
                            .caseWorkerId(caseWorkerProfile.getCaseWorkerId())
                            .services(mapServicesToDto(caseWorkerProfile.getCaseWorkerWorkAreas()))
                            .region(caseWorkerProfile.getRegion())
                            .regionId(caseWorkerProfile.getRegionId())
                            .roles(mapRolesToDto(caseWorkerProfile.getCaseWorkerRoles()))
                            .baseLocations(mapBaseLocationsToDto(caseWorkerProfile.getCaseWorkerLocations()))
                            .userType(caseWorkerProfile.getUserType().getDescription())
                            .skills(mapSkillsToDto(caseWorkerProfile.getCaseWorkerSkills()))
                            .build();

            if (caseWorkerProfile.getTaskSupervisor() != null) {
                searchStaffUserResponseValue.setTaskSupervisor(caseWorkerProfile.getTaskSupervisor());
            }
            if (caseWorkerProfile.getCaseAllocator() != null) {
                searchStaffUserResponseValue.setCaseAllocator(caseWorkerProfile.getCaseAllocator());
            }
            if (caseWorkerProfile.getSuspended() != null) {
                searchStaffUserResponseValue.setSuspended(caseWorkerProfile.getSuspended());
            }
            if (caseWorkerProfile.getUserAdmin() != null) {
                searchStaffUserResponseValue.setStaffAdmin(caseWorkerProfile.getUserAdmin());
            }
            searchStaffUserResponse.add(
                    searchStaffUserResponseValue
            );
        });

        return searchStaffUserResponse;
    }

    private List<Role> mapRolesToDto(List<CaseWorkerRole> caseWorkerRoles) {
        List<Role> rolesDto = new ArrayList<>();
        for (CaseWorkerRole caseWorkerRole : caseWorkerRoles) {
            var roleDto = Role.builder()
                    .createdTime(caseWorkerRole.getCreatedDate())
                    .lastUpdatedTime(caseWorkerRole.getLastUpdate())
                    .roleId(caseWorkerRole.getRoleId().toString())
                    .roleName(caseWorkerRole.getRoleType().getDescription())
                    .isPrimary(caseWorkerRole.getPrimaryFlag())
                    .build();

            rolesDto.add(roleDto);
        }
        return rolesDto;
    }

    private List<Location> mapBaseLocationsToDto(List<CaseWorkerLocation> caseWorkerLocations) {
        List<Location> locations = new ArrayList<>();
        for (CaseWorkerLocation caseWorkerLocation : caseWorkerLocations) {
            var location = Location.builder()
                    .createdTime(caseWorkerLocation.getCreatedDate())
                    .lastUpdatedTime(caseWorkerLocation.getLastUpdate())
                    .baseLocationId(caseWorkerLocation.getLocationId())
                    .locationName(caseWorkerLocation.getLocation())
                    .isPrimary(caseWorkerLocation.getPrimaryFlag())
                    .build();

            locations.add(location);
        }
        return locations;
    }

    private List<ServiceResponse> mapServicesToDto(List<CaseWorkerWorkArea> caseWorkerWorkAreas) {
        List<ServiceResponse> serviceResponses = new ArrayList<>();
        for (CaseWorkerWorkArea caseWorkerWorkArea : caseWorkerWorkAreas) {
            var service = ServiceResponse.builder()
                    .service(caseWorkerWorkArea.getAreaOfWork())
                    .serviceCode(caseWorkerWorkArea.getServiceCode())
                    .build();

            serviceResponses.add(service);
        }
        return serviceResponses;
    }

    private List<SkillResponse> mapSkillsToDto(List<CaseWorkerSkill> caseWorkerSkills) {

        List<SkillResponse> skills = new ArrayList<>();
        caseWorkerSkills.forEach(caseWorkerSkill -> {
            Skill skill = caseWorkerSkill.getSkill();
            var skillResponse = SkillResponse.builder()
                    .skillId(skill.getSkillId())
                    .description(skill.getDescription())
                    .build();
            skills.add(skillResponse);

        });

        return skills;
    }


    @Override
    public StaffWorkerSkillResponse getServiceSkills() {
        List<ServiceSkill> serviceSkills = new ArrayList<>();
        List<SkillDTO> skillData = null;
        List<Skill> skills = skillRepository.findAll();
        if (!ObjectUtils.isEmpty(skills)) {
            skillData = skills.stream().map(skill -> {
                SkillDTO skillDTO = new SkillDTO();
                skillDTO.setServiceId(skill.getServiceId());
                skillDTO.setSkillId(skill.getSkillId());
                skillDTO.setSkillCode(skill.getSkillCode());
                skillDTO.setUserType(skill.getUserType());
                skillDTO.setDescription(skill.getDescription());
                return skillDTO;
            }).toList();

            serviceSkills = mapSkillToServicesSkill(skillData);
        }


        StaffWorkerSkillResponse staffWorkerSkillResponse = new StaffWorkerSkillResponse();
        staffWorkerSkillResponse.setServiceSkills(serviceSkills);
        return staffWorkerSkillResponse;
    }

    /**
     * To convert skills data to ServiceSkills.
     * @param skillData List of skills
     * @return List of ServiceSkill
     */
    public List<ServiceSkill> mapSkillToServicesSkill(List<SkillDTO> skillData) {

        Map<String, List<SkillDTO>> result = skillData.stream()
                .collect(
                        Collectors.toMap(
                                SkillDTO::getServiceId,
                                Collections::singletonList,
                                this::mergeSkillsWithDuplicateServiceIds
                        )
                );


        List<ServiceSkill> serviceSkills = new ArrayList<>();
        result.forEach(
                (key, value) -> {
                    ServiceSkill serviceSkill = new ServiceSkill();
                    serviceSkill.setId(key);
                    serviceSkill.setSkills(value);
                    serviceSkills.add(serviceSkill);
                }
        );
        return serviceSkills;

    }

    private List<SkillDTO> mergeSkillsWithDuplicateServiceIds(List<SkillDTO> existingResults,
                                                              List<SkillDTO> newResults) {
        List<SkillDTO> mergedResults = new ArrayList<>();
        mergedResults.addAll(existingResults);
        mergedResults.addAll(newResults);
        return mergedResults;
    }

    @Override
    public List<RoleType> getJobTitles() {
        return roleTypeRepository.findAll();
    }

    @Override
    public List<UserType> fetchUserTypes() {
        return userTypeRepository
                .findAll();
    }



    @Override
    @SuppressWarnings("unchecked")
    public StaffProfileCreationResponse updateStaffProfile(StaffProfileCreationRequest profileRequest) {

        log.info("{}:: processStaffProfileUpdation starts::",
                loggingComponentName);

        jsrValidatorStaffProfile.validateStaffProfile(profileRequest,STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfileToUpdate = validateStaffProfileForUpdate(profileRequest);


        CaseWorkerProfile caseWorkerProfile = updateStaffProfiles(profileRequest, caseWorkerProfileToUpdate);
        StaffProfileCreationResponse response = null;

        if (null != caseWorkerProfile) {

            staffProfileAuditService.saveStaffAudit(AuditStatus.SUCCESS, StringUtils.EMPTY,
                    caseWorkerProfile.getCaseWorkerId(), profileRequest, STAFF_PROFILE_UPDATE);

            response = StaffProfileCreationResponse.builder()
                    .caseWorkerId(caseWorkerProfile.getCaseWorkerId())
                    .build();
        }
        return response;
    }

    @Override
    public void reinviteStaffProfile(StaffProfileCreationRequest profileRequest) {

        CaseWorkerProfile caseWorkerProfile = caseWorkerProfileRepo
                .findByEmailId(profileRequest.getEmailId().toLowerCase());
        //if caseworker profile does not have the input emailid throw error
        if (caseWorkerProfile == null) {
            staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, PROFILE_NOT_PRESENT_IN_SRD,
                    StringUtils.EMPTY, profileRequest, STAFF_PROFILE_UPDATE);
            throw new StaffReferenceException(HttpStatus.NOT_FOUND, PROFILE_NOT_PRESENT_IN_SRD,
                    PROFILE_NOT_PRESENT_IN_SRD);
        }
        ResponseEntity<Object> responseEntity = createUserProfileInIdamUP(profileRequest);
        UserProfileCreationResponse upResponse = (UserProfileCreationResponse) (responseEntity.getBody());

        // update idamid in case its different in idam
        if (upResponse != null && !upResponse.getIdamId().equals(caseWorkerProfile.getCaseWorkerId())) {
            caseWorkerProfileRepo.delete(caseWorkerProfile);
            cwrCommonRepository.flush();
            caseWorkerProfile.setCaseWorkerId(upResponse.getIdamId());
            caseWorkerProfile.getCaseWorkerLocations().forEach(e -> e.setCaseWorkerId(upResponse.getIdamId()));
            caseWorkerProfile.getCaseWorkerRoles().forEach(e -> e.setCaseWorkerId(upResponse.getIdamId()));
            caseWorkerProfile.getCaseWorkerWorkAreas().forEach(e -> e.setCaseWorkerId(upResponse.getIdamId()));
            caseWorkerProfile.getCaseWorkerSkills().forEach(e -> e.setCaseWorkerId(upResponse.getIdamId()));
            caseWorkerProfileRepo.save(caseWorkerProfile);
        }
    }


    private CaseWorkerProfile validateStaffProfileForUpdate(StaffProfileCreationRequest profileRequest) {

        // get all existing profile from db (used IN clause)
        CaseWorkerProfile caseWorkerProfile = caseWorkerProfileRepo
                .findByEmailId(profileRequest.getEmailId().toLowerCase());
        if (caseWorkerProfile == null) {
            staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE,PROFILE_NOT_PRESENT_IN_SRD,
                    StringUtils.EMPTY,profileRequest,STAFF_PROFILE_UPDATE);
            throw new StaffReferenceException(HttpStatus.NOT_FOUND,PROFILE_NOT_PRESENT_IN_SRD,
                    PROFILE_NOT_PRESENT_IN_SRD);
        }

        UserProfileResponse userProfileResponse = getUserProfileFromUP(caseWorkerProfile.getCaseWorkerId());

        if (nonNull(userProfileResponse) && isNotTrue(STATUS_ACTIVE.equals(userProfileResponse.getIdamStatus()))) {

            throw new StaffReferenceException(HttpStatus.BAD_REQUEST, IDAM_STATUS_NOT_ACTIVE,
                    IDAM_STATUS_NOT_ACTIVE);
        } else if (userProfileResponse == null) {
            staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, PROFILE_NOT_PRESENT_IN_UP_OR_IDAM,
                    StringUtils.EMPTY, profileRequest, STAFF_PROFILE_UPDATE);
            throw new StaffReferenceException(HttpStatus.NOT_FOUND, PROFILE_NOT_PRESENT_IN_UP_OR_IDAM,
                    PROFILE_NOT_PRESENT_IN_UP_OR_IDAM);
        }

        if (isTrue(caseWorkerProfile.getSuspended())) {
            //when existing profile with delete flag is true then log exception add entry in exception table
            staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, ALREADY_SUSPENDED_ERROR_MESSAGE,
                    caseWorkerProfile.getCaseWorkerId(), profileRequest, STAFF_PROFILE_UPDATE);
            throw new StaffReferenceException(HttpStatus.BAD_REQUEST, ALREADY_SUSPENDED_ERROR_MESSAGE,
                    ALREADY_SUSPENDED_ERROR_MESSAGE);
        }
        return caseWorkerProfile;

    }

    public UserProfileResponse getUserProfileFromUP(String idamId) {
        Response response = userProfileFeignClient.getUserProfileWithRolesById(idamId);
        ResponseEntity<Object> responseEntity = toResponseEntity(response, UserProfileResponse.class);


        Optional<Object> resultResponse = validateAndGetResponseEntity(responseEntity);
        if (resultResponse.isPresent() && resultResponse.get() instanceof UserProfileResponse profileResponse) {
            if (nonNull(profileResponse.getIdamId())) {
                return profileResponse;
            }
        }
        return null;
    }



    public CaseWorkerProfile updateStaffProfiles(StaffProfileCreationRequest cwUiRequest,
                                                       CaseWorkerProfile caseWorkerProfile) {

        CaseWorkerProfile processedCwProfile;

        try {

            //process update and suspend CW profiles
            CaseWorkerProfile cwProfileToPersist = null;
            cwProfileToPersist = processExistingCaseWorkers(
                    cwUiRequest, caseWorkerProfile);
            // persist in db


            processedCwProfile = persistCaseWorker(cwProfileToPersist,
                    cwUiRequest);
        } catch (Exception exp) {
            log.error("{}:: createCaseWorkerUserProfiles failed  ::{}", loggingComponentName,
                    exp);
            throw exp;
        }
        return processedCwProfile;
    }

    public CaseWorkerProfile processExistingCaseWorkers(
            StaffProfileCreationRequest cwUiRequest, CaseWorkerProfile caseWorkerProfiles) {

        CaseWorkerProfile filteredProfile = null;

        if (cwUiRequest.isSuspended()) {
            //when existing profile with delete flag is true in request then suspend user
            if (isUserSuspended(UserProfileUpdatedData.builder().idamStatus(IDAM_STATUS_SUSPENDED).build(),
                    caseWorkerProfiles.getCaseWorkerId(), ORIGIN_EXUI)) {
                caseWorkerProfiles.setSuspended(true);
                filteredProfile = caseWorkerProfiles;
                return filteredProfile;
            }
        } else {
            //when existing profile with delete flag is false then update user in CRD db and roles in SIDAM
            filteredProfile = updateSidamRoles(caseWorkerProfiles, cwUiRequest);
            return filteredProfile;
        }
        //add user roles in user profile and filter out UP failed records

        return null;
    }


    public CaseWorkerProfile persistCaseWorker(
            CaseWorkerProfile updateCaseWorkerProfile,
            StaffProfileCreationRequest cwUiRequest) {
        CaseWorkerProfile processedCwProfiles = null;
        CaseWorkerProfile profilesToBePersisted =
                deleteChildrenAndUpdateCwProfiles(updateCaseWorkerProfile, cwUiRequest);


        if (isNotEmpty(profilesToBePersisted)) {
            processedCwProfiles = caseWorkerProfileRepo.save(profilesToBePersisted);
            log.info("{}::case worker profile inserted ", loggingComponentName);
        }

        return processedCwProfiles;
    }

    // deletes children and updates caseworker profile
    private CaseWorkerProfile deleteChildrenAndUpdateCwProfiles(CaseWorkerProfile updateCaseWorkerProfiles,
                                                                      StaffProfileCreationRequest cwUiRequest) {
        CaseWorkerProfile updatedProfiles = null;
        if (isNotEmpty(updateCaseWorkerProfiles)) {
            caseWorkerLocationRepository.deleteByCaseWorkerProfile(updateCaseWorkerProfiles);
            caseWorkerWorkAreaRepository.deleteByCaseWorkerProfile(updateCaseWorkerProfiles);
            caseWorkerRoleRepository.deleteByCaseWorkerProfile(updateCaseWorkerProfiles);
            caseWorkerSkillRepository.deleteByCaseWorkerProfile(updateCaseWorkerProfiles);
            cwrCommonRepository.flush();
            updatedProfiles = updateUserProfile(cwUiRequest,
                    updateCaseWorkerProfiles);
        }
        return updatedProfiles;
    }

    public CaseWorkerProfile updateUserProfile(StaffProfileCreationRequest cwrdProfileRequest,
                                               CaseWorkerProfile caseWorkerProfile) {
        //update existing profile with file StaffProfileCreationRequest
        populateStaffProfile(cwrdProfileRequest, caseWorkerProfile, caseWorkerProfile.getCaseWorkerId());
        return caseWorkerProfile;
    }


    public boolean isUserSuspended(UserProfileUpdatedData userProfileUpdatedData, String userId,
                                   String origin) {

        boolean status = true;
        try {
            Optional<Object> resultResponse = getUserProfileUpdateResponse(userProfileUpdatedData, userId, origin);

            if (!resultResponse.isPresent()
                    || (isNull(((UserProfileRolesResponse) resultResponse.get())
                    .getAttributeResponse()))
                    || (!(((UserProfileRolesResponse) resultResponse.get())
                    .getAttributeResponse().getIdamStatusCode().equals(HttpStatus.OK.value())))) {
                log.info("{}:: {} case worker profiles isUserSuspended ", loggingComponentName,
                        UP_FAILURE_ROLES);
                status = false;
            }

        } catch (Exception ex) {
            log.error("{}:: UserProfile modify api failed with error :: {}::  {}",
                    loggingComponentName, ex.getMessage(), UP_FAILURE_ROLES);
            status = false;
        }
        return status;
    }

    private Optional<Object> getUserProfileUpdateResponse(UserProfileUpdatedData userProfileUpdatedData,
                                                          String userId, String origin) {
        Response response = userProfileFeignClient.modifyUserRoles(userProfileUpdatedData, userId, origin);
        log.info("{}:: UserProfile update roles :: status code {}", loggingComponentName,
                response.status());

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
    public CaseWorkerProfile updateSidamRoles(CaseWorkerProfile updateCaseWorkerProfiles,
                                                    StaffProfileCreationRequest cwUiRequest) {
        CaseWorkerProfile filteredUpdateCwProfile = null;
        boolean isAddRoleSuccess = updateUserRolesInIdam(cwUiRequest,
                updateCaseWorkerProfiles.getCaseWorkerId(),STAFF_PROFILE_UPDATE);
        if (isAddRoleSuccess) {
            filteredUpdateCwProfile = updateCaseWorkerProfiles;
        }
        return filteredUpdateCwProfile;
    }

    public boolean updateUserRolesInIdam(StaffProfileCreationRequest cwrProfileRequest, String idamId,
                                         String operationType) {


        Response response = userProfileFeignClient.getUserProfileWithRolesById(idamId);
        ResponseEntity<Object> responseEntity = toResponseEntity(response, UserProfileResponse.class);

        Optional<Object> resultResponse = validateAndGetResponseEntity(responseEntity);
        if (resultResponse.isPresent() && resultResponse.get() instanceof UserProfileResponse profileResponse
                && nonNull(profileResponse.getIdamStatus())) {
            if (isNotTrue(profileResponse.getIdamStatus().equals(STATUS_ACTIVE))) {

                staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, IDAM_STATUS_NOT_ACTIVE,
                        profileResponse.getIdamId(), cwrProfileRequest, operationType);

                log.error("{}:: updateUserRolesInIdam :: status code {}", loggingComponentName,
                        profileResponse.getIdamStatus());

                throw new StaffReferenceException(HttpStatus.BAD_REQUEST, IDAM_STATUS_NOT_ACTIVE,
                        IDAM_STATUS_NOT_ACTIVE);
            }

        } else {
            log.error("{}:: updateUserRolesInIdam :: status code {}", loggingComponentName,
                    UP_FAILURE_ROLES);
            throw new StaffReferenceException(HttpStatus.BAD_REQUEST, IDAM_STATUS_USER_PROFILE,
                    IDAM_STATUS_USER_PROFILE);

        }
        Set<String> mappedRoles = getUserRolesByRoleId(cwrProfileRequest);


        Set<String> idamRolesCwr = new HashSet<>();


        if (cwrProfileRequest.isStaffAdmin()) {
            idamRolesCwr.add(ROLE_CWD_USER);
            idamRolesCwr.add(ROLE_STAFF_ADMIN);
        }

        idamRolesCwr.addAll(mappedRoles);
        Set<RoleName> mergedRoles = new HashSet<>();

        UserProfileResponse userProfileResponse = (UserProfileResponse) requireNonNull(responseEntity.getBody());

        Set<String> userProfileRoles = copyOf(userProfileResponse.getRoles());
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
        return isEachRoleUpdated(builder.build(), idamId, "EXUI");
    }

    public boolean isEachRoleUpdated(UserProfileUpdatedData userProfileUpdatedData, String userId,
                                     String origin) {
        boolean isEachRoleUpdated;
        try {
            Optional<Object> resultResponse = getUserProfileUpdateResponse(userProfileUpdatedData, userId, origin);

            if (resultResponse.isPresent() && resultResponse.get() instanceof UserProfileRolesResponse
                    userProfileRolesResponse) {
                if (nonNull(userProfileRolesResponse.getRoleAdditionResponse())
                        || nonNull(userProfileRolesResponse.getAttributeResponse())) {
                    isEachRoleUpdated = isRecordupdatedinUP(userProfileRolesResponse);

                } else {
                    log.info("{}:: isEachRoleUpdated  failed:: message {}", loggingComponentName,
                            UP_FAILURE_ROLES);
                    isEachRoleUpdated = false;
                }
            } else {
                log.info("{}:: isEachRoleUpdated  failed:: message {}", loggingComponentName,
                        UP_FAILURE_ROLES);
                isEachRoleUpdated = false;
            }

        } catch (Exception ex) {
            log.error("{}:: UserProfile modify api failed with error :: {}::  {}",
                    loggingComponentName, ex.getMessage(), UP_FAILURE_ROLES);

            throw new StaffReferenceException(HttpStatus.BAD_REQUEST, StringUtils.EMPTY,
                    UP_FAILURE_ROLES);
        }
        return isEachRoleUpdated;
    }

    private boolean isRecordupdatedinUP(UserProfileRolesResponse userProfileRolesResponse) {

        boolean isRecordUpdate = true;
        if (nonNull(userProfileRolesResponse.getRoleAdditionResponse())
                && isNotTrue(userProfileRolesResponse.getRoleAdditionResponse().getIdamStatusCode()
                .equals(valueOf(HttpStatus.CREATED.value())))) {

            log.info("{}:: isRecordupdatedinUP  failed:: message {}", loggingComponentName,
                    userProfileRolesResponse.getRoleAdditionResponse().getIdamMessage());
            isRecordUpdate = false;
        }
        if (nonNull(userProfileRolesResponse.getAttributeResponse())
                && !(userProfileRolesResponse.getAttributeResponse().getIdamStatusCode()
                .equals(Integer.valueOf(HttpStatus.OK.value())))) {

            log.info("{}:: isRecordupdatedinUP  failed:: message {}", loggingComponentName,
                    userProfileRolesResponse.getRoleAdditionResponse().getIdamMessage());
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
