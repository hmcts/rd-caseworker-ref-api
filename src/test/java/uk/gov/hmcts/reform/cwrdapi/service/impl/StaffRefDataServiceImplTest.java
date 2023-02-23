package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.hibernate.validator.internal.util.Contracts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.SkillResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.StaffReferenceException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SkillsRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserByIdResponse;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.EMPTY_SET;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.TestSupport.validateSearchUserProfileResponse;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_NOT_ACTIVE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_USER_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_CREATE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STAFF_PROFILE_UPDATE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STATUS_ACTIVE;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateAndBuildPagination;

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


    SearchRequest searchReq;

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

    @Test
    void should_return_null_list_of_service_skills() {
        List<Skill> skills = null;
        when(skillRepository.findAll()).thenReturn(skills);
        StaffWorkerSkillResponse staffWorkerSkillResponse = staffRefDataServiceImpl.getServiceSkills();

        assertThat(staffWorkerSkillResponse).isNotNull();

        List<ServiceSkill> serviceSkills = staffWorkerSkillResponse.getServiceSkills();

        assertThat(serviceSkills).isEmpty();
    }

    @Test
    void should_return_case_worker_profile_with_status_code_200() {

        var pageRequest =
                validateAndBuildPagination(20, 1,
                        20, 1);

        String searchString = "cwr";
        ArrayList<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile caseWorkerProfile = buildCaseWorkerProfile();
        caseWorkerProfiles.add(caseWorkerProfile);
        Page<CaseWorkerProfile> pages = new PageImpl<>(caseWorkerProfiles);

        when(caseWorkerProfileRepository.findByFirstNameOrLastName(searchString.toLowerCase(), pageRequest))
                .thenReturn(pages);
        ResponseEntity<List<SearchStaffUserResponse>> responseEntity =
                staffRefDataServiceImpl.retrieveStaffUserByName(searchString, pageRequest);
        assertEquals(200, responseEntity.getStatusCodeValue());

        assertThat(responseEntity.getHeaders().get("total-records").get(0)).isEqualTo("1");

        List<SearchStaffUserResponse> searchResponse =
                responseEntity.getBody();
        assertThat(responseEntity.getBody()).isNotNull();
        validateSearchStaffUserResponses(searchResponse);
    }

    @Test
    void should_return_case_worker_profile_with_status_code_200_when_missing_boolean_values() {

        CaseWorkerProfile caseWorkerProfile = buildCaseWorkerProfile();

        caseWorkerProfile.setSuspended(null);
        caseWorkerProfile.setTaskSupervisor(null);
        caseWorkerProfile.setCaseAllocator(null);
        caseWorkerProfile.setUserAdmin(null);

        ArrayList<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(caseWorkerProfile);
        Page<CaseWorkerProfile> pages = new PageImpl<>(caseWorkerProfiles);

        var pageRequest =
                validateAndBuildPagination(20, 1,
                        20, 1);
        String searchString = "cwr";
        when(caseWorkerProfileRepository.findByFirstNameOrLastName(searchString.toLowerCase(), pageRequest))
                .thenReturn(pages);
        ResponseEntity<List<SearchStaffUserResponse>> responseEntity =
                staffRefDataServiceImpl.retrieveStaffUserByName(searchString, pageRequest);

        assertEquals(200, responseEntity.getStatusCodeValue());
        assertThat(responseEntity.getHeaders().get("total-records").get(0)).isEqualTo("1");

        List<SearchStaffUserResponse> searchResponse =
                responseEntity.getBody();
        assertThat(responseEntity.getBody()).isNotNull();
        validateSearchStaffUserFalseResponses(searchResponse);
    }

    @Test
    void should_return_empty_list_of_case_worker_profile_with_status_code_200() {

        var pageRequest =
                validateAndBuildPagination(20, 1,
                        20, 1);

        String searchString = "cwr";
        ArrayList<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        Page<CaseWorkerProfile> pages = new PageImpl<>(caseWorkerProfiles);

        when(caseWorkerProfileRepository.findByFirstNameOrLastName(searchString.toLowerCase(), pageRequest))
                .thenReturn(pages);
        ResponseEntity<List<SearchStaffUserResponse>> responseEntity =
                staffRefDataServiceImpl.retrieveStaffUserByName(searchString, pageRequest);
        assertEquals(200, responseEntity.getStatusCodeValue());

        List<SearchStaffUserResponse> searchResponse =
                responseEntity.getBody();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(searchResponse).isEmpty();
    }

    @Test
    void should_return_case_worker_profileSearch_with_status_code_200() {
        ArrayList<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile caseWorkerProfile = buildCaseWorkerProfile();
        caseWorkerProfiles.add(caseWorkerProfile);
        Page<CaseWorkerProfile> pages = new PageImpl<>(caseWorkerProfiles);
        searchReq = SearchRequest.builder()
                .role(String.valueOf(1L))
                .jobTitle(String.valueOf(1L))
                .location("11112")
                .serviceCode("SvcCode1")
                .skill(String.valueOf(1L))
                .userType("1")
                .role("task supervisor,Staff Administrator")
                .build();

        when(caseWorkerProfileRepository.findByCaseWorkerProfiles(any(), any(), any(),
                any(), any(), any(),any()))
                .thenReturn(pages);
        var pageRequest = validateAndBuildPagination(20, 1,
                20, 1);

        ResponseEntity<List<SearchStaffUserResponse>> responseEntity =
                staffRefDataServiceImpl.retrieveStaffProfile(searchReq, pageRequest);
        assertEquals(200, responseEntity.getStatusCodeValue());
        if (responseEntity.getHeaders().get("total-records") != null
                && !responseEntity.getHeaders().get("total-records").isEmpty()) {
            assertThat(responseEntity.getHeaders().get("total-records").get(0)).isEqualTo("1");
        }

        assertThat(responseEntity.getBody()).isNotNull();
        assertTrue(validateSearchUserProfileResponse(responseEntity, searchReq));
    }


    @Test
    void should_return_empty_list_case_worker_profileSearch_with_status_code_200() {

        var pageRequest =
                validateAndBuildPagination(20, 1,
                        20, 1);
        ArrayList<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        Page<CaseWorkerProfile> pages = new PageImpl<>(caseWorkerProfiles);
        searchReq = SearchRequest.builder()
                .build();

        when(caseWorkerProfileRepository.findByCaseWorkerProfiles(any(), any(), any(),
                any(), any(), any(), any()))
                .thenReturn(pages);
        ResponseEntity<List<SearchStaffUserResponse>> responseEntity =
                staffRefDataServiceImpl.retrieveStaffProfile(searchReq, pageRequest);
        assertEquals(200, responseEntity.getStatusCodeValue());
        if (responseEntity.getHeaders().get("total-records") != null
                && !responseEntity.getHeaders().get("total-records").isEmpty()) {
            assertThat(responseEntity.getHeaders().get("total-records").get(0)).isEqualTo("0");
        }

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).isEmpty();
    }


    void validateSearchStaffUserResponses(List<SearchStaffUserResponse> searchResponses) {
        assertThat(searchResponses).hasSize(1);

        SearchStaffUserResponse searchStaffUserResponse = searchResponses.get(0);
        assertThat(searchStaffUserResponse.getFirstName()).isEqualTo("firstName");
        assertThat(searchStaffUserResponse.getLastName()).isEqualTo("Last`name");
        assertThat(searchStaffUserResponse.getEmailId()).isEqualTo("a@b.com");
        assertThat(searchStaffUserResponse.isSuspended()).isTrue();
        assertThat(searchStaffUserResponse.isTaskSupervisor()).isTrue();
        assertThat(searchStaffUserResponse.isCaseAllocator()).isFalse();
        assertThat(searchStaffUserResponse.isStaffAdmin()).isTrue();

        ServiceResponse serviceResponse = searchStaffUserResponse.getServices().get(0);

        assertThat(serviceResponse.getService()).isEqualTo("TestArea");
        assertThat(serviceResponse.getServiceCode()).isEqualTo("SvcCode1");

        assertThat(searchStaffUserResponse.getRegionId()).isEqualTo(111122222);
        assertThat(searchStaffUserResponse.getRegion()).isEqualTo("region");

        Role role = searchStaffUserResponse.getRoles().get(0);

        assertThat(role.getRoleId()).isEqualTo("1");
        assertThat(role.getRoleName()).isEqualTo("testRole1");

        Location location = searchStaffUserResponse.getBaseLocations().get(0);

        assertThat(location.getBaseLocationId()).isEqualTo(11112);
        assertThat(location.getLocationName()).isEqualTo("test location");
        assertThat(searchStaffUserResponse.getUserType()).isEqualTo("userTypeId");

        SkillResponse skillResponse = searchStaffUserResponse.getSkills().get(0);

        assertThat(skillResponse.getSkillId()).isEqualTo(1L);
        assertThat(skillResponse.getDescription()).isEqualTo("desc1");


    }

    void validateSearchStaffUserFalseResponses(List<SearchStaffUserResponse> searchResponses) {
        assertThat(searchResponses).hasSize(1);

        SearchStaffUserResponse searchStaffUserResponse = searchResponses.get(0);
        assertThat(searchStaffUserResponse.getFirstName()).isEqualTo("firstName");
        assertThat(searchStaffUserResponse.getLastName()).isEqualTo("Last`name");
        assertThat(searchStaffUserResponse.getEmailId()).isEqualTo("a@b.com");
        assertThat(searchStaffUserResponse.isSuspended()).isFalse();
        assertThat(searchStaffUserResponse.isTaskSupervisor()).isFalse();
        assertThat(searchStaffUserResponse.isCaseAllocator()).isFalse();
        assertThat(searchStaffUserResponse.isStaffAdmin()).isFalse();

        ServiceResponse serviceResponse = searchStaffUserResponse.getServices().get(0);

        assertThat(serviceResponse.getService()).isEqualTo("TestArea");
        assertThat(serviceResponse.getServiceCode()).isEqualTo("SvcCode1");

        assertThat(searchStaffUserResponse.getRegionId()).isEqualTo(111122222);
        assertThat(searchStaffUserResponse.getRegion()).isEqualTo("region");

        Role role = searchStaffUserResponse.getRoles().get(0);

        assertThat(role.getRoleId()).isEqualTo("1");
        assertThat(role.getRoleName()).isEqualTo("testRole1");

        Location location = searchStaffUserResponse.getBaseLocations().get(0);

        assertThat(location.getBaseLocationId()).isEqualTo(11112);
        assertThat(location.getLocationName()).isEqualTo("test location");
        assertThat(searchStaffUserResponse.getUserType()).isEqualTo("userTypeId");

        SkillResponse skillResponse = searchStaffUserResponse.getSkills().get(0);

        assertThat(skillResponse.getSkillId()).isEqualTo(1L);
        assertThat(skillResponse.getDescription()).isEqualTo("desc1");


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
        userType.setUserTypeId(1L);
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

    uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile buildCaseWorkerProfileForDto() {
        Role
                role = Role.builder()
                .roleId("1")
                .roleName("roleName")
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .isPrimary(true)
                .build();
        Location location = Location
                .builder()
                .baseLocationId(11111)
                .locationName("LocationName")
                .isPrimary(true)
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .build();
        WorkArea workArea = WorkArea.builder()
                .areaOfWork("areaOfWork")
                .serviceCode("serviceCode")
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .build();

        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile caseWorkerProfile =
                uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile.builder()
                        .id("27fbd198-552e-4c32-9caf-37be1545caaf")
                        .firstName("firstName")
                        .lastName("lastName")
                        .officialEmail("a@b.com")
                        .regionName("regionName")
                        .regionId(1)
                        .userType("userType")
                        .userId(11111L)
                        .suspended("false")
                        .createdTime(LocalDateTime.now())
                        .lastUpdatedTime(LocalDateTime.now())
                        .roles(singletonList(role))
                        .locations(singletonList(location))
                        .workAreas(singletonList(workArea))
                        .taskSupervisor("Y")
                        .caseAllocator("N")
                        .build();

        return caseWorkerProfile;
    }

    private CaseWorkerSkill getCaseWorkerSkill() {

        CaseWorkerSkill caseWorkerSkill = new CaseWorkerSkill();
        caseWorkerSkill.setCaseWorkerId("423ec46f-359f-4fcc-9ecc-b1cab4d7e683");
        caseWorkerSkill.setSkillId(1L);
        caseWorkerSkill.setSkill(getSkillData());

        return caseWorkerSkill;

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
    void test_saveStaffProfile() throws JsonProcessingException {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        String body = mapper.writeValueAsString(userProfileCreationResponse);
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);

        when(userProfileFeignClient.createUserProfile(any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(201).build());
        when(caseWorkerProfileRepository.save(any())).thenReturn(caseWorkerProfile);
        staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        verify(caseWorkerProfileRepository, times(1)).save(any());
        verify(jsrValidatorStaffProfile, times(1)).validateStaffProfile(any(), any());
    }

    @Test
    void test_saveStaffProfileValidationAudit() {

        staffProfileAuditService.saveStaffAudit(AuditStatus.SUCCESS, null,
                caseWorkerProfile.getCaseWorkerId(), staffProfileCreationRequest, STAFF_PROFILE_CREATE);
        verify(staffAuditRepository, times(0)).save(any());
    }

    @Test
    void test_saveStaffProfileAlreadyPresent() {
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(caseWorkerProfile);
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest, STAFF_PROFILE_CREATE);
        InvalidRequestException thrown = Assertions.assertThrows(InvalidRequestException.class, () -> {
            staffRefDataServiceImpl.processStaffProfileCreation(staffProfileCreationRequest);
        });

        assertThat(thrown.getMessage()).contains("The profile is already created for the given email Id");
    }

    @Test
    void test_newStaffProfileSuspended() {
        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest, STAFF_PROFILE_CREATE);
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
        caseWorkerProfile = staffRefDataServiceImpl.persistStaffProfile(caseWorkerProfile, staffProfileCreationRequest);
        assertNull(caseWorkerProfile);
    }

    @Test
    void test_persistStaffProfile() {
        when(caseWorkerProfileRepository.save(any())).thenReturn(caseWorkerProfile);
        caseWorkerProfile = staffRefDataServiceImpl.persistStaffProfile(caseWorkerProfile, staffProfileCreationRequest);
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
        when(userProfileFeignClient.createUserProfile(any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(200).build());
        ResponseEntity<Object> response = staffRefDataServiceImpl
                .createUserProfileInIdamUP(staffProfileCreationRequest);
        assertNotNull(response);
    }

    @Test
    void test_createUserProfileInIdamUP_error() throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(500, "Failure", "Method Not Allowed ",
                "Internal Server Error", "2022-01-10");
        String body = mapper.writeValueAsString(errorResponse);
        doReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(500).build())
                .when(userProfileFeignClient).createUserProfile(any(UserProfileCreationRequest.class), anyString());

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl.createUserProfileInIdamUP(staffProfileCreationRequest);
        });
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, thrown.getStatus());
    }

    @Test
    void test_createUserProfileInIdamUP_forbiddenError() throws JsonProcessingException {
        ErrorResponse errorResponse = new ErrorResponse(405, "Failure", "Method Not Allowed ",
                "Method Not Allowed", "2022-01-10");
        String body = mapper.writeValueAsString(errorResponse);

        doReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(405).build())
                .when(userProfileFeignClient).createUserProfile(any(UserProfileCreationRequest.class), anyString());
        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl.createUserProfileInIdamUP(staffProfileCreationRequest);
        });
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, thrown.getStatus());
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

        StaffProfileCreationRequest staffProfileCreationRequest = getStaffProfileUpdateRequest();

        StaffProfileCreationResponse staffProfileCreationResponse = staffRefDataServiceImpl
                .updateStaffProfile(staffProfileCreationRequest);


        assertThat(staffProfileCreationResponse).isNotNull();
        assertThat(staffProfileCreationResponse.getCaseWorkerId()).isEqualTo("CWID1");
    }


    @Test
    void test_populateStaffProfile() throws JsonProcessingException {

        //ValidateStaffProfile
        staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE, null,
                "1234", staffProfileCreationRequest, STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");


        StaffProfileCreationRequest staffProfileCreationRequest = getStaffProfileUpdateRequest();

        staffRefDataServiceImpl.populateStaffProfile(staffProfileCreationRequest, caseWorkerProfile, "CWID1");

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
                "1234", staffProfileCreationRequest, STAFF_PROFILE_UPDATE);

        CaseWorkerProfile caseWorkerProfileInput = new CaseWorkerProfile();
        caseWorkerProfileInput.setCaseWorkerId("CWID1");
        caseWorkerProfileInput.setCaseWorkerLocations(new ArrayList<>());
        caseWorkerProfileInput.setCaseWorkerWorkAreas(new ArrayList<>());
        caseWorkerProfileInput.setCaseWorkerRoles(new ArrayList<>());

        StaffProfileCreationRequest staffProfileCreationRequest = getStaffProfileUpdateRequest();

        CaseWorkerProfile caseWorkerProfile = staffRefDataServiceImpl
                .updateUserProfile(staffProfileCreationRequest, caseWorkerProfileInput);


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
    void test_create_staff_profile_with_changed_values() throws JsonProcessingException {

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);

        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);
        when(caseWorkerProfileRepository.save(any())).thenReturn(caseWorkerProfile);


        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);

        userProfileResponse.setRoles(roles);
        userProfileResponse.setFirstName("testFNChanged");
        userProfileResponse.setLastName("testLNChanged");

        when(userProfileFeignClient.createUserProfile(any(), anyString()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(201).build());

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("12345678");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        roleAdditionResponse.setIdamMessage("success");

        StaffProfileCreationRequest staffProfileCreationRequest = getStaffProfileUpdateRequest();

        StaffProfileCreationResponse staffProfileCreationResponse = staffRefDataServiceImpl
                .processStaffProfileCreation(staffProfileCreationRequest);


        assertThat(staffProfileCreationResponse).isNotNull();
        assertThat(staffProfileCreationResponse.getCaseWorkerId()).isEqualTo("CWID1");
    }


    @Test
    void test_create_staff_profile_with_changed_values_PresentInUP() throws JsonProcessingException {

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID3");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("cwr-func-test-user@test.com");

        when(caseWorkerProfileRepository.findByEmailId(any())).thenReturn(null);

        List<CaseWorkerProfile> caseWorkerProfiles = singletonList(caseWorkerProfile);
        when(caseWorkerProfileRepository.save(any())).thenReturn(caseWorkerProfile);


        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);

        userProfileResponse.setRoles(roles);
        userProfileResponse.setFirstName("testFNChanged");
        userProfileResponse.setLastName("testLNChanged");

        when(userProfileFeignClient.createUserProfile(any(), anyString()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(409).build());


        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("12345678");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        roleAdditionResponse.setIdamMessage("success");

        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(200).build());

        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileRolesResponse),
                                defaultCharset())
                        .status(200).build());

        StaffProfileCreationRequest staffProfileCreationRequest = getStaffProfileUpdateRequest();

        StaffProfileCreationResponse staffProfileCreationResponse = staffRefDataServiceImpl
                .processStaffProfileCreation(staffProfileCreationRequest);


        assertThat(staffProfileCreationResponse).isNotNull();
        assertThat(staffProfileCreationResponse.getCaseWorkerId()).isEqualTo("CWID3");
    }


    @Test
    void staffProfilePendingStatusInUP() throws JsonProcessingException {

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(IDAM_STATUS_NOT_ACTIVE);

        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(200).build());

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl
                    .updateUserRolesInIdam(staffProfileCreationRequest, "1234", STAFF_PROFILE_CREATE);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_NOT_ACTIVE);
    }

    @Test
    void staffProfileDoesNotExitInIdam() throws JsonProcessingException {

        UserProfileResponse userProfileResponse = new UserProfileResponse();

        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(200).build());

        StaffReferenceException thrown = Assertions.assertThrows(StaffReferenceException.class, () -> {
            staffRefDataServiceImpl
                    .updateUserRolesInIdam(staffProfileCreationRequest, "1234", STAFF_PROFILE_CREATE);

        });

        assertThat(thrown.getStatus().value()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(thrown.getErrorDescription()).isEqualTo(IDAM_STATUS_USER_PROFILE);
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

    @Test
    void test_should_Throw_404_When_fetchStaffProfileById_not_found_in_cwp() {
        final String caseWorkerId = "";
        doReturn(Optional.empty())
                .when(caseWorkerProfileRepository).findByCaseWorkerId(any());
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                staffRefDataServiceImpl.fetchStaffProfileById(caseWorkerId));
    }

    @Test
    void should_return_fetchStaffProfileById_with_status_code_200() throws JsonProcessingException {
        doReturn(Optional.of(buildCaseWorkerProfile()))
                .when(caseWorkerProfileRepository).findByCaseWorkerId(
                        "27fbd198-552e-4c32-9caf-37be1545caaf");
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamStatus("Pending");
        when(userProfileFeignClient.getUserProfile(any()))
                .thenReturn(Response.builder().request(mock(Request.class))
                        .body(mapper.writeValueAsString(userProfileResponse), defaultCharset())
                        .status(200).build());
        ResponseEntity<SearchStaffUserByIdResponse> response =
                staffRefDataServiceImpl.fetchStaffProfileById("27fbd198-552e-4c32-9caf-37be1545caaf");
        SearchStaffUserByIdResponse searchStaffUserByIdResponse = response.getBody();

        Contracts.assertNotNull(searchStaffUserByIdResponse);
        assertEquals(1L, searchStaffUserByIdResponse.getSkills().size());
        assertEquals(1L, searchStaffUserByIdResponse.getSkills().get(0).getSkillId());
        assertEquals("desc1", searchStaffUserByIdResponse.getSkills().get(0).getDescription());
        assertEquals("Pending", searchStaffUserByIdResponse.getIdamStatus());
        verify(caseWorkerProfileRepository, times(1)).findByCaseWorkerId(any());
    }

    @Test
    void should_return_fetchStaffProfileById_with_status_code_404_not_found_in_cwp() throws JsonProcessingException {
        doReturn(Optional.of(buildCaseWorkerProfile()))
                .when(caseWorkerProfileRepository).findByCaseWorkerId(
                        "27fbd198-552e-4c32-9caf-37be1545caaf");
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        when(userProfileFeignClient.getUserProfile(any()))
                .thenReturn(Response.builder().request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(),
                                Request.Body.empty(),
                                null))
                        .body(mapper.writeValueAsString(userProfileResponse),
                                defaultCharset())
                        .status(404).build());

        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                staffRefDataServiceImpl.fetchStaffProfileById("27fbd198-552e-4c32-9caf-37be1545caaf"));
    }
}
