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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.AttributeResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Skill;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.IdamRolesMappingException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.StaffReferenceException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.LocationReferenceDataFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRolesMappingResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.LrdOrgInfoServiceResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerLocationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerRoleRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerWorkAreaRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CwrdCommonRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.RequestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_SUSPENDED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STATUS_ACTIVE;

@ExtendWith(MockitoExtension.class)
class CaseWorkerServiceImplTest {
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
    private LocationReferenceDataFeignClient locationReferenceDataFeignClient;
    @Mock
    private IdamRoleMappingService idamRoleMappingService;
    @Mock
    private TopicPublisher topicPublisher;
    @Mock
    private CwrdCommonRepository cwrdCommonRepository;
    @Mock
    private CaseWorkerStaticValueRepositoryAccessorImpl caseWorkerStaticValueRepositoryAccessorImpl;
    @Mock
    IValidationService validationServiceFacade;

    static final String COMMON_EMAIL_PATTERN = "CWR-func-test-user";

    private CaseWorkersProfileCreationRequest cwProfileCreationRequest;
    private RoleType roleType;
    private UserType userType;
    private CaseWorkerProfile caseWorkerProfile;

    ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private CaseWorkerServiceImpl caseWorkerServiceImpl;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Set<String> idamRoles = new HashSet<>();
        idamRoles.add("IdamRole1");
        idamRoles.add("IdamRole2");

        CaseWorkerRoleRequest caseWorkerRoleRequest =
                new CaseWorkerRoleRequest("testRole", true);

