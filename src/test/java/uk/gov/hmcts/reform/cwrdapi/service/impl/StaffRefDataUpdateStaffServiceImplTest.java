package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerSkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.service.ICwrdCommonRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorStaffProfile;
import uk.gov.hmcts.reform.cwrdapi.service.IStaffProfileAuditService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.StaffProfileCreateUpdateUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_SUSPENDED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ORIGIN_EXUI;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_NOT_PRESENT_IN_DB;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_CREATE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_UPDATE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STATUS_ACTIVE;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"AbbreviationAsWordInName", "unchecked"})
public class StaffRefDataUpdateStaffServiceImplTest {
    @Mock
    private SkillRepository skillRepository;


    @InjectMocks
    private StaffRefDataServiceImpl staffRefDataServiceImpl;
    @Mock
    private CaseWorkerProfileRepository caseWorkerProfileRepository;
    @Mock
    CaseWorkerLocationRepository caseWorkerLocationRepository;
    @Mock
    CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;
    @Mock
    CaseWorkerRoleRepository caseWorkerRoleRepository;
    @Mock
    CaseWorkerIdamRoleAssociationRepository caseWorkerIdamRoleAssociationRepository;
    @Mock
    private UserProfileFeignClient userProfileFeignClient;
    @Mock
    private TopicPublisher topicPublisher;
    @Mock
    private CaseWorkerStaticValueRepositoryAccessorImpl caseWorkerStaticValueRepositoryAccessorImpl;
    @Mock
    IStaffProfileAuditService staffProfileAuditService;
    @Mock
    IJsrValidatorStaffProfile jsrValidatorStaffProfile;
    @Mock
    private StaffProfileCreateUpdateUtil staffProfileCreateUpdateUtil;
    @Mock
    StaffAuditRepository staffAuditRepository;

    private StaffProfileCreationRequest staffProfileCreationRequest;
    private StaffProfileCreationResponse staffProfileCreationRespone = new StaffProfileCreationResponse();
    private RoleType roleType;
    private UserType userType;
    private Skill skill;
    private CaseWorkerProfile caseWorkerProfile;

    ObjectMapper mapper = new ObjectMapper();

    @Mock
    CaseWorkerSkillRepository caseWorkerSkillRepository;

    @Mock
    ICwrdCommonRepository cwrCommonRepository;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<String> idamRoles = new HashSet<>();
        idamRoles.add("IdamRole1");
        idamRoles.add("IdamRole2");

        StaffProfileRoleRequest staffProfileRoleRequest =
                new StaffProfileRoleRequest(1,"testRole1", true);

        CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("testLocation")
                .locationId(1)
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA4")
                .service("service")
                .build();

        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId(1)
                .description("training")
                .build();


        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("test@test.com")
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .services(singletonList(caseWorkerServicesRequest))
                .baseLocations(singletonList(caseWorkerLocationRequest))
                .roles(singletonList(staffProfileRoleRequest))
                .skills(singletonList(skillsRequest))
                .build();

        Role caseWorkerRole = new Role();
        caseWorkerRole.setRoleId("id");
        caseWorkerRole.setRoleName("role name");
        caseWorkerRole.setPrimary(true);

        caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWR-func-test-user@test.com");

        roleType = new RoleType();
        roleType.setRoleId(1L);
        roleType.setDescription("testRole1");

        userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("testUser1");

        skill = new Skill();
        skill.setSkillId(1L);
        skill.setDescription("training");
        MockitoAnnotations.openMocks(this);


        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("test@test.com")
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .services(singletonList(caseWorkerServicesRequest))
                .baseLocations(singletonList(caseWorkerLocationRequest))
                .roles(singletonList(staffProfileRoleRequest))
                .skills(singletonList(skillsRequest))
                .build();



        caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWR-func-test-user@test.com");

        roleType = new RoleType();
        roleType.setRoleId(1L);
        roleType.setDescription("testRole1");

        userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("testUser1");

