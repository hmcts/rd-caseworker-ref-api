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
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private CaseWorkersProfileCreationRequest cwProfileCreationRequest;
    private RoleType roleType;
    private UserType userType;

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
        when(caseWorkerProfileRepository.findByEmailId(cwProfileCreationRequest.getEmailId()))
            .thenReturn(null);
        when(caseWorkerIdamRoleAssociationRepository.findByRoleTypeInAndServiceCodeIn(any(), any()))
                .thenReturn(new ArrayList<>());

        when(userProfileFeignClient.createUserProfile(any())).thenReturn(Response.builder()
            .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(201).build());

        caseWorkerServiceImpl
            .processCaseWorkerProfiles(
                singletonList(cwProfileCreationRequest));

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
                Charset.defaultCharset())
            .status(409).build());
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
            .thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                    null)).body(mapper.writeValueAsString(userProfileResponse),
                    Charset.defaultCharset())
                .status(200).build());

        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
            .thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                    null)).body(mapper.writeValueAsString(userProfileRolesResponse),
                    Charset.defaultCharset())
                .status(200).build());

        when(caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes()).thenReturn(singletonList(roleType));
        when(caseWorkerStaticValueRepositoryAccessorImpl.getUserTypes()).thenReturn(singletonList(userType));
        when(caseWorkerProfileRepository.findByEmailId(cwProfileCreationRequest.getEmailId()))
            .thenReturn(null);
        when(caseWorkerProfileRepository.saveAll(any()))
            .thenReturn(singletonList(new CaseWorkerProfile()));

        List<CaseWorkerProfile> savedProfiles = caseWorkerServiceImpl
            .processCaseWorkerProfiles(
                singletonList(cwProfileCreationRequest));

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

        when(caseWorkerProfileRepository.findByEmailId(cwProfileCreationRequest.getEmailId()))
            .thenReturn(profile);

        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
            .thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                    null)).body(mapper.writeValueAsString(userProfileRolesResponse),
                    Charset.defaultCharset())
                .status(200).build());

        caseWorkerServiceImpl
            .processCaseWorkerProfiles(
                singletonList(cwProfileCreationRequest));

        verify(caseWorkerProfileRepository, times(1)).saveAll(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
        verify(caseWorkerProfileRepository, times(1)).findByEmailId(any());
        cwProfileCreationRequest.setSuspended(false);
    }

    @Test
    public void test_buildIdamRoleMappings_success() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
            .serviceId("BA11")
            .idamRoles("role1")
            .roleId(1)
            .build();

        IdamRolesMappingResponse idamRolesMappingResponse = caseWorkerServiceImpl
            .buildIdamRoleMappings(singletonList(serviceRoleMapping));

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
        verify(topicPublisher, times(2)).sendMessage(any());
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

        String userProfileResponseBody = mapper.writeValueAsString(userProfileResponse);
        when(caseWorkerStaticValueRepositoryAccessorImpl.getRoleTypes()).thenReturn(singletonList(roleType));
        when(caseWorkerStaticValueRepositoryAccessorImpl.getUserTypes()).thenReturn(singletonList(userType));
        when(caseWorkerProfileRepository.findByEmailId(cwProfileCreationRequest.getEmailId()))
            .thenReturn(profile);
        when(userProfileFeignClient.getUserProfileWithRolesById(any()))
            .thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                    null)).body(userProfileResponseBody, Charset.defaultCharset())
                .status(200).build());

        String userProfileRolesResponseBody = mapper.writeValueAsString(userProfileRolesResponse);
        when(userProfileFeignClient.modifyUserRoles(any(), any(), any()))
            .thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                    null)).body(userProfileRolesResponseBody, Charset.defaultCharset())
                .status(200).build());

        caseWorkerServiceImpl.processCaseWorkerProfiles(singletonList(cwProfileCreationRequest));

        verify(userProfileFeignClient, times(1)).getUserProfileWithRolesById(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(), any(), any());
        verify(caseWorkerProfileRepository, times(1)).saveAll(any());
        verify(caseWorkerProfileRepository, times(1)).findByEmailId(any());
        verify(caseWorkerLocationRepository, times(1)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerWorkAreaRepository, times(1)).deleteByCaseWorkerProfileIn(any());
        verify(caseWorkerRoleRepository, times(1)).deleteByCaseWorkerProfileIn(any());

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

}