        CaseWorkerLocationRequest caseWorkerLocationRequest = CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true)
                .location("testLocation")
                .locationId(1)
                .build();

        CaseWorkerWorkAreaRequest caseWorkerWorkAreaRequest = CaseWorkerWorkAreaRequest
                .caseWorkerWorkAreaRequest()
                .areaOfWork("testAOW")
                .serviceCode("testServiceCode")
                .build();

        cwProfileCreationRequest = CaseWorkersProfileCreationRequest
                .caseWorkersProfileCreationRequest()
                .suspended(false)
                .caseAllocator(false)
                .taskSupervisor(true)
                .emailId("test@test.com")
                .idamRoles(idamRoles)
                .firstName("testFN")
                .lastName("testLN")
                .regionId(1)
                .region("testRegion")
                .userType("testUser1")
                .workerWorkAreaRequests(singletonList(caseWorkerWorkAreaRequest))
                .baseLocations(singletonList(caseWorkerLocationRequest))
                .roles(singletonList(caseWorkerRoleRequest))
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
    }

    @Test
    void test_saveCwUserProfile() throws JsonProcessingException {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        String body = mapper.writeValueAsString(userProfileCreationResponse);
        when(caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes()).thenReturn(singletonList(roleType));
        when(caseWorkerStaticValueRepositoryAccessorImpl.getUserTypes()).thenReturn(singletonList(userType));
        when(caseWorkerProfileRepository.findByEmailIdIn(any()))
                .thenReturn(new ArrayList<>());
        when(caseWorkerIdamRoleAssociationRepository.findByRoleTypeInAndServiceCodeIn(any(), any()))
                .thenReturn(new ArrayList<>());

        when(userProfileFeignClient.createUserProfile(any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(201).build());

        List<CaseWorkersProfileCreationRequest> requests = new ArrayList<>();
        requests.add(cwProfileCreationRequest);
        caseWorkerServiceImpl.processCaseWorkerProfiles(requests);

        verify(caseWorkerProfileRepository, times(1)).saveAll(any());
        verify(caseWorkerIdamRoleAssociationRepository, times(1)).findByRoleTypeInAndServiceCodeIn(any(), any());
    }

    @Test
    void test_setNewCaseWorkerProfileFlag() {
        CaseWorkerProfile caseWorkerProfileMock = mock(CaseWorkerProfile.class);
        caseWorkerProfileMock.setCaseWorkerId("CWID1");
        caseWorkerServiceImpl.setNewCaseWorkerProfileFlag(caseWorkerProfileMock);
        assertThat(caseWorkerProfileMock).isNotNull();

        verify(caseWorkerProfileMock, times(1)).setNew(true);

    }

    @Test
    void test_409WhileCwUserProfileCreation() throws JsonProcessingException {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("12345678");
        userProfileCreationResponse.setIdamRegistrationResponse(1);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("12345678");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("12345678");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        roleAdditionResponse.setIdamMessage("success");

        when(userProfileFeignClient.createUserProfile(any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(mapper.writeValueAsString(userProfileCreationResponse),
                        defaultCharset())
                .status(409).build());
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

        when(caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes()).thenReturn(singletonList(roleType));
        when(caseWorkerStaticValueRepositoryAccessorImpl.getUserTypes()).thenReturn(singletonList(userType));
        when(caseWorkerProfileRepository.findByEmailIdIn(any())).thenReturn(new ArrayList<>());
        when(caseWorkerProfileRepository.saveAll(any())).thenReturn(singletonList(new CaseWorkerProfile()));
        List<CaseWorkersProfileCreationRequest> requests = new ArrayList<>();
        requests.add(cwProfileCreationRequest);
        List<CaseWorkerProfile> savedProfiles = caseWorkerServiceImpl.processCaseWorkerProfiles(requests);

        assertThat(savedProfiles).isNotEmpty();
        Assertions.assertFalse(roleAdditionResponse.getIdamMessage().isEmpty());
        verify(caseWorkerProfileRepository, times(1)).saveAll(any());

        //Todo update error

    }

    @Test
    void testSuspendCwUserProfile() throws JsonProcessingException {

        cwProfileCreationRequest.setSuspended(true);
        CaseWorkerProfile profile = new CaseWorkerProfile();
        profile.setCaseWorkerId("1");
        profile.setSuspended(false);
        profile.setEmailId(cwProfileCreationRequest.getEmailId());


        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        AttributeResponse attributeResponse = new AttributeResponse();
        attributeResponse.setIdamStatusCode(HttpStatus.OK.value());
        userProfileRolesResponse.setAttributeResponse(attributeResponse);

        when(caseWorkerProfileRepository.findByEmailIdIn(any()))
                .thenReturn(Arrays.asList(profile));

        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(mapper.writeValueAsString(userProfileRolesResponse),
                                defaultCharset())
                        .status(200).build());

        List<CaseWorkersProfileCreationRequest> requests = new ArrayList<>();
        requests.add(cwProfileCreationRequest);
        caseWorkerServiceImpl.processCaseWorkerProfiles(requests);

        verify(caseWorkerProfileRepository, times(1)).saveAll(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
        verify(caseWorkerProfileRepository, times(1)).findByEmailIdIn(any());
        cwProfileCreationRequest.setSuspended(false);
    }

    @Test
    void test_buildIdamRoleMappings_success() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
                .serviceId("BA11")
                .idamRoles("role1")
                .roleId(1)
                .build();
        ServiceRoleMapping serviceRoleMapping1 = ServiceRoleMapping.builder()
                .serviceId("BA12")
                .idamRoles("role2")
                .roleId(2)
                .build();

        List<ServiceRoleMapping> serviceRoleMappingList = new ArrayList<>();
        serviceRoleMappingList.add(serviceRoleMapping);
        serviceRoleMappingList.add(serviceRoleMapping1);
        IdamRolesMappingResponse idamRolesMappingResponse = caseWorkerServiceImpl
                .buildIdamRoleMappings(serviceRoleMappingList);

        Set<String> serviceCode = new HashSet<>();
        serviceCode.add(serviceRoleMapping.getServiceId());
        serviceCode.add(serviceRoleMapping1.getServiceId());

        assertThat(idamRolesMappingResponse.getStatusCode()).isEqualTo(201);
        assertThat(idamRolesMappingResponse.getMessage())
                .isEqualTo(CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS + serviceCode.toString());
    }

    @Test
    void test_buildIdamRoleMappings_exception() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder().roleId(1).build();
        List<ServiceRoleMapping> serviceRoleMappingList = singletonList(serviceRoleMapping);
        doThrow(new RuntimeException("Exception message"))
                .when(idamRoleMappingService).buildIdamRoleAssociation(any());
        Assertions.assertThrows(IdamRolesMappingException.class, () ->
                caseWorkerServiceImpl.buildIdamRoleMappings(serviceRoleMappingList));
    }

    @Test
    void test_publishCaseWorkerDataToTopic() {
        ReflectionTestUtils.setField(caseWorkerServiceImpl, "caseWorkerDataPerMessage", 1);
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("1234");

        CaseWorkerProfile secondCaseWorkerProfile = new CaseWorkerProfile();
        secondCaseWorkerProfile.setCaseWorkerId("1234");

        caseWorkerProfiles.add(caseWorkerProfile);
        caseWorkerProfiles.add(secondCaseWorkerProfile);
        caseWorkerServiceImpl.publishCaseWorkerDataToTopic(caseWorkerProfiles);
        verify(topicPublisher, times(1)).sendMessage(any());
    }


    @Test
    void test_shouldThrow404WhenCaseworker_profile_not_found() {
        final List<String> caseWorkerIds = singletonList("");

        doReturn(emptyList())
                .when(caseWorkerProfileRepository).findByCaseWorkerIdIn(any());
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                caseWorkerServiceImpl.fetchCaseworkersById(caseWorkerIds));
    }

    @Test
    void test_should_return_caseworker_profile() {
        doReturn(singletonList(buildCaseWorkerProfile()))
                .when(caseWorkerProfileRepository).findByCaseWorkerIdIn(singletonList(
                        "27fbd198-552e-4c32-9caf-37be1545caaf"));
        caseWorkerServiceImpl.fetchCaseworkersById(
                singletonList("27fbd198-552e-4c32-9caf-37be1545caaf"));
        assertNotNull(caseWorkerServiceImpl
                .fetchCaseworkersById(singletonList("27fbd198-552e-4c32-9caf-37be1545caaf")));
        verify(caseWorkerProfileRepository, times(2)).findByCaseWorkerIdIn(any());
    }

    CaseWorkerProfile buildCaseWorkerProfile() {

        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setCaseWorkerRoleId(1L);
        caseWorkerRole.setCaseWorkerId("CWID1");
        caseWorkerRole.setRoleId(1L);
        caseWorkerRole.setPrimaryFlag(false);
        caseWorkerRole.setCreatedDate(LocalDateTime.now());
        caseWorkerRole.setLastUpdate(LocalDateTime.now());
        caseWorkerRole.setRoleType(roleType);

        CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setCaseWorkerId("CWID1");
        caseWorkerLocation.setCaseWorkerLocationId(11111L);
        caseWorkerLocation.setCreatedDate(LocalDateTime.now());
        caseWorkerLocation.setLastUpdate(LocalDateTime.now());
        caseWorkerLocation.setLocationId(11112);
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
        caseWorkerProfile.setCreatedDate(LocalDateTime.now());
        caseWorkerProfile.setLastUpdate(LocalDateTime.now());

        caseWorkerProfile.setCaseWorkerId("27fbd198-552e-4c32-9caf-37be1545caaf");
        caseWorkerProfile.setCaseWorkerRoles(singletonList(caseWorkerRole));
        caseWorkerProfile.setCaseWorkerLocations(singletonList(caseWorkerLocation));
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
        Skill skill = Skill.builder()
                .skillId(1L)
                .skillCode("1")
                .description("testSkills")
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
                        .staffAdmin("false")
                        .createdTime(LocalDateTime.now())
                        .lastUpdatedTime(LocalDateTime.now())
                        .roles(singletonList(role))
                        .skills(singletonList(skill))
                        .locations(singletonList(location))
                .workAreas(singletonList(workArea))
                .taskSupervisor("Y")
                .caseAllocator("N")
                .build();

        return caseWorkerProfile;
    }

    @Test
    void testUpdateRole() throws JsonProcessingException {

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("1");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("1");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        CaseWorkerProfile profile = new CaseWorkerProfile();
        profile.setCaseWorkerId("1");
        profile.setEmailId(cwProfileCreationRequest.getEmailId());
        profile.setSuspended(false);

        List<CaseWorkerProfile> profiles = new ArrayList<>();
        profiles.add(profile);

        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        when(caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes()).thenReturn(singletonList(roleType));
        when(caseWorkerStaticValueRepositoryAccessorImpl.getUserTypes()).thenReturn(singletonList(userType));
        when(caseWorkerProfileRepository.findByEmailIdIn(any())).thenReturn(profiles);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset()).status(200).build());

        String userProfileRolesResponseBody = mapper.writeValueAsString(userProfileRolesResponse);
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileRolesResponseBody, defaultCharset())
                        .status(200).build());

        List<CaseWorkersProfileCreationRequest> requests = new ArrayList<>();
        requests.add(cwProfileCreationRequest);
        caseWorkerServiceImpl.processCaseWorkerProfiles(requests);

        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
        verify(caseWorkerProfileRepository, times(1)).saveAll(any());
        verify(caseWorkerProfileRepository, times(1)).findByEmailIdIn(any());
        verify(caseWorkerLocationRepository, times(1)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerWorkAreaRepository, times(1)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerRoleRepository, times(1)).deleteByCaseWorkerProfileIn(any());

    }

    @Test
    void updateRoleFailsForInvalidResponseFromIdam_Scenario1() throws JsonProcessingException {

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("1");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(IDAM_STATUS_SUSPENDED);
        userProfileResponse.setRoles(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("1");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        CaseWorkerProfile profile = new CaseWorkerProfile();
        profile.setCaseWorkerId("1");
        profile.setEmailId(cwProfileCreationRequest.getEmailId());
        Set<String> emails = new HashSet<>();
        emails.add(cwProfileCreationRequest.getEmailId());
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        when(caseWorkerProfileRepository.findByEmailIdIn(emails)).thenReturn(Arrays.asList(profile));
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());

        List<CaseWorkersProfileCreationRequest> requests = new ArrayList<>();
        requests.add(cwProfileCreationRequest);
        caseWorkerServiceImpl.processCaseWorkerProfiles(requests);

        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(0)).modifyUserRoles(any(), any(), any());
        verify(caseWorkerProfileRepository, times(0)).saveAll(any());
        verify(caseWorkerProfileRepository, times(1)).findByEmailIdIn(any());
        verify(caseWorkerLocationRepository, times(0)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerWorkAreaRepository, times(0)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerRoleRepository, times(0)).deleteByCaseWorkerProfileIn(any());

    }

    @Test
    void updateRoleFailsForInvalidResponseFromIdam_Scenario2() throws JsonProcessingException {

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("1");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("1");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("400");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        CaseWorkerProfile profile = new CaseWorkerProfile();
        profile.setCaseWorkerId("1");
        profile.setEmailId(cwProfileCreationRequest.getEmailId());
        profile.setSuspended(false);


        when(caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes()).thenReturn(singletonList(roleType));
        Set<String> emails = new HashSet<>();
        emails.add(cwProfileCreationRequest.getEmailId());
        when(caseWorkerProfileRepository.findByEmailIdIn(emails)).thenReturn(Arrays.asList(profile));
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());

        String userProfileRolesResponseBody = mapper.writeValueAsString(userProfileRolesResponse);
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileRolesResponseBody, defaultCharset())
                        .status(200).build());

        List<CaseWorkersProfileCreationRequest> requests = new ArrayList<>();
        requests.add(cwProfileCreationRequest);
        caseWorkerServiceImpl.processCaseWorkerProfiles(requests);

        verify(caseWorkerProfileRepository, times(1)).findByEmailIdIn(any());
        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());

        verify(caseWorkerProfileRepository, times(0)).saveAll(any());
        verify(caseWorkerLocationRepository, times(0)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerWorkAreaRepository, times(0)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerRoleRepository, times(0)).deleteByCaseWorkerProfileIn(any());

    }

    @Test
    void updateRoleFailsForInvalidResponseFromIdam_Scenario3() throws JsonProcessingException {

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("1");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);
        CaseWorkerProfile profile = new CaseWorkerProfile();
        profile.setCaseWorkerId("1");
        profile.setEmailId(cwProfileCreationRequest.getEmailId());
        profile.setSuspended(false);

        Set<String> emails = new HashSet<>();
        emails.add(cwProfileCreationRequest.getEmailId());
        when(caseWorkerProfileRepository.findByEmailIdIn(emails)).thenReturn(Arrays.asList(profile));
        when(caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes()).thenReturn(singletonList(roleType));

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("1");
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());

        String userProfileRolesResponseBody = mapper.writeValueAsString(userProfileRolesResponse);
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileRolesResponseBody, defaultCharset())
                        .status(200).build());
        List<CaseWorkersProfileCreationRequest> requests = new ArrayList<>();
        requests.add(cwProfileCreationRequest);
        caseWorkerServiceImpl.processCaseWorkerProfiles(requests);

        verify(caseWorkerProfileRepository, times(1)).findByEmailIdIn(any());
        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());

        verify(caseWorkerProfileRepository, times(0)).saveAll(any());
        verify(caseWorkerLocationRepository, times(0)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerWorkAreaRepository, times(0)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerRoleRepository, times(0)).deleteByCaseWorkerProfileIn(any());

    }

    @Test
    void updateRoleFailsForInvalidResponseFromIdam_Scenario4() throws JsonProcessingException {

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("1");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("1");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        CaseWorkerProfile profile = new CaseWorkerProfile();
        profile.setCaseWorkerId("1");
        profile.setEmailId(cwProfileCreationRequest.getEmailId());
        profile.setSuspended(false);

        when(caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes()).thenReturn(singletonList(roleType));
        Set<String> emails = new HashSet<>();
        emails.add(cwProfileCreationRequest.getEmailId());
        when(caseWorkerProfileRepository.findByEmailIdIn(emails)).thenReturn(Arrays.asList(profile));
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(Optional.empty().toString(), defaultCharset())
                        .status(200).build());

        List<CaseWorkersProfileCreationRequest> requests = new ArrayList<>();
        requests.add(cwProfileCreationRequest);
        caseWorkerServiceImpl.processCaseWorkerProfiles(requests);

        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
        verify(caseWorkerProfileRepository, times(0)).saveAll(any());
        verify(caseWorkerProfileRepository, times(1)).findByEmailIdIn(any());
        verify(caseWorkerLocationRepository, times(0)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerWorkAreaRepository, times(0)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerRoleRepository, times(0)).deleteByCaseWorkerProfileIn(any());


    }

    @Test
    void updateRoleFailsForInvalidResponseFromIdam_Scenario5() throws JsonProcessingException {

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("1");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileCreationResponse.setIdamId("1");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        CaseWorkerProfile profile = new CaseWorkerProfile();
        profile.setCaseWorkerId("1");
        profile.setEmailId(cwProfileCreationRequest.getEmailId());
        profile.setSuspended(false);

        when(caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes()).thenReturn(singletonList(roleType));
        Set<String> emails = new HashSet<>();
        emails.add(cwProfileCreationRequest.getEmailId());
        when(caseWorkerProfileRepository.findByEmailIdIn(emails)).thenReturn(Arrays.asList(profile));
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());


        doThrow(new RuntimeException()).when(userProfileFeignClient).modifyUserRoles(any(), any(), any());
        List<CaseWorkersProfileCreationRequest> requests = new ArrayList<>();
        requests.add(cwProfileCreationRequest);
        caseWorkerServiceImpl.processCaseWorkerProfiles(requests);

        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
        verify(caseWorkerProfileRepository, times(0)).saveAll(any());
        verify(caseWorkerProfileRepository, times(1)).findByEmailIdIn(any());
        verify(caseWorkerLocationRepository, times(0)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerWorkAreaRepository, times(0)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerRoleRepository, times(0)).deleteByCaseWorkerProfileIn(any());


    }

    @Test
    void testMapCaseWorkerProfileRequest() {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId("1");
        CaseWorkerProfile caseWorkerProfile = caseWorkerServiceImpl.mapCaseWorkerProfileRequest(
                "1", cwProfileCreationRequest, new CaseWorkerProfile());

        assertThat(caseWorkerProfile.getCaseWorkerId()).isEqualTo("1");
        assertThat(caseWorkerProfile.getFirstName()).isEqualTo(cwProfileCreationRequest.getFirstName());
        assertThat(caseWorkerProfile.getLastName()).isEqualTo(cwProfileCreationRequest.getLastName());
        assertThat(caseWorkerProfile.getEmailId()).isEqualTo(cwProfileCreationRequest.getEmailId());
        assertThat(caseWorkerProfile.getSuspended()).isFalse();
        assertThat(caseWorkerProfile.getUserTypeId()).isZero();
        assertThat(caseWorkerProfile.getRegionId()).isEqualTo(cwProfileCreationRequest.getRegionId());
        assertThat(caseWorkerProfile.getRegion()).isEqualTo(cwProfileCreationRequest.getRegion());
        assertThat(caseWorkerProfile.getCaseAllocator()).isEqualTo(cwProfileCreationRequest.isCaseAllocator());
        assertThat(caseWorkerProfile.getTaskSupervisor()).isEqualTo(cwProfileCreationRequest.isTaskSupervisor());
    }

    @Test
    void testRefreshRoleAllocationReturns200() throws JsonProcessingException {
        LrdOrgInfoServiceResponse lrdOrgInfoServiceResponse = new LrdOrgInfoServiceResponse();
        lrdOrgInfoServiceResponse.setServiceCode("BAA1");
        lrdOrgInfoServiceResponse.setCcdServiceName("cmc");
        String body = mapper.writeValueAsString(List.of(lrdOrgInfoServiceResponse));

        when(locationReferenceDataFeignClient.getLocationRefServiceMapping("cmc"))
                .thenReturn(Response.builder()
                        .request(mock(Request.class)).body(body, defaultCharset()).status(201).build());


        CaseWorkerProfile caseWorkerProfile = buildCaseWorkerProfile();

        PageRequest pageRequest = RequestUtils.validateAndBuildPaginationObject(0, 1,
                "caseWorkerId", "ASC",
                20, "id", CaseWorkerProfile.class);

        PageImpl<CaseWorkerProfile> page = new PageImpl<>(Collections.singletonList(caseWorkerProfile));
        when(caseWorkerProfileRepository.findByServiceCodeIn(Set.of("BAA1"), pageRequest))
                .thenReturn(page);
        ResponseEntity<Object> responseEntity = caseWorkerServiceImpl
                .fetchStaffProfilesForRoleRefresh("cmc", pageRequest);

        assertEquals(200, responseEntity.getStatusCodeValue());

    }

    @Test
    void testRefreshRoleAllocationWhenLrdResponseIsNon200() {

        PageRequest pageRequest = RequestUtils.validateAndBuildPaginationObject(0, 1,
                "caseWorkerId", "ASC",
                20, "id", CaseWorkerProfile.class);
        when(locationReferenceDataFeignClient.getLocationRefServiceMapping("cmc"))
                .thenReturn(Response.builder()
                        .request(mock(Request.class)).body("body", defaultCharset()).status(400).build());

        Assertions.assertThrows(StaffReferenceException.class, () ->
                caseWorkerServiceImpl
                        .fetchStaffProfilesForRoleRefresh("cmc", pageRequest));

    }


    @Test
    void testRefreshRoleAllocationWhenCrdResponseIsEmpty() throws JsonProcessingException {

        LrdOrgInfoServiceResponse lrdOrgInfoServiceResponse = new LrdOrgInfoServiceResponse();
        lrdOrgInfoServiceResponse.setServiceCode("BAA1");
        lrdOrgInfoServiceResponse.setCcdServiceName("cmc");
        String body = mapper.writeValueAsString(List.of(lrdOrgInfoServiceResponse));

        when(locationReferenceDataFeignClient.getLocationRefServiceMapping("cmc"))
                .thenReturn(Response.builder()
                        .request(mock(Request.class)).body(body, defaultCharset()).status(201).build());

        PageRequest pageRequest = RequestUtils.validateAndBuildPaginationObject(0, 1,
                "caseWorkerId", "ASC",
                20, "id", CaseWorkerProfile.class);

        PageImpl<CaseWorkerProfile> page = new PageImpl<>(Collections.emptyList());
        when(caseWorkerProfileRepository.findByServiceCodeIn(Set.of("BAA1"), pageRequest))
                .thenReturn(page);
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
                caseWorkerServiceImpl
                        .fetchStaffProfilesForRoleRefresh("cmc", pageRequest));
    }

    @Test
    void testRefreshRoleAllocationWhenLrdResponseIsEmpty() throws JsonProcessingException {

        String body = mapper.writeValueAsString(Collections.emptyList());

        when(locationReferenceDataFeignClient.getLocationRefServiceMapping("cmc"))
                .thenReturn(Response.builder()
                        .request(mock(Request.class)).body(body, defaultCharset()).status(201).build());


        List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();

        PageRequest pageRequest = RequestUtils.validateAndBuildPaginationObject(0, 1,
                "caseWorkerId", "ASC",
                20, "id", CaseWorkerProfile.class);

        Assertions.assertThrows(StaffReferenceException.class, () ->
                caseWorkerServiceImpl
                        .fetchStaffProfilesForRoleRefresh("cmc", pageRequest));
    }

    @Test
    void testRefreshRoleAllocationWhenLrdResponseReturns400() throws JsonProcessingException {
        ErrorResponse errorResponse = ErrorResponse
                .builder()
                .errorCode(400)
                .errorDescription("testErrorDesc")
                .errorMessage("testErrorMsg")
                .build();
        String body = mapper.writeValueAsString(errorResponse);

        when(locationReferenceDataFeignClient.getLocationRefServiceMapping("cmc"))
                .thenReturn(Response.builder()
                        .request(mock(Request.class)).body(body, defaultCharset()).status(400).build());


        PageRequest pageRequest = RequestUtils.validateAndBuildPaginationObject(0, 1,
                "caseWorkerId", "ASC",
                20, "id", CaseWorkerProfile.class);

        Assertions.assertThrows(StaffReferenceException.class, () ->
                caseWorkerServiceImpl
                        .fetchStaffProfilesForRoleRefresh("cmc", pageRequest));
    }

    @Test
    void testUpdateUserProfile() {
        List<CaseWorkerRole> caseWorkerRoles = new ArrayList<>();
        caseWorkerRoles.add(new CaseWorkerRole());
        List<CaseWorkerWorkArea> caseWorkerWorkAreas = new ArrayList<>();
        caseWorkerWorkAreas.add(new CaseWorkerWorkArea());
        List<CaseWorkerLocation> caseWorkerLocations = new ArrayList<>();
        caseWorkerLocations.add(new CaseWorkerLocation());

        caseWorkerProfile.setCaseWorkerWorkAreas(caseWorkerWorkAreas);
        caseWorkerProfile.setCaseWorkerRoles(caseWorkerRoles);
        caseWorkerProfile.setCaseWorkerLocations(caseWorkerLocations);

        CaseWorkerProfile actualUpdatedUser = caseWorkerServiceImpl
                .updateUserProfile(cwProfileCreationRequest, caseWorkerProfile);

        assertEquals(cwProfileCreationRequest
                .getWorkerWorkAreaRequests().size(), actualUpdatedUser.getCaseWorkerWorkAreas().size());
        assertEquals(cwProfileCreationRequest.getBaseLocations().size(),
                actualUpdatedUser.getCaseWorkerLocations().size());
        assertNotEquals(cwProfileCreationRequest.getRoles().size(), actualUpdatedUser.getCaseWorkerRoles().size());
    }

    @Test
    void testNamesMismatch_Sc1() throws JsonProcessingException {

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setFirstName("Fname");
        userProfileResponse.setLastName("Lname");
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);
        cwProfileCreationRequest.setFirstName("Fname");
        cwProfileCreationRequest.setLastName("Lname");
        Set<String> idamroles = new HashSet<>(Arrays.asList("IdamRole1", "IdamRole4", "IdamRole2"));
        cwProfileCreationRequest.setIdamRoles(idamroles);
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        String userProfileRolesResponseBody = mapper.writeValueAsString(userProfileRolesResponse);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileRolesResponseBody, defaultCharset())
                        .status(200).build());
        caseWorkerServiceImpl.updateUserRolesInIdam(cwProfileCreationRequest, "1");
        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
    }

    @Test
    void testNamesMismatch_Sc2() throws JsonProcessingException {

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setFirstName("Fname");
        userProfileResponse.setLastName("Lname");
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);
        cwProfileCreationRequest.setFirstName("Fname");
        cwProfileCreationRequest.setLastName("Lname");
        Set<String> idamroles = new HashSet<>(Arrays.asList());
        cwProfileCreationRequest.setIdamRoles(idamroles);
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        String userProfileRolesResponseBody = mapper.writeValueAsString(userProfileRolesResponse);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());
        caseWorkerServiceImpl.updateUserRolesInIdam(cwProfileCreationRequest, "1");
        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(0)).modifyUserRoles(any(), any(), any());

    }

    @Test
    void testNamesMismatch_Sc3() throws JsonProcessingException {

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setFirstName("Fname");
        userProfileResponse.setLastName("Lname");
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);
        cwProfileCreationRequest.setFirstName("F1name");
        cwProfileCreationRequest.setLastName("L2name");
        Set<String> idamroles = new HashSet<>(Arrays.asList("IdamRole1", "IdamRole4"));
        cwProfileCreationRequest.setIdamRoles(idamroles);
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        String userProfileRolesResponseBody = mapper.writeValueAsString(userProfileRolesResponse);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileRolesResponseBody, defaultCharset())
                        .status(200).build());
        caseWorkerServiceImpl.updateUserRolesInIdam(cwProfileCreationRequest, "1");
        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
    }

    @Test
    void testNamesMismatch_Sc4() throws JsonProcessingException {

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode("201");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setFirstName("Fname");
        userProfileResponse.setLastName("Lname");
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);
        cwProfileCreationRequest.setFirstName("Fname");
        cwProfileCreationRequest.setLastName("L2name");
        Set<String> idamroles = new HashSet<>(Arrays.asList("IdamRole1", "IdamRole4"));
        cwProfileCreationRequest.setIdamRoles(idamroles);
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        String userProfileRolesResponseBody = mapper.writeValueAsString(userProfileRolesResponse);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileRolesResponseBody, defaultCharset())
                        .status(200).build());
        caseWorkerServiceImpl.updateUserRolesInIdam(cwProfileCreationRequest, "1");
        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
    }

    @Test
    void testNamesMismatch_Sc5() throws JsonProcessingException {

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        AttributeResponse attributeResponse = new AttributeResponse();
        attributeResponse.setIdamStatusCode(HttpStatus.OK.value());
        userProfileRolesResponse.setRoleAdditionResponse(null);
        userProfileRolesResponse.setAttributeResponse(attributeResponse);
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setFirstName("Fname");
        userProfileResponse.setLastName("Lname");
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);
        cwProfileCreationRequest.setFirstName("Fname");
        cwProfileCreationRequest.setLastName("L2name");
        Set<String> idamroles = new HashSet<>(Arrays.asList("IdamRole1", "IdamRole4"));
        cwProfileCreationRequest.setIdamRoles(idamroles);
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        String userProfileRolesResponseBody = mapper.writeValueAsString(userProfileRolesResponse);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileRolesResponseBody, defaultCharset())
                        .status(200).build());
        caseWorkerServiceImpl.updateUserRolesInIdam(cwProfileCreationRequest, "1");
        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
    }

    @Test
    void testNamesMismatch_Sc6() throws JsonProcessingException {

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        AttributeResponse attributeResponse = new AttributeResponse();
        attributeResponse.setIdamStatusCode(400);
        userProfileRolesResponse.setRoleAdditionResponse(null);
        userProfileRolesResponse.setAttributeResponse(attributeResponse);
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setFirstName("Fname");
        userProfileResponse.setLastName("Lname");
        userProfileResponse.setIdamId("1");
        List<String> roles = Arrays.asList("IdamRole1", "IdamRole4");
        userProfileResponse.setIdamStatus(STATUS_ACTIVE);
        userProfileResponse.setRoles(roles);
        cwProfileCreationRequest.setFirstName("Fname");
        cwProfileCreationRequest.setLastName("L2name");
        Set<String> idamroles = new HashSet<>(Arrays.asList("IdamRole1", "IdamRole4"));
        cwProfileCreationRequest.setIdamRoles(idamroles);
        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        String userProfileRolesResponseBody = mapper.writeValueAsString(userProfileRolesResponse);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileResponseBody, defaultCharset())
                        .status(200).build());
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
                .thenReturn(Response.builder()
                        .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                                null)).body(userProfileRolesResponseBody, defaultCharset())
                        .status(200).build());
        assertEquals(false, caseWorkerServiceImpl.updateUserRolesInIdam(cwProfileCreationRequest, "1"));
    }
}