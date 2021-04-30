package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.client.domain.AttributeResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.IdamRolesMappingException;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfilesDeletionResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRolesMappingResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_STATUS_SUSPENDED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.STATUS_ACTIVE;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerServiceImplTest {
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
    private IdamRoleMappingService idamRoleMappingService;
    @Mock
    private TopicPublisher topicPublisher;
    @Mock
    private CwrdCommonRepository cwrdCommonRepository;
    @Mock
    private CaseWorkerStaticValueRepositoryAccessorImpl caseWorkerStaticValueRepositoryAccessorImpl;

    @Mock
    IValidationService validationServiceFacade;

    public static final String COMMON_EMAIL_PATTERN = "CWR-func-test-user";

    private CaseWorkersProfileCreationRequest cwProfileCreationRequest;
    private RoleType roleType;
    private UserType userType;
    private CaseWorkerProfile caseWorkerProfile;

    ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private CaseWorkerServiceImpl caseWorkerServiceImpl;

    @Before
    public void setUp() {
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
                .emailId("test@test.com")
                .idamRoles(idamRoles)
                .firstName("testFN")
                .lastName("testLN")
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
    public void test_saveCwUserProfile() throws JsonProcessingException {
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

        when(userProfileFeignClient.createUserProfile(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, defaultCharset()).status(201).build());

        List<CaseWorkersProfileCreationRequest> requests = new ArrayList<>();
        requests.add(cwProfileCreationRequest);
        caseWorkerServiceImpl.processCaseWorkerProfiles(requests);

        verify(caseWorkerProfileRepository, times(1)).saveAll(any());
        verify(caseWorkerIdamRoleAssociationRepository, times(1)).findByRoleTypeInAndServiceCodeIn(any(), any());
    }

    @Test
    public void test_409WhileCwUserProfileCreation() throws JsonProcessingException {
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

        when(userProfileFeignClient.createUserProfile(any())).thenReturn(Response.builder()
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
        verify(caseWorkerProfileRepository, times(1)).saveAll(any());

        //Todo update error

    }

    @Test
    public void testSuspendCwUserProfile() throws JsonProcessingException {

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
    public void test_buildIdamRoleMappings_success() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
                .serviceId("BA11")
                .idamRoles("role1")
                .roleId(1)
                .build();
        List<ServiceRoleMapping> serviceRoleMappingList = singletonList(serviceRoleMapping);
        IdamRolesMappingResponse idamRolesMappingResponse = caseWorkerServiceImpl
                .buildIdamRoleMappings(serviceRoleMappingList);

        Set<String> serviceCode = new HashSet<>();
        serviceCode.add(serviceRoleMapping.getServiceId());

        assertThat(idamRolesMappingResponse.getStatusCode()).isEqualTo(201);
        assertThat(idamRolesMappingResponse.getMessage())
                .isEqualTo(CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS + serviceCode.toString());
    }

    @Test(expected = IdamRolesMappingException.class)
    public void test_buildIdamRoleMappings_exception() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder().roleId(1).build();
        doThrow(new RuntimeException("Exception message"))
                .when(idamRoleMappingService).buildIdamRoleAssociation(any());
        caseWorkerServiceImpl.buildIdamRoleMappings(singletonList(serviceRoleMapping));
    }

    @Test
    public void test_publishCaseWorkerDataToTopic() {
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


    @Test(expected = ResourceNotFoundException.class)
    public void test_shouldThrow404WhenCaseworker_profile_not_found() {
        doReturn(emptyList())
                .when(caseWorkerProfileRepository).findByCaseWorkerIdIn(any());
        caseWorkerServiceImpl.fetchCaseworkersById(singletonList(""));
    }

    @Test
    public void test_should_return_caseworker_profile() {
        doReturn(singletonList(buildCaseWorkerProfile()))
                .when(caseWorkerProfileRepository).findByCaseWorkerIdIn(singletonList(
                "27fbd198-552e-4c32-9caf-37be1545caaf"));
        caseWorkerServiceImpl.fetchCaseworkersById(
                singletonList("27fbd198-552e-4c32-9caf-37be1545caaf"));
        verify(caseWorkerProfileRepository, times(1)).findByCaseWorkerIdIn(any());
    }

    public CaseWorkerProfile buildCaseWorkerProfile() {

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
        caseWorkerProfile.setCreatedDate(LocalDateTime.now());
        caseWorkerProfile.setLastUpdate(LocalDateTime.now());

        caseWorkerProfile.setCaseWorkerId("27fbd198-552e-4c32-9caf-37be1545caaf");
        caseWorkerProfile.setCaseWorkerRoles(singletonList(caseWorkerRole));
        caseWorkerProfile.setCaseWorkerLocations(singletonList(caseWorkerLocation));
        return caseWorkerProfile;
    }


    public uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile buildCaseWorkerProfileForDto() {
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
                        .build();

        return caseWorkerProfile;
    }

    @Test
    public void testUpdateRole() throws JsonProcessingException {

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
    public void updateRoleFailsForInvalidResponseFromIdam_Scenario1() throws JsonProcessingException {

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
    public void updateRoleFailsForInvalidResponseFromIdam_Scenario2() throws JsonProcessingException {

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
    public void updateRoleFailsForInvalidResponseFromIdam_Scenario3() throws JsonProcessingException {

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
    public void updateRoleFailsForInvalidResponseFromIdam_Scenario4() throws JsonProcessingException {

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
    public void updateRoleFailsForInvalidResponseFromIdam_Scenario5() throws JsonProcessingException {

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
    public void testMapCaseWorkerProfileRequest() {
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
    }

    @Test
    public void testDeleteUserProfileByUserId() {
        Response responseMock = mock(Response.class);

        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();
        deletionResponse.setMessage("Case Worker Profiles successfully deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(userProfileFeignClient.deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null))
                .thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NO_CONTENT.value());
        when(caseWorkerProfileRepository.findByCaseWorkerId(any(String.class)))
                .thenReturn(Optional.ofNullable(caseWorkerProfile));

        CaseWorkerProfilesDeletionResponse deletionResp =
                caseWorkerServiceImpl.deleteByUserId(caseWorkerProfile.getCaseWorkerId());

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(caseWorkerProfileRepository, times(1)).findByCaseWorkerId(any(String.class));
        verify(caseWorkerProfileRepository, times(1)).deleteAll(any());
        verify(responseMock, times(2)).status();
        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null);
    }

    @Test
    public void testDeleteUserProfileByUserId_WhenUpReturns404() {
        Response responseMock = mock(Response.class);

        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();
        deletionResponse.setMessage("Case Worker Profiles successfully deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(userProfileFeignClient.deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null))
                .thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NOT_FOUND.value());
        when(caseWorkerProfileRepository.findByCaseWorkerId(any(String.class)))
                .thenReturn(Optional.ofNullable(caseWorkerProfile));

        CaseWorkerProfilesDeletionResponse deletionResp =
                caseWorkerServiceImpl.deleteByUserId(caseWorkerProfile.getCaseWorkerId());

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(caseWorkerProfileRepository, times(1)).findByCaseWorkerId(any(String.class));
        verify(caseWorkerProfileRepository, times(1)).deleteAll(any());
        verify(responseMock, times(3)).status();
        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null);
    }

    @Test
    public void testDeleteUserProfileByUserId_WhenUpReturnsError() {
        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();
        deletionResponse.setMessage("UP Delete request failed for userId: " + caseWorkerProfile.getCaseWorkerId()
                + ". With the following UP message: INTERNAL SERVER ERROR");
        deletionResponse.setStatusCode(BAD_REQUEST.value());

        Response responseMock = mock(Response.class);

        when(userProfileFeignClient.deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null))
                .thenReturn(responseMock);
        when(responseMock.status()).thenReturn(BAD_REQUEST.value());

        CaseWorkerProfilesDeletionResponse deletionResp =
                caseWorkerServiceImpl.deleteByUserId(caseWorkerProfile.getCaseWorkerId());

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).contains("UP Delete request failed for userId");

        verify(responseMock, times(3)).status();
        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null);
        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(caseWorkerProfile.getCaseWorkerId(), null);
    }

    @Test
    public void testDeleteUserProfileByEmailPattern() {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(caseWorkerProfile);

        Response responseMock = mock(Response.class);

        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();
        deletionResponse.setMessage("Case Worker Profiles successfully deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(userProfileFeignClient.deleteUserProfile(null, COMMON_EMAIL_PATTERN))
                .thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NO_CONTENT.value());
        when(caseWorkerProfileRepository
                .findByEmailIdIgnoreCaseContaining(COMMON_EMAIL_PATTERN)).thenReturn(caseWorkerProfiles);

        CaseWorkerProfilesDeletionResponse deletionResp =
                caseWorkerServiceImpl.deleteByEmailPattern(COMMON_EMAIL_PATTERN);

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(null, COMMON_EMAIL_PATTERN);
        verify(responseMock, times(1)).status();
        verify(caseWorkerProfileRepository, times(1))
                .findByEmailIdIgnoreCaseContaining(any(String.class));
        verify(caseWorkerProfileRepository, times(1)).deleteAll(any());
    }

    @Test
    public void testDeleteUserProfileByEmailPattern_WhenUpReturns404() {
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(caseWorkerProfile);

        Response responseMock = mock(Response.class);

        CaseWorkerProfilesDeletionResponse deletionResponse = new CaseWorkerProfilesDeletionResponse();
        deletionResponse.setMessage("Case Worker Profiles successfully deleted");
        deletionResponse.setStatusCode(NO_CONTENT.value());

        when(userProfileFeignClient.deleteUserProfile(null, COMMON_EMAIL_PATTERN))
                .thenReturn(responseMock);
        when(responseMock.status()).thenReturn(NOT_FOUND.value());
        when(caseWorkerProfileRepository
                .findByEmailIdIgnoreCaseContaining(COMMON_EMAIL_PATTERN)).thenReturn(caseWorkerProfiles);

        CaseWorkerProfilesDeletionResponse deletionResp =
                caseWorkerServiceImpl.deleteByEmailPattern(COMMON_EMAIL_PATTERN);

        assertThat(deletionResp.getStatusCode()).isEqualTo(deletionResponse.getStatusCode());
        assertThat(deletionResp.getMessage()).isEqualTo(deletionResponse.getMessage());

        verify(userProfileFeignClient, times(1))
                .deleteUserProfile(null, COMMON_EMAIL_PATTERN);
        verify(responseMock, times(2)).status();
        verify(caseWorkerProfileRepository, times(1))
                .findByEmailIdIgnoreCaseContaining(any(String.class));
        verify(caseWorkerProfileRepository, times(1)).deleteAll(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateUserAfterUpDeleteWhenStatusIs204() {
        Optional<CaseWorkerProfile> userProfile = mock(Optional.class);
        String userId = UUID.randomUUID().toString();

        when(userProfile.isPresent()).thenReturn(false);

        CaseWorkerProfilesDeletionResponse deletionResponse =
                caseWorkerServiceImpl.validateUserAfterUpDelete(userProfile, userId, 204);

        assertThat(deletionResponse.getStatusCode()).isEqualTo(204);
        assertThat(deletionResponse.getMessage())
                .isEqualTo("User deleted in UP but was not present in CRD with userId: " + userId);

        verify(userProfile, times(1)).isPresent();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testValidateUserAfterUpDeleteWhenStatusIsNot204() {
        Optional<CaseWorkerProfile> userProfile = mock(Optional.class);
        String userId = UUID.randomUUID().toString();

        when(userProfile.isPresent()).thenReturn(false);

        CaseWorkerProfilesDeletionResponse deletionResponse =
                caseWorkerServiceImpl.validateUserAfterUpDelete(userProfile, userId, 404);

        assertThat(deletionResponse.getStatusCode()).isEqualTo(404);
        assertThat(deletionResponse.getMessage())
                .isEqualTo("User was not present in UP or CRD with userId: " + userId);

        verify(userProfile, times(1)).isPresent();
    }

}