package uk.gov.hmcts.reform.cwrdapi.service.impl;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
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
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
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


    @SuppressWarnings("unchecked")
    public StaffProfileCreationResponse processStaffProfileCreation(StaffProfileCreationRequest staffProfileRequest) {

        final CaseWorkerProfile newStaffProfiles;
        final CaseWorkerProfile processedStaffProfiles;
        final StaffProfileCreationResponse response;

        validateStaffProfile.validateStaffProfile(staffProfileRequest);

        checkStaffProfileEmail(staffProfileRequest);
        newStaffProfiles = createCaseWorkerProfile(staffProfileRequest);

        processedStaffProfiles = persistStaffProfile(newStaffProfiles,staffProfileRequest);


        response = StaffProfileCreationResponse.builder()
                    .caseWorkerId(processedStaffProfiles.getCaseWorkerId())
                    .build();

        return response;
    }



    private void checkStaffProfileEmail(StaffProfileCreationRequest profileRequest) {

        // get all existing profile from db (used IN clause)
        CaseWorkerProfile dbCaseWorker = caseWorkerProfileRepo.findByEmailId(profileRequest.getEmailId());

        if (dbCaseWorker != null) {
            validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE,PROFILE_ALREADY_CREATED,
                    null,profileRequest);
            throw new InvalidRequestException(PROFILE_ALREADY_CREATED);
        }
    }

    public CaseWorkerProfile createCaseWorkerProfile(StaffProfileCreationRequest profileRequest) {
        CaseWorkerProfile finalCaseWorkerProfile = null;
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
                finalCaseWorkerProfile = new CaseWorkerProfile();
                populateStaffProfile(profileRequest,finalCaseWorkerProfile, upResponse.getIdamId());
            }
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
        CaseWorkerProfile processedStaffProfiles = null;

        if (null != caseWorkerProfile) {
            caseWorkerProfile.setNew(true);
            processedStaffProfiles = caseWorkerProfileRepo.save(caseWorkerProfile);

            if (isNotEmpty(processedStaffProfiles)) {
                validationServiceFacade.saveStaffAudit(AuditStatus.SUCCESS, null,
                        processedStaffProfiles.getCaseWorkerId(), request);
                log.info("{}:: {} case worker profiles inserted ::", loggingComponentName, caseWorkerProfile);
            } else {
                validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE, null,
                        caseWorkerProfile.getCaseWorkerId(), request);
            }
        }
        return caseWorkerProfile;
    }

    public void publishStaffProfileToTopic(StaffProfileCreationResponse staffProfileCreationResponse) {

        topicPublisher.sendMessage(List.of(staffProfileCreationResponse.getCaseWorkerId()));
    }



    @Override
    public List<UserType> fetchUserTypes() {
        return userTypeRepository
                .findAll();
    }

    @Override
    public StaffWorkerSkillResponse getServiceSkills() {
        List<Skill> skills = null;
        List<ServiceSkill> serviceSkills = null;
        try {
            List<SkillDTO> skillData = null;
            skills = skillRepository.findAll();

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
        Map<String, List<SkillDTO>> result = null;
        if (skillData != null) {
            result = skillData.stream()
                    .collect(
                            Collectors.toMap(
                                    SkillDTO::getServiceId,
                                    Collections::singletonList,
                                    this::mergeSkillsWithDuplicateServiceIds
                            )
                    );
        }


        List<ServiceSkill> serviceSkills = new ArrayList<>();

        if (result != null) {
            result.forEach(
                    (key, value) -> {
                        ServiceSkill serviceSkill = new ServiceSkill();
                        serviceSkill.setId(key);
                        serviceSkill.setSkills(value);
                        serviceSkills.add(serviceSkill);
                    }
            );
        }

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
}
