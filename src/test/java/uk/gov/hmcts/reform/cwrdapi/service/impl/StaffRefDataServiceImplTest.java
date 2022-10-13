package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.StaffReferenceException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.StaffReferenceException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.domain.SkillDTO;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerSkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.SkillRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.StaffProfileCreateUpdateUtil;
import uk.gov.hmcts.reform.cwrdapi.repository.StaffAuditRepository;
import uk.gov.hmcts.reform.cwrdapi.service.ICwrdCommonRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;
import uk.gov.hmcts.reform.cwrdapi.util.StaffProfileCreateUpdateUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STATUS_ACTIVE;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"AbbreviationAsWordInName", "unchecked"})
class StaffRefDataServiceImplTest {
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
    IValidationService validationServiceFacade;
    @Mock
    IJsrValidatorInitializer validateStaffProfile;
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
    IJsrValidatorInitializer validateStaffProfile;
    @Mock
    private StaffProfileCreateUpdateUtil staffProfileCreateUpdateUtil;
    @Mock
    StaffAuditRepository staffAuditRepository;

    @Mock
    CaseWorkerSkillRepository caseWorkerSkillRepository;

    @Mock
    ICwrdCommonRepository cwrCommonRepository;

    private StaffProfileCreationRequest staffProfileCreationRequest;
    private StaffProfileCreationResponse staffProfileCreationRespone = new StaffProfileCreationResponse();
    private RoleType roleType;
    private UserType userType;
    private Skill skill;
    private CaseWorkerProfile caseWorkerProfile;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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

    }

    @Test
    void should_return_non_empty_list_of_service_skills() {
        List<Skill> skills = getSkillsData();
        when(skillRepository.findAll()).thenReturn(skills);
        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataServiceImpl.getServiceSkills();

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();

        assertThat(serviceSkills).isNotNull();

        ServiceSkill serviceSkill = serviceSkills.get(0);

        assertThat(serviceSkill.getId()).isEqualTo("BBA3");

        SkillDTO skillDTO = serviceSkill.getSkills().get(0);

        assertThat(skillDTO.getSkillId()).isEqualTo(1L);
        assertThat(skillDTO.getSkillCode()).isEqualTo("A1");
        assertThat(skillDTO.getDescription()).isEqualTo("desc1");
        assertThat(skillDTO.getUserType()).isEqualTo("user_type1");

    }

    @Test
    void should_return_empty_list_of_service_skills() {
        List<Skill> skills = new ArrayList<>();
        when(skillRepository.findAll()).thenReturn(skills);
        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataServiceImpl.getServiceSkills();

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();
        assertThat(serviceSkills).isEmpty();

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

    @Test
    void test_saveStaffProfile() throws JsonProcessingException {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        String body = mapper.writeValueAsString(userProfileCreationResponse);
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);

        when(userProfileFeignClient.createUserProfile(any(),any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(201).build());
        when(caseWorkerProfileRepository.save(any())).thenReturn(caseWorkerProfile);
        staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        verify(caseWorkerProfileRepository, times(1)).save(any());
        verify(validateStaffProfile, times(1)).validateStaffProfile(any());
    }

    @Test
    void test_saveStaffProfileValidationAudit() {

        validationServiceFacade.saveStaffAudit(AuditStatus.SUCCESS,null,
                caseWorkerProfile.getCaseWorkerId(),staffProfileCreationRequest);
        verify(staffAuditRepository, times(0)).save(any());
    }

    @Test
    void test_saveStaffProfileAlreadyPresent() {
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);
        validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest);
        InvalidRequestException thrown = Assertions.assertThrows(InvalidRequestException.class, () -> {
            staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        });

        assertThat(thrown.getMessage()).contains("The profile is already created for the given email Id");
    }

    @Test
    void test_newStaffProfileSuspended() {
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);
        validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest);
        staffProfileCreationRequest.setSuspended(true);
        InvalidRequestException thrown = Assertions.assertThrows(InvalidRequestException.class, () -> {
            staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        });
        String errorMsg = "There is no user present to suspend. "
                + "Please try again or check with HMCTS Support Team";
        assertThat(thrown.getMessage()).contains(errorMsg);
    }

    @Test
    void test_createUserProfileRequest() {
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void test_createUserProfileRequest_StaffAdmin() {
        staffProfileCreationRequest.setStaffAdmin(true);
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void test_createUserProfileRequestEmptyRoles() {
        when(staffProfileCreateUpdateUtil.getUserRolesByRoleId(any())).thenReturn(EMPTY_SET);
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(0);
    }



    @Test
    void test_createUserProfileRequestNullRoles() {
        when(staffProfileCreateUpdateUtil.getUserRolesByRoleId(any())).thenReturn(null);
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(0);
    }

    @Test
    void test_publishCaseWorkerDataToTopic() {
        ReflectionTestUtils.setField(staffRefDataServiceImpl, "caseWorkerDataPerMessage", 1);
        staffProfileCreationRespone.setCaseWorkerId("1");
        staffRefDataServiceImpl.publishStaffProfileToTopic(staffProfileCreationRespone);
        verify(topicPublisher, times(1)).sendMessage(any());

    }


    @Test
    void test_persistStaffProfileNull() {
        when(caseWorkerProfileRepository.save(any())).thenReturn(null);
        caseWorkerProfile = staffRefDataServiceImpl.persistStaffProfile(caseWorkerProfile,staffProfileCreationRequest);
        assertNull(caseWorkerProfile);
    }

    @Test
    void test_persistStaffProfile() {
        when(caseWorkerProfileRepository.save(any())).thenReturn(caseWorkerProfile);
        caseWorkerProfile = staffRefDataServiceImpl.persistStaffProfile(caseWorkerProfile,staffProfileCreationRequest);
        assertThat(caseWorkerProfile.getCaseWorkerId()).isEqualTo("CWID1");
        assertThat(caseWorkerProfile.getFirstName()).isEqualTo("CWFirstName");
        assertThat(caseWorkerProfile.getLastName()).isEqualTo("CWLastName");
    }

    @Test
    void test_createUserProfileInIdamUP() throws JsonProcessingException {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        String body = mapper.writeValueAsString(userProfileCreationResponse);
        when(userProfileFeignClient.createUserProfile(any(),any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(409).build());
        ResponseEntity<Object> response = staffRefDataServiceImpl
                .createUserProfileInIdamUP(staffProfileCreationRequest);
        assertNotNull(response);
    }

    @Test
    void test_createUserProfileInIdamUP_error() throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(500,"Failure","Method Not Allowed ",
                "Internal Server Error", "2022-01-10");
        String body = mapper.writeValueAsString(errorResponse);
        doReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(500).build())
                .when(userProfileFeignClient).createUserProfile(any(UserProfileCreationRequest.class),anyString());

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl.createUserProfileInIdamUP(staffProfileCreationRequest);
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,thrown.getStatus());
    }

    @Test
    void test_createUserProfileInIdamUP_forbiddenError() throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(405,"Failure","Method Not Allowed ",
                "Method Not Allowed", "2022-01-10");
        String body = mapper.writeValueAsString(errorResponse);

        doReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(405).build())
                .when(userProfileFeignClient).createUserProfile(any(UserProfileCreationRequest.class),anyString());
        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl.createUserProfileInIdamUP(staffProfileCreationRequest);
        });
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED,thrown.getStatus());
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
        when(caseWorkerProfileRepository.save(any())).thenReturn(caseWorkerProfile);
        staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        verify(caseWorkerProfileRepository, times(1)).save(any());
        verify(validateStaffProfile, times(1)).validateStaffProfile(any());
    }

    @Test
    void test_saveStaffProfileValidationAudit() {

        validationServiceFacade.saveStaffAudit(AuditStatus.SUCCESS,null,
                caseWorkerProfile.getCaseWorkerId(),staffProfileCreationRequest);
        verify(staffAuditRepository, times(0)).save(any());
    }

    @Test
    void test_saveStaffProfileAlreadyPresent() {
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);
        validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest);
        InvalidRequestException thrown = Assertions.assertThrows(InvalidRequestException.class, () -> {
            staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        });

        assertThat(thrown.getMessage()).contains("The profile is already created for the given email Id");
    }

    @Test
    void test_newStaffProfileSuspended() {
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);
        validationServiceFacade.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest);
        staffProfileCreationRequest.setSuspended(true);
        InvalidRequestException thrown = Assertions.assertThrows(InvalidRequestException.class, () -> {
            staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        });
        String errorMsg = "There is no user present to suspend. "
                + "Please try again or check with HMCTS Support Team";
        assertThat(thrown.getMessage()).contains(errorMsg);
    }

    @Test
    void test_createUserProfileRequest() {
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void test_createUserProfileRequest_StaffAdmin() {
        staffProfileCreationRequest.setStaffAdmin(true);
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    void test_createUserProfileRequestEmptyRoles() {
        when(staffProfileCreateUpdateUtil.getUserRolesByRoleId(any())).thenReturn(EMPTY_SET);
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(0);
    }



    @Test
    void test_createUserProfileRequestNullRoles() {
        when(staffProfileCreateUpdateUtil.getUserRolesByRoleId(any())).thenReturn(null);
        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);
        assertThat(response.getEmail()).isEqualTo("test@test.com");
        assertThat(response.getFirstName()).isEqualTo("testFN");
        assertThat(response.getLastName()).isEqualTo("testLN");
        assertThat(response.getLanguagePreference().toString()).hasToString("EN");
        assertThat(response.getUserCategory().toString()).hasToString("CASEWORKER");
        assertThat(response.getUserType().toString()).hasToString("INTERNAL");
        assertThat(response.getRoles()).hasSizeGreaterThanOrEqualTo(0);
    }

    @Test
    void test_publishCaseWorkerDataToTopic() {
        ReflectionTestUtils.setField(staffRefDataServiceImpl, "caseWorkerDataPerMessage", 1);
        staffProfileCreationRespone.setCaseWorkerId("1");
        staffRefDataServiceImpl.publishStaffProfileToTopic(staffProfileCreationRespone);
        verify(topicPublisher, times(1)).sendMessage(any());

    }


    @Test
    void test_persistStaffProfileNull() {
        when(caseWorkerProfileRepository.save(any())).thenReturn(null);
        caseWorkerProfile = staffRefDataServiceImpl.persistStaffProfile(caseWorkerProfile,staffProfileCreationRequest);
        assertNull(caseWorkerProfile);
    }

    @Test
    void test_persistStaffProfile() {
        when(caseWorkerProfileRepository.save(any())).thenReturn(caseWorkerProfile);
        caseWorkerProfile = staffRefDataServiceImpl.persistStaffProfile(caseWorkerProfile,staffProfileCreationRequest);
        assertThat(caseWorkerProfile.getCaseWorkerId()).isEqualTo("CWID1");
        assertThat(caseWorkerProfile.getFirstName()).isEqualTo("CWFirstName");
        assertThat(caseWorkerProfile.getLastName()).isEqualTo("CWLastName");
    }

    @Test
    void test_createUserProfileInIdamUP() throws JsonProcessingException {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        String body = mapper.writeValueAsString(userProfileCreationResponse);
        when(userProfileFeignClient.createUserProfile(any(),any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(409).build());
        ResponseEntity<Object> response = staffRefDataServiceImpl
                .createUserProfileInIdamUP(staffProfileCreationRequest);
        assertNotNull(response);
    }

    @Test
    void test_createUserProfileInIdamUP_error() throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(500,"Failure","Method Not Allowed ",
                "Internal Server Error", "2022-01-10");
        String body = mapper.writeValueAsString(errorResponse);
        doReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(500).build())
                .when(userProfileFeignClient).createUserProfile(any(UserProfileCreationRequest.class),anyString());

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl.createUserProfileInIdamUP(staffProfileCreationRequest);
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,thrown.getStatus());
    }

    @Test
    void test_createUserProfileInIdamUP_forbiddenError() throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(405,"Failure","Method Not Allowed ",
                "Method Not Allowed", "2022-01-10");
        String body = mapper.writeValueAsString(errorResponse);

        doReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(405).build())
                .when(userProfileFeignClient).createUserProfile(any(UserProfileCreationRequest.class),anyString());
        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl.createUserProfileInIdamUP(staffProfileCreationRequest);
        });
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED,thrown.getStatus());
    }


    @Test
    void test_update_staff_profile_with_changed_values() throws JsonProcessingException {

        StaffProfileCreationRequest staffProfileCreationRequest =  getStaffProfileUpdateRequest();
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

        StaffProfileCreationResponse staffProfileCreationResponse  = staffRefDataServiceImpl
                .updateStaffProfile(staffProfileCreationRequest);

        UserProfileCreationRequest response = staffRefDataServiceImpl
                .createUserProfileRequest(staffProfileCreationRequest);

        assertThat(staffProfileCreationResponse).isNotNull();
        assertThat(staffProfileCreationResponse.getCaseWorkerId()).isEqualTo("CWID1");
    }

    private StaffProfileCreationRequest getStaffProfileUpdateRequest(){

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
                .skillId("1L")
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
}