        skill = new Skill();
        skill.setSkillId(1L);
        skill.setDescription("training");

    }



    CaseWorkerProfile buildCaseWorkerProfile() {

        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setCaseWorkerRoleId(1L);
        caseWorkerRole.setCaseWorkerId("CWID1");
        caseWorkerRole.setRoleId(1L);
        caseWorkerRole.setPrimaryFlag(false);
        caseWorkerRole.setCreatedDate(LocalDateTime.now());
        caseWorkerRole.setLastUpdate(LocalDateTime.now());

        RoleType roleType = new RoleType();
        roleType.setRoleId(1L);
        roleType.setDescription("testRole1");

        caseWorkerRole.setRoleType(roleType);

        CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setCaseWorkerId("CWID1");
        caseWorkerLocation.setCaseWorkerLocationId(11111L);
        caseWorkerLocation.setCreatedDate(LocalDateTime.now());
        caseWorkerLocation.setLastUpdate(LocalDateTime.now());
        caseWorkerLocation.setLocationId(11112);
        caseWorkerLocation.setLocation("test location");
        caseWorkerLocation.setPrimaryFlag(true);

        UserType userType = new UserType();
        userType.setDescription("userTypeId");

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setFirstName("firstName");
        caseWorkerProfile.setLastName("Last`name");
        caseWorkerProfile.setEmailId("a@b.com");
        caseWorkerProfile.setRegion("region");
        caseWorkerProfile.setRegionId(111122222);
        caseWorkerProfile.setUserTypeId(112L);
        caseWorkerProfile.setUserType(userType);
        caseWorkerProfile.setSuspended(true);
        caseWorkerProfile.setTaskSupervisor(true);
        caseWorkerProfile.setCaseAllocator(false);
        caseWorkerProfile.setUserAdmin(true);
        caseWorkerProfile.setCreatedDate(LocalDateTime.now());
        caseWorkerProfile.setLastUpdate(LocalDateTime.now());

        caseWorkerProfile.setCaseWorkerId("27fbd198-552e-4c32-9caf-37be1545caaf");
        caseWorkerProfile.setCaseWorkerRoles(singletonList(caseWorkerRole));
        caseWorkerProfile.setCaseWorkerLocations(singletonList(caseWorkerLocation));
        CaseWorkerSkill caseWorkerSkill = getCaseWorkerSkill();

        caseWorkerProfile.setCaseWorkerSkills(singletonList(caseWorkerSkill));

        CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea();
        caseWorkerWorkArea.setCaseWorkerWorkAreaId(1L);
        caseWorkerWorkArea.setCaseWorkerId("CWID1");
        caseWorkerWorkArea.setAreaOfWork("TestArea");
        caseWorkerWorkArea.setServiceCode("SvcCode1");
        caseWorkerWorkArea.setCreatedDate(LocalDateTime.now());
        caseWorkerWorkArea.setLastUpdate(LocalDateTime.now());

        caseWorkerProfile.setCaseWorkerWorkAreas(singletonList(caseWorkerWorkArea));
        return caseWorkerProfile;
    }



    private List<Skill> getSkillsData() {
        Skill skill1 = new Skill();
        skill1.setServiceId("BBA3");
        skill1.setSkillId(1L);
        skill1.setSkillCode("A1");
        skill1.setDescription("desc1");
        skill1.setUserType("user_type1");

        Skill skill2 = new Skill();
        skill2.setServiceId("BBA3");

        skill2.setSkillId(1L);

        skill2.setSkillId(3L);

        skill2.setSkillCode("A3");
        skill2.setDescription("desc3");
        skill2.setUserType("user_type3");


        Skill skill3 = new Skill();
        skill3.setServiceId("ABA1");
        skill3.setSkillId(2L);
        skill3.setSkillCode("A2");
        skill3.setDescription("desc2");
        skill3.setUserType("user_type2");

        Skill skill4 = new Skill();
        skill4.setServiceId("ABA1");
        skill4.setSkillId(4L);
        skill4.setSkillCode("A4");
        skill4.setDescription("desc4");
        skill4.setUserType("user_type4");

        List<Skill> skills = List.of(skill1, skill2, skill3, skill4);

        return skills;
    }

    private Skill getSkillData() {
        Skill skill = new Skill();
        skill.setServiceId("BBA3");
        skill.setSkillId(1L);
        skill.setSkillCode("A1");
        skill.setDescription("desc1");
        skill.setUserType("user_type1");

        return skill;
    }


    @Test
    void test_saveStaffProfileValidationAudit() {

        staffProfileAuditService.saveStaffAudit(AuditStatus.SUCCESS,null,
                caseWorkerProfile.getCaseWorkerId(),staffProfileCreationRequest,STAFF_PROFILE_CREATE);
        verify(staffAuditRepository, times(0)).save(any());
    }

    @Test
    void test_saveStaffProfileAlreadyPresent() {
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_CREATE);
        InvalidRequestException thrown = Assertions.assertThrows(InvalidRequestException.class, () -> {
            staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        });

        assertThat(thrown.getMessage()).contains("The profile is already created for the given email Id");
    }

    @Test
    void test_newStaffProfileSuspended() {
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_CREATE);
        staffProfileCreationRequest.setSuspended(true);
        InvalidRequestException thrown = Assertions.assertThrows(InvalidRequestException.class, () -> {
            staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        });
        String errorMsg = "There is no user present to suspend. "
                + "Please try again or check with HMCTS Support Team";
        assertThat(thrown.getMessage()).contains(errorMsg);
    }




    @Test
    void test_update_staff_profile_with_changed_values() throws JsonProcessingException {

        //ValidateStaffProfile
        //doNothing().when(validateStaffProfile).validateStaffProfile(any());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);

        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);
        when(caseWorkerProfileRepository.findByEmailIdIn(anySet()))
                .thenReturn(caseWorkerProfiles);
        when(caseWorkerProfileRepository.saveAll(anyList())).thenReturn(caseWorkerProfiles);


        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);

        userProfileResponse.setRoles(roles);
        userProfileResponse.setFirstName("testFNChanged");
        userProfileResponse.setLastName("testLNChanged");

        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(200).build());

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("12345678");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        roleAdditionResponse.setIdamMessage("success");

        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileRolesResponse),
                                defaultCharset())
                        .status(200).build());

        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();

        StaffProfileCreationResponse staffProfileCreationResponse  = staffRefDataServiceImpl
                .updateStaffProfile(staffProfileCreationRequest);


        assertThat(staffProfileCreationResponse).isNotNull();
        assertThat(staffProfileCreationResponse.getCaseWorkerId()).isEqualTo("CWID1");
    }

    @Test
    void test_isUserSuspended_should_return_false() throws JsonProcessingException {
        CaseWorkerProfile dbProfile = new CaseWorkerProfile();
        dbProfile.setCaseWorkerId("CWID1");
        dbProfile.setFirstName("CWFirstName");
        dbProfile.setLastName("CWLastName");
        dbProfile.setEmailId("cwr-func-test-user@test.com");

        StaffProfileCreationRequest cwUiRequest = getStaffProfileUpdateRequest();

        boolean suspended = staffRefDataServiceImpl.isUserSuspended(UserProfileUpdatedData.builder().idamStatus(IDAM_STATUS_SUSPENDED).build(),
                dbProfile.getCaseWorkerId(), ORIGIN_EXUI, cwUiRequest.getRowId());
        assertThat(suspended).isFalse();

    }

    @Test
    void test_isUserSuspended_should_return_false_with_roles_data() throws JsonProcessingException {
        CaseWorkerProfile dbProfile = new CaseWorkerProfile();
        dbProfile.setCaseWorkerId("CWID1");
        dbProfile.setFirstName("CWFirstName");
        dbProfile.setLastName("CWLastName");
        dbProfile.setEmailId("cwr-func-test-user@test.com");

        StaffProfileCreationRequest cwUiRequest = getStaffProfileUpdateRequest();

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
       // userProfileCreationResponse.setIdamId("12345678");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        roleAdditionResponse.setIdamMessage("success");

        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileRolesResponse),
                                defaultCharset())
                        .status(200).build());

        boolean suspended = staffRefDataServiceImpl.isUserSuspended(UserProfileUpdatedData.builder().idamStatus(IDAM_STATUS_SUSPENDED).build(),
                dbProfile.getCaseWorkerId(), ORIGIN_EXUI, cwUiRequest.getRowId());
        assertThat(suspended).isFalse();

    }

    @Test
    void test_updateUserRolesInIdam() throws JsonProcessingException {
        CaseWorkerProfile dbProfile = new CaseWorkerProfile();
        dbProfile.setCaseWorkerId("CWID1");
        dbProfile.setFirstName("CWFirstName");
        dbProfile.setLastName("CWLastName");
        dbProfile.setEmailId("cwr-func-test-user@test.com");

        StaffProfileCreationRequest cwUiRequest = getStaffProfileUpdateRequest();

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        // userProfileCreationResponse.setIdamId("12345678");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        roleAdditionResponse.setIdamMessage("success");


        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE,IDAM_STATUS,
                StringUtils.EMPTY,cwUiRequest,STAFF_PROFILE_UPDATE);



        boolean updateUserRolesInIdam = staffRefDataServiceImpl.updateUserRolesInIdam(cwUiRequest,dbProfile.getCaseWorkerId());
        assertThat(updateUserRolesInIdam).isFalse();

    }


    @Test
    void test_check_staff_profile_for_update() throws JsonProcessingException {

        //ValidateStaffProfile
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_UPDATE);
        CaseWorkerProfile caseWorkerProfile = null;

        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);

        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();


        ResourceNotFoundException thrown = Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            staffRefDataServiceImpl.updateStaffProfile(staffProfileCreationRequest);
        });

        assertThat(thrown.getMessage()).contains(PROFILE_NOT_PRESENT_IN_DB);


    }

    @Test
    void test_processExistingCaseWorkers() throws JsonProcessingException {

        //ValidateStaffProfile
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");


        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();

        Map<String, StaffProfileCreationRequest> emailToRequestMap = new HashMap<>();

        emailToRequestMap.put("cwr-func-test-user@test.com",staffProfileCreationRequest);

        List<CaseWorkerProfile> cwDbProfiles = Collections.singletonList(caseWorkerProfile);
        Pair<List<CaseWorkerProfile>, List<CaseWorkerProfile>> updateAndSuspendedLists = staffRefDataServiceImpl
                .processExistingCaseWorkers(emailToRequestMap, cwDbProfiles);


        assertThat(updateAndSuspendedLists).isNotNull();


    }

    @Test
    void test_populateStaffProfile() throws JsonProcessingException {

        //ValidateStaffProfile
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");


        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();

        staffRefDataServiceImpl.populateStaffProfile(staffProfileCreationRequest,caseWorkerProfile,"CWID1");

        assertThat(caseWorkerProfile).isNotNull();
        assertThat(caseWorkerProfile.getCaseWorkerId()).isEqualTo("CWID1");
        assertThat(caseWorkerProfile.getSuspended()).isFalse();
        assertThat(caseWorkerProfile.getCaseAllocator()).isFalse();
        assertThat(caseWorkerProfile.getTaskSupervisor()).isTrue();
        assertThat(caseWorkerProfile.getUserAdmin()).isTrue();
        assertThat(caseWorkerProfile.getEmailId()).isEqualTo("cwr-func-test-user@test.com");
        assertThat(caseWorkerProfile.getFirstName()).isEqualTo("testFN");
        assertThat(caseWorkerProfile.getLastName()).isEqualTo("testLN");
        assertThat(caseWorkerProfile.getRegionId()).isEqualTo(1);
        assertThat(caseWorkerProfile.getRegion()).isEqualTo("testRegion");

    }


    @Test
    void test_updateUserProfile() throws JsonProcessingException {

        //ValidateStaffProfile
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfileInput = new CaseWorkerProfile();
        caseWorkerProfileInput.setCaseWorkerId("CWID1");
        caseWorkerProfileInput.setCaseWorkerLocations(new ArrayList<>());
        caseWorkerProfileInput.setCaseWorkerWorkAreas(new ArrayList<>());
        caseWorkerProfileInput.setCaseWorkerRoles(new ArrayList<>());

        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();

        CaseWorkerProfile caseWorkerProfile = staffRefDataServiceImpl
                .updateUserProfile(staffProfileCreationRequest,caseWorkerProfileInput);


        assertThat(caseWorkerProfile).isNotNull();
        assertThat(caseWorkerProfile.getCaseWorkerId()).isEqualTo("CWID1");
        assertThat(caseWorkerProfile.getSuspended()).isFalse();
        assertThat(caseWorkerProfile.getCaseAllocator()).isFalse();
        assertThat(caseWorkerProfile.getTaskSupervisor()).isTrue();
        assertThat(caseWorkerProfile.getUserAdmin()).isTrue();
        assertThat(caseWorkerProfile.getEmailId()).isEqualTo("cwr-func-test-user@test.com");
        assertThat(caseWorkerProfile.getFirstName()).isEqualTo("testFN");
        assertThat(caseWorkerProfile.getLastName()).isEqualTo("testLN");
        assertThat(caseWorkerProfile.getRegionId()).isEqualTo(1);
        assertThat(caseWorkerProfile.getRegion()).isEqualTo("testRegion");
        assertThat(caseWorkerProfile.getCaseWorkerLocations()).hasSize(0);
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas()).hasSize(0);
        assertThat(caseWorkerProfile.getCaseWorkerRoles()).hasSize(0);


    }

    @Test
    void test_processExistingCaseWorkers_suspendedUsers() throws JsonProcessingException {

        //ValidateStaffProfile
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");


        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();

        Map<String, StaffProfileCreationRequest> emailToRequestMap = new HashMap<>();

        emailToRequestMap.put("cwr-func-test-user@test.com",staffProfileCreationRequest);

        List<CaseWorkerProfile> cwDbProfiles = Collections.singletonList(caseWorkerProfile);
        Pair<List<CaseWorkerProfile>, List<CaseWorkerProfile>> updateAndSuspendedLists = staffRefDataServiceImpl
                .processExistingCaseWorkers(emailToRequestMap, cwDbProfiles);


        assertThat(updateAndSuspendedLists).isNotNull();


    }

    private StaffProfileCreationRequest getStaffProfileUpdateRequest() {

        Set<String> idamRoles = new HashSet<>();
        idamRoles.add("IdamRole1");
        idamRoles.add("IdamRole2");

        StaffProfileRoleRequest staffProfileRoleRequest =
                new StaffProfileRoleRequest(1, "testRole1", true);

        CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("testLocation")
                .locationId(1)
                .build();

        CaseWorkerServicesRequest caseWorkerServicesRequest = CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .serviceCode("ABCA4")
                .service("service")
                .build();

        SkillsRequest skillsRequest = SkillsRequest
                .skillsRequest()
                .skillId(1)
                .description("training")
                .build();


        StaffProfileCreationRequest staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("cwr-func-test-user@test.com")
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .services(singletonList(caseWorkerServicesRequest))
                .baseLocations(singletonList(caseWorkerLocationRequest))
                .roles(singletonList(staffProfileRoleRequest))
                .skills(singletonList(skillsRequest))
                .build();

        return staffProfileCreationRequest;

    }

    private CaseWorkerSkill getCaseWorkerSkill() {

        CaseWorkerSkill caseWorkerSkill = new CaseWorkerSkill();
        caseWorkerSkill.setCaseWorkerId("423ec46f-359f-4fcc-9ecc-b1cab4d7e683");
        caseWorkerSkill.setSkillId(1L);
        caseWorkerSkill.setSkill(getSkillData());

        return caseWorkerSkill;

    }
}
