package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.StaffProfileCreateUpdateUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@Disabled
class StaffProfileCreateServiceImplTest {
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
    IValidationService validationServiceFacade;
    @Mock
    SkillRepository skillRepository;
    @Mock
    IJsrValidatorInitializer validateStaffProfile;
    @Mock
    private StaffProfileCreateUpdateUtil staffProfileCreateUpdateUtil;
    @Mock
    StaffAuditRepository staffAuditRepository;

    static final String COMMON_EMAIL_PATTERN = "CWR-func-test-user";

    private StaffProfileCreationRequest staffProfileCreationRequest;
    private StaffProfileCreationResponse staffProfileCreationRespone = new StaffProfileCreationResponse();
    private RoleType roleType;
    private UserType userType;
    private Skill skill;
    private CaseWorkerProfile caseWorkerProfile;

    ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private StaffProfileCreateServiceImpl staffProfileServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<String> idamRoles = new HashSet<>();
        idamRoles.add("IdamRole1");
        idamRoles.add("IdamRole2");

        CaseWorkerRoleRequest caseWorkerRoleRequest =
                new CaseWorkerRoleRequest("testRole1", true);

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
                .skillId("1L")
                .description("training")
                .build();


        staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .staffAdmin(true)
                .emailId("test@test.com")
                .roles(singletonList(caseWorkerRoleRequest))
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .services(singletonList(caseWorkerServicesRequest))
                .baseLocations(singletonList(caseWorkerLocationRequest))
                .roles(singletonList(caseWorkerRoleRequest))
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
    }

    @Test
    void test_saveStaffProfile() throws JsonProcessingException {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        String body = mapper.writeValueAsString(userProfileCreationResponse);
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);

        when(userProfileFeignClient.createUserProfile(any(),any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(201).build());

        staffProfileServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        verify(staffProfileCreateUpdateUtil, times(1)).persistStaffProfile(any());
        verify(validateStaffProfile, times(1)).validateStaffProfile(any());
    }

    @Test
    void test_saveStaffProfileValidation() throws JsonProcessingException {

        when(staffProfileCreateUpdateUtil.persistStaffProfile(any())).thenReturn(caseWorkerProfile);
        StaffProfileCreationResponse response = staffProfileServiceImpl
                .processStaffProfileCreation(staffProfileCreationRequest);
        assertThat(response.getCaseWorkerId()).isEqualTo("CWID1");
    }

    @Test
    void test_saveStaffProfileValidationAudit() throws JsonProcessingException {

        validationServiceFacade.saveStaffAudit(AuditStatus.SUCCESS,null,
                caseWorkerProfile.getCaseWorkerId(),staffProfileCreationRequest);
        verify(staffAuditRepository, times(0)).save(any());
    }

    @Test
    void test_409WhileCwUserProfileCreation() throws JsonProcessingException {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        String body = mapper.writeValueAsString(userProfileCreationResponse);
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);

        when(userProfileFeignClient.createUserProfile(any(),any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(409).build());

        staffProfileServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        verify(staffProfileCreateUpdateUtil, times(1)).persistStaffProfile(any());
    }

    @Test
    void test_saveStaffProfileAlreadyPresent() throws JsonProcessingException {
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);
        validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest);
        InvalidRequestException thrown = Assertions.assertThrows(InvalidRequestException.class, () -> {
            staffProfileServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        });

        assertThat(thrown.getMessage()).contains("The profile is already created for the given email Id");
    }

    @Test
    void test_createUserProfileRequest() {
        UserProfileCreationRequest response = staffProfileServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).isEqualTo("EN");
        assertThat(response.getUserCategory().toString()).isEqualTo("CASEWORKER");
        assertThat(response.getUserType().toString()).isEqualTo("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void test_createUserProfileRequestEmptyRoles() {
        when(staffProfileCreateUpdateUtil.getUserRolesByRoleId(any())).thenReturn(EMPTY_SET);
        UserProfileCreationRequest response = staffProfileServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).isEqualTo("EN");
        assertThat(response.getUserCategory().toString()).isEqualTo("CASEWORKER");
        assertThat(response.getUserType().toString()).isEqualTo("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(0);
    }

    @Test
    void test_createUserProfileRequestNullRoles() {
        when(staffProfileCreateUpdateUtil.getUserRolesByRoleId(any())).thenReturn(null);
        UserProfileCreationRequest response = staffProfileServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).isEqualTo("EN");
        assertThat(response.getUserCategory().toString()).isEqualTo("CASEWORKER");
        assertThat(response.getUserType().toString()).isEqualTo("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(0);
    }

    @Test
    void test_buildCreateUserProfile_exception() throws IOException {
        Mockito.when(staffProfileServiceImpl.createUserProfileInIdamUP(any()))
                .thenThrow(new RuntimeException("Failure test"));
        Exception thrown  = Assertions.assertThrows(Exception.class, () -> {
            staffProfileServiceImpl.createCaseWorkerProfile(any());
        });
        assertThat(thrown.getMessage()).contains("Failure test");
    }

    @Test
    void test_publishCaseWorkerDataToTopic() {
        ReflectionTestUtils.setField(staffProfileServiceImpl, "caseWorkerDataPerMessage", 1);
        staffProfileCreationRespone.setCaseWorkerId("1");
        staffProfileServiceImpl.publishStaffProfileToTopic(staffProfileCreationRespone);
        verify(topicPublisher, times(1)).sendMessage(any());
    }
}