package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.SkillResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.StaffReferenceException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
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
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_USER_TO_SUSPEND_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_ALREADY_CREATED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_CWD_USER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_STAFF_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SRD;

@Service
@Slf4j
@SuppressWarnings("AbbreviationAsWordInName")
public class StaffRefDataServiceImpl implements StaffRefDataService {


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
    private UserProfileFeignClient userProfileFeignClient;

    @Autowired
    private TopicPublisher topicPublisher;

    @Autowired
    IValidationService validationServiceFacade;

    @Autowired
    SkillRepository skillRepository;

    @Autowired
    IJsrValidatorInitializer validateStaffProfile;

    @Autowired
    StaffProfileCreateUpdateUtil staffProfileCreateUpdateUtil;


    @SuppressWarnings("unchecked")
    public StaffProfileCreationResponse processStaffProfileCreation(StaffProfileCreationRequest staffProfileRequest) {

        final CaseWorkerProfile newStaffProfiles;
        final CaseWorkerProfile processedStaffProfiles;
        final StaffProfileCreationResponse response;

        validateStaffProfile.validateStaffProfile(staffProfileRequest);

        checkStaffProfileEmailAndSuspendFlag(staffProfileRequest);
        newStaffProfiles = createCaseWorkerProfile(staffProfileRequest);

        processedStaffProfiles = persistStaffProfile(newStaffProfiles,staffProfileRequest);

        response = StaffProfileCreationResponse.builder()
                    .caseWorkerId(processedStaffProfiles.getCaseWorkerId())
                    .build();

        return response;
    }



    private void checkStaffProfileEmailAndSuspendFlag(StaffProfileCreationRequest profileRequest) {

        // get all existing profile from db (used IN clause)
        CaseWorkerProfile dbCaseWorker = caseWorkerProfileRepo.findByEmailId(profileRequest.getEmailId());

        if (isNotEmpty(dbCaseWorker)) {
            invalidRequestError(profileRequest, PROFILE_ALREADY_CREATED);
        }

        if (profileRequest.isSuspended()) {
            invalidRequestError(profileRequest, NO_USER_TO_SUSPEND_PROFILE);
        }
    }

    private void invalidRequestError(StaffProfileCreationRequest profileRequest, String errorMessage) {

        validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE,NO_USER_TO_SUSPEND_PROFILE,
                null,profileRequest);
        throw new InvalidRequestException(errorMessage);
    }

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

    public ResponseEntity<Object> createUserProfileInIdamUP(StaffProfileCreationRequest staffProfileRequest) {

        ResponseEntity<Object> responseEntity;
        Response response = null;
        Object clazz;
        try {
            response = userProfileFeignClient.createUserProfile(createUserProfileRequest(staffProfileRequest), SRD);

            clazz = (response.status() == 201 || response.status() == 409)
                    ? UserProfileCreationResponse.class : ErrorResponse.class;

            responseEntity = JsonFeignResponseUtil.toResponseEntity(response, clazz);

            if ((response.status() != 409 && isNotEmpty(responseEntity.getBody()))
                    && (responseEntity.getStatusCode().is4xxClientError()
                    || responseEntity.getStatusCode().is5xxServerError())) {

                ErrorResponse error = (ErrorResponse) responseEntity.getBody();

                String errorMessage = error != null ? error.getErrorMessage() : null;
                String errorDescription = error != null ? error.getErrorDescription() : null;

                validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE, errorMessage,
                            null, staffProfileRequest);
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

    // creating user profile request
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
                false);
    }

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

    public CaseWorkerProfile persistStaffProfile(CaseWorkerProfile caseWorkerProfile,
                                                 StaffProfileCreationRequest request) {

        log.info("{}:: persistStaffProfile starts::", loggingComponentName);
        CaseWorkerProfile savedStaffProfiles = null;

        if (isNotEmpty(caseWorkerProfile)) {
            caseWorkerProfile.setNew(true);
            savedStaffProfiles = caseWorkerProfileRepo.save(caseWorkerProfile);

            if (isNotEmpty(savedStaffProfiles)) {
                validationServiceFacade.saveStaffAudit(AuditStatus.SUCCESS, null,
                        savedStaffProfiles.getCaseWorkerId(), request);
                log.info("{}::persistStaffProfile inserted {} ::",
                        loggingComponentName,caseWorkerProfile.getCaseWorkerId());
            } else {
                validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE, null,
                        caseWorkerProfile.getCaseWorkerId(), request);
            }
        }
        return savedStaffProfiles;
    }

    public void publishStaffProfileToTopic(StaffProfileCreationResponse staffProfileCreationResponse) {

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
    public ResponseEntity<List<SearchStaffUserResponse>> retrieveStaffProfile(SearchRequest searchRequest, PageRequest pageRequest) {
        Page<CaseWorkerProfile> pageable =
                caseWorkerProfileRepo.findByCaseWorkerProfiles(searchRequest,pageRequest);
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
        try {
            List<SkillDTO> skillData = new ArrayList<>();
            List<Skill> skills = skillRepository.findAll();
            Optional<List<Skill>> skillsOptional = Optional.ofNullable(skills);
            if (skillsOptional.isPresent()) {
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

        } catch (Exception exp) {
            log.error("{}:: StaffRefDataService getServiceSkills failed :: {}", loggingComponentName,
                    exp);
            throw exp;
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
                                skill -> skill.getServiceId(),
                                skill -> Collections.singletonList(skill),
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

}
