package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.client.domain.AttributeResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.StaffReferenceException;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_NOT_ACTIVE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_SUSPENDED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_USER_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ORIGIN_EXUI;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_NOT_PRESENT_IN_SRD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PROFILE_NOT_PRESENT_IN_UP_OR_IDAM;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_CREATE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_UPDATE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STATUS_ACTIVE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.UP_STATUS_PENDING;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"AbbreviationAsWordInName", "unchecked"})
class StaffRefDataUpdateStaffServiceImplTest {
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
    void test_updateStaffProfile_when_user_exists_in_idam_only() throws JsonProcessingException {
        //a) user exists in idam only - user update request with same email id but different name and role,
        // it should be 404 as user does not exist in SRD


        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);

        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);


        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);

        userProfileResponse.setRoles(roles);
        userProfileResponse.setFirstName("testFNChanged");
        userProfileResponse.setLastName("testLNChanged");

        UserProfileResponse userProfileResponseEmpty = new UserProfileResponse();

        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponseEmpty),
                                defaultCharset())
                        .status(404).build());

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("12345678");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        roleAdditionResponse.setIdamMessage("success");


        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();



        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            StaffProfileCreationResponse staffProfileCreationResponse  = staffRefDataServiceImpl
                    .updateStaffProfile(staffProfileCreationRequest);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(PROFILE_NOT_PRESENT_IN_UP_OR_IDAM);


    }


    @Test
    void test_updateStaffProfile_user_exists_in_up_and_idam() throws JsonProcessingException {
        //b) use exists in idam and up only - user update request with same email id but different name
        // and role,it should be 404 as user does not exist in SRD

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);

        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);


        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);

        userProfileResponse.setRoles(roles);
        userProfileResponse.setFirstName("testFNChanged");
        userProfileResponse.setLastName("testLNChanged");


        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("12345678");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        roleAdditionResponse.setIdamMessage("success");


        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            StaffProfileCreationResponse staffProfileCreationResponse  = staffRefDataServiceImpl
                    .updateStaffProfile(staffProfileCreationRequest);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(PROFILE_NOT_PRESENT_IN_SRD);

    }

    @Test
    void test_updateStaffProfile_with_changed_values_UpAndIdam_Status_Pending() throws JsonProcessingException {
        // Scenario 6: User exists in SRD,UP and IDAM, UP and IDAM Status is PENDING

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);

        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(UP_STATUS_PENDING);

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


        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            StaffProfileCreationResponse staffProfileCreationResponse  = staffRefDataServiceImpl
                    .updateStaffProfile(staffProfileCreationRequest);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_NOT_ACTIVE);
    }

    @Test
    void test_updateStaffProfile_with_changed_values_UpAndIdam_Status_Suspended() throws JsonProcessingException {
        // Scenario 7: User exists in SRD,UP and IDAM, UP and IDAM Status is PENDING

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);

        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(IDAM_STATUS_SUSPENDED);

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


        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            StaffProfileCreationResponse staffProfileCreationResponse  = staffRefDataServiceImpl
                    .updateStaffProfile(staffProfileCreationRequest);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_NOT_ACTIVE);
    }

    @Test
    void test_updateStaffProfile_with_changed_values() throws JsonProcessingException {


        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);

        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);
        when(caseWorkerProfileRepository.save(any())).thenReturn(caseWorkerProfile);


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
    void test_updateStaffProfile_with_changed_values_with_exception() throws JsonProcessingException {


        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");


        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);


        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(IDAM_STATUS_SUSPENDED);

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


        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();
        StaffProfileCreationRequest staffProfileCreationRequestEmpty = StaffProfileCreationRequest
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
                .userType("testUser1").build();

        List<StaffProfileCreationRequest> stafProfiles  =
                List.of(staffProfileCreationRequest, staffProfileCreationRequestEmpty);

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl.updateStaffProfiles(staffProfileCreationRequestEmpty,caseWorkerProfile);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_NOT_ACTIVE);


    }

    @Test
    void test_isUserSuspended_should_return_false() throws JsonProcessingException {
        CaseWorkerProfile dbProfile = new CaseWorkerProfile();
        dbProfile.setCaseWorkerId("CWID1");
        dbProfile.setFirstName("CWFirstName");
        dbProfile.setLastName("CWLastName");
        dbProfile.setEmailId("cwr-func-test-user@test.com");

        StaffProfileCreationRequest cwUiRequest = getStaffProfileUpdateRequest();

        boolean suspended = staffRefDataServiceImpl
                .isUserSuspended(UserProfileUpdatedData.builder().idamStatus(IDAM_STATUS_SUSPENDED).build(),
                dbProfile.getCaseWorkerId(), ORIGIN_EXUI);
        assertThat(suspended).isFalse();

    }

    @Test
    void test_isUserSuspended_should_return_false_with_roles_data() throws JsonProcessingException {
        CaseWorkerProfile dbProfile = new CaseWorkerProfile();
        dbProfile.setCaseWorkerId("CWID1");
        dbProfile.setFirstName("CWFirstName");
        dbProfile.setLastName("CWLastName");
        dbProfile.setEmailId("cwr-func-test-user@test.com");


        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
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
        StaffProfileCreationRequest cwUiRequest = getStaffProfileUpdateRequest();

        boolean suspended = staffRefDataServiceImpl
                .isUserSuspended(UserProfileUpdatedData.builder().idamStatus(IDAM_STATUS_SUSPENDED).build(),
                dbProfile.getCaseWorkerId(), ORIGIN_EXUI);
        assertThat(suspended).isFalse();

    }

    @Test
    void testSuspendStaffUserProfile() throws JsonProcessingException {

        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();
        staffProfileCreationRequest.setSuspended(true);
        CaseWorkerProfile profile = new CaseWorkerProfile();
        profile.setCaseWorkerId("1");
        profile.setSuspended(false);
        profile.setEmailId(staffProfileCreationRequest.getEmailId());


        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        AttributeResponse attributeResponse = new AttributeResponse();
        attributeResponse.setIdamStatusCode(HttpStatus.OK.value());
        userProfileRolesResponse.setAttributeResponse(attributeResponse);


        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileRolesResponse),
                                defaultCharset())
                        .status(200).build());

        List<StaffProfileCreationRequest> requests = new ArrayList<>();
        requests.add(staffProfileCreationRequest);
        staffRefDataServiceImpl.updateStaffProfiles(staffProfileCreationRequest,profile);

        verify(caseWorkerProfileRepository, times(1)).save(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
    }


    @Test
    void test_updateUserRolesInIdam_with_IdamRoles_Idam_Status_Active() throws JsonProcessingException {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");


        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);


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

        StaffProfileCreationRequest cwUiRequest =  getStaffProfileUpdateRequest();

        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE,IDAM_STATUS,
                StringUtils.EMPTY,cwUiRequest,STAFF_PROFILE_UPDATE);


        boolean updateUserRolesInIdam = staffRefDataServiceImpl
                .updateUserRolesInIdam(cwUiRequest,caseWorkerProfile.getCaseWorkerId(),STAFF_PROFILE_UPDATE);
        assertThat(updateUserRolesInIdam).isTrue();

    }

    @Test
    void test_updateUserRolesInIdam_with_IdamRoles_Idam_Status_InActive() throws JsonProcessingException {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");


        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);


        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(IDAM_STATUS_SUSPENDED);

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

        StaffProfileCreationRequest cwUiRequest =  getStaffProfileUpdateRequest();

        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE,IDAM_STATUS,
                StringUtils.EMPTY,cwUiRequest,STAFF_PROFILE_UPDATE);

        String caseWorkerId = caseWorkerProfile.getCaseWorkerId();

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            boolean updateUserRolesInIdam = staffRefDataServiceImpl
                    .updateUserRolesInIdam(cwUiRequest,caseWorkerId,STAFF_PROFILE_UPDATE);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_NOT_ACTIVE);

    }

    @Test
    void test_updateUserRolesInIdam_with_IdamRoles_Empty() throws JsonProcessingException {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");


        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);


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
                                null)).body(mapper.writeValueAsString(null),
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

        StaffProfileCreationRequest cwUiRequest =  getStaffProfileUpdateRequest();

        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE,IDAM_STATUS,
                StringUtils.EMPTY,cwUiRequest,STAFF_PROFILE_UPDATE);


        String caseWorkerId = caseWorkerProfile.getCaseWorkerId();
        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            boolean updateUserRolesInIdam = staffRefDataServiceImpl
                    .updateUserRolesInIdam(cwUiRequest,caseWorkerId,STAFF_PROFILE_UPDATE);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_USER_PROFILE);

    }

    @Test
    void test_updateUserRolesInIdam() throws JsonProcessingException {
        CaseWorkerProfile dbProfile = new CaseWorkerProfile();
        dbProfile.setCaseWorkerId("CWID1");
        dbProfile.setFirstName("CWFirstName");
        dbProfile.setLastName("CWLastName");
        dbProfile.setEmailId("cwr-func-test-user@test.com");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(IDAM_STATUS_SUSPENDED);

        userProfileResponse.setRoles(roles);
        userProfileResponse.setFirstName("testFNChanged");
        userProfileResponse.setLastName("testLNChanged");

        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(200).build());

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();

        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        roleAdditionResponse.setIdamMessage("success");

        StaffProfileCreationRequest cwUiRequest = getStaffProfileUpdateRequest();

        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE,IDAM_STATUS,
                StringUtils.EMPTY,cwUiRequest,STAFF_PROFILE_UPDATE);
        String caserWorkerId = dbProfile.getCaseWorkerId();
        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            boolean updateUserRolesInIdam = staffRefDataServiceImpl
                    .updateUserRolesInIdam(cwUiRequest,caserWorkerId,STAFF_PROFILE_UPDATE);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_NOT_ACTIVE);

    }

    @Test
    void test_updateUserRolesInIdamDataMistmatch() throws JsonProcessingException {
        CaseWorkerProfile dbProfile = new CaseWorkerProfile();
        dbProfile.setCaseWorkerId("CWID1");
        dbProfile.setFirstName("CWFirstNameU");
        dbProfile.setLastName("CWLastNameU");
        dbProfile.setEmailId("cwr-func-test-user@test.com");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(IDAM_STATUS_SUSPENDED);

        userProfileResponse.setRoles(roles);
        userProfileResponse.setFirstName("testFNChanged");
        userProfileResponse.setLastName("testLNChanged");

        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(200).build());

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();

        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        roleAdditionResponse.setIdamMessage("success");

        StaffProfileCreationRequest cwUiRequest = getStaffProfileUpdateRequest();
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE,IDAM_STATUS,
                StringUtils.EMPTY,cwUiRequest,STAFF_PROFILE_UPDATE);
        String caserWorkerId = dbProfile.getCaseWorkerId();
        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            boolean updateUserRolesInIdam = staffRefDataServiceImpl
                    .updateUserRolesInIdam(cwUiRequest,caserWorkerId,STAFF_PROFILE_UPDATE);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_NOT_ACTIVE);



    }


    @Test
    void test_updateUserRolesInIdam_with_StaffAdminRoleDelete_Idam_Status_Active() throws JsonProcessingException {

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);

        userProfileResponse.setRoles(roles);
        userProfileResponse.setFirstName("testFN");
        userProfileResponse.setLastName("testLN");

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


        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileRolesResponse),
                                defaultCharset())
                        .status(200).build());

        StaffProfileCreationRequest cwUiRequest =  getStaffProfileUpdateRequest();
        cwUiRequest.setStaffAdmin(false);

        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE,IDAM_STATUS,
                StringUtils.EMPTY,cwUiRequest,STAFF_PROFILE_UPDATE);


        boolean updateUserRolesInIdam = staffRefDataServiceImpl
                .updateUserRolesInIdam(cwUiRequest,caseWorkerProfile.getCaseWorkerId(),STAFF_PROFILE_UPDATE);
        assertThat(updateUserRolesInIdam).isFalse();
    }



    @Test
    void test_check_staff_profile_for_update() throws JsonProcessingException {

        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_UPDATE);
        CaseWorkerProfile caseWorkerProfile = null;

        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);

        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();


        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl.updateStaffProfile(staffProfileCreationRequest);
        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(PROFILE_NOT_PRESENT_IN_SRD);


    }

    @Test
    void test_processExistingCaseWorkers() throws JsonProcessingException {

        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(IDAM_STATUS_SUSPENDED);

        userProfileResponse.setRoles(roles);
        userProfileResponse.setFirstName("testFNChanged");
        userProfileResponse.setLastName("testLNChanged");

        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(200).build());

        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();

        Map<String, StaffProfileCreationRequest> emailToRequestMap = new HashMap<>();

        emailToRequestMap.put("cwr-func-test-user@test.com",staffProfileCreationRequest);

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl
                    .processExistingCaseWorkers(staffProfileCreationRequest, caseWorkerProfile);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_NOT_ACTIVE);


    }

    @Test
    void test_PopulateStaffProfile() throws JsonProcessingException {

        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setLocation("Location1");
        caseWorkerLocation.setLocationId(1);
        when(staffProfileCreateUpdateUtil.mapStaffLocationRequest(anyString(), any()))
                .thenReturn(List.of(caseWorkerLocation));

        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setRoleId(1L);
        caseWorkerRole.setPrimaryFlag(true);

        when(staffProfileCreateUpdateUtil.mapStaffRoleRequestMapping(anyString(), any()))
                .thenReturn(List.of(caseWorkerRole));

        CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea();
        caseWorkerWorkArea.setCaseWorkerWorkAreaId(1L);
        caseWorkerWorkArea.setAreaOfWork("Service1");


        when(staffProfileCreateUpdateUtil.mapStaffAreaOfWork(any(),anyString()))
                .thenReturn(List.of(caseWorkerWorkArea));

        CaseWorkerSkill caseWorkerSkill = new CaseWorkerSkill();
        caseWorkerSkill.setSkillId(1L);

        when(staffProfileCreateUpdateUtil.mapStaffSkillRequestMapping(anyString(),any()))
                .thenReturn(List.of(caseWorkerSkill));
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
        assertThat(caseWorkerProfile.isNew()).isFalse();
        assertThat(caseWorkerProfile.getUserType()).isNull();

        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getLocationId()).isEqualTo(1);
        assertThat(caseWorkerProfile.getCaseWorkerLocations().get(0).getLocation()).isEqualTo("Location1");

        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getRoleId()).isEqualTo(1L);
        assertThat(caseWorkerProfile.getCaseWorkerRoles().get(0).getPrimaryFlag()).isTrue();

        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getCaseWorkerWorkAreaId()).isEqualTo(1L);
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas().get(0).getAreaOfWork()).isEqualTo("Service1");

        assertThat(caseWorkerProfile.getCaseWorkerSkills().get(0).getSkillId()).isEqualTo(1L);


    }

    @Test
    void test_PopulateStaffProfile_with_empty_Skills() throws JsonProcessingException {

        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");


        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();
        staffProfileCreationRequest.setSkills(null);

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
        assertThat(caseWorkerProfile.getCaseWorkerLocations()).isEmpty();
        assertThat(caseWorkerProfile.getCaseWorkerWorkAreas()).isEmpty();
        assertThat(caseWorkerProfile.getCaseWorkerRoles()).isEmpty();


    }

    @Test
    void test_processExistingCaseWorkers_suspendedUsers() throws JsonProcessingException {

        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest,STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(IDAM_STATUS_SUSPENDED);

        userProfileResponse.setRoles(roles);
        userProfileResponse.setFirstName("testFNChanged");
        userProfileResponse.setLastName("testLNChanged");

        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(200).build());

        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();

        Map<String, StaffProfileCreationRequest> emailToRequestMap = new HashMap<>();

        emailToRequestMap.put("cwr-func-test-user@test.com",staffProfileCreationRequest);

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl
                    .processExistingCaseWorkers(staffProfileCreationRequest, caseWorkerProfile);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_NOT_ACTIVE);



    }

    @Test
    void test_setNewCaseWorkerProfileFlagFlow1() throws JsonProcessingException {
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        staffRefDataServiceImpl.setNewCaseWorkerProfileFlag(caseWorkerProfile);

        assertThat(caseWorkerProfile.isNew()).isTrue();

    }

    @Test
    void test_setNewCaseWorkerProfileFlagFlow2() throws JsonProcessingException {
        CaseWorkerProfile caseWorkerProfile = null;

        staffRefDataServiceImpl.setNewCaseWorkerProfileFlag(caseWorkerProfile);

        assertThat(caseWorkerProfile).isNull();

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
