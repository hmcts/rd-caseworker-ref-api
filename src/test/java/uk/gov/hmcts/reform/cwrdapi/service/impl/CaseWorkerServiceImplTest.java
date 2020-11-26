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
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.IdamRolesMappingException;
import uk.gov.hmcts.reform.cwrdapi.controllers.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRoleAssocResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.RoleTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.repository.UserTypeRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerServiceImplTest {
    @Mock
    private CaseWorkerProfileRepository caseWorkerProfileRepository;
    @Mock
    private RoleTypeRepository roleTypeRepository;
    @Mock
    private UserTypeRepository userTypeRepository;
    @Mock
    private CaseWorkerIdamRoleAssociationRepository caseWorkerIdamRoleAssociationRepository;
    @Mock
    private UserProfileFeignClient userProfileFeignClient;
    @Mock
    private IdamRoleMappingService idamRoleMappingService;

    private CaseWorkersProfileCreationRequest caseWorkersProfileCreationRequest;
    private RoleType roleType;
    private UserType userType;

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

        caseWorkersProfileCreationRequest = CaseWorkersProfileCreationRequest
                .caseWorkersProfileCreationRequest()
                .deleteFlag(false)
                .emailId("test@test.com")
                .idamRoles(idamRoles)
                .firstName("testFN")
                .lastName("testLN")
                .region("testRegion")
                .userType("testUser1")
                .workerWorkAreaRequests(Collections.singletonList(caseWorkerWorkAreaRequest))
                .baseLocations(Collections.singletonList(caseWorkerLocationRequest))
                .roles(Collections.singletonList(caseWorkerRoleRequest))
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

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileCreationResponse);

        when(roleTypeRepository.findAll()).thenReturn(Collections.singletonList(roleType));
        when(userTypeRepository.findAll()).thenReturn(Collections.singletonList(userType));
        when(caseWorkerProfileRepository.findByEmailId(caseWorkersProfileCreationRequest.getEmailId()))
                .thenReturn(null);
        when(userProfileFeignClient.createUserProfile(any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(201).build());

        ResponseEntity<Object> objectResponseEntity = caseWorkerServiceImpl
                .processCaseWorkerProfiles(
                        Collections.singletonList(caseWorkersProfileCreationRequest));

        verify(caseWorkerProfileRepository, times(1)).saveAll(any());

        assertThat(objectResponseEntity.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void testDeleteCwUserProfile() throws JsonProcessingException {

        CaseWorkerProfile profile = new CaseWorkerProfile();
        profile.setCaseWorkerId("1");
        profile.setDeleteFlag(true);
        profile.setEmailId(caseWorkersProfileCreationRequest.getEmailId());

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileCreationResponse);

        when(caseWorkerProfileRepository.findByEmailId(caseWorkersProfileCreationRequest.getEmailId()))
                .thenReturn(profile);

        when(userProfileFeignClient.modifyUserRoles(any(),any(),any()))
                .thenReturn(Response.builder()
                .request(Request.create(Request.HttpMethod.POST, "", new HashMap<>(), Request.Body.empty(),
                        null)).body(body, Charset.defaultCharset()).status(200).build());

        ResponseEntity<Object> objectResponseEntity = caseWorkerServiceImpl
                .processCaseWorkerProfiles(
                        Collections.singletonList(caseWorkersProfileCreationRequest));

        verify(caseWorkerProfileRepository, times(0)).saveAll(any());
        verify(userProfileFeignClient, times(1)).modifyUserRoles(any(),any(),any());
        verify(caseWorkerProfileRepository, times(1)).findByEmailId(any());
        assertThat(objectResponseEntity.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void test_buildIdamRoleMappings_success() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
                .serivceId("BA11")
                .idamRoles("role1")
                .roleId(1)
                .build();

        IdamRoleAssocResponse idamRoleAssocResponse = caseWorkerServiceImpl
                .buildIdamRoleMappings(Collections.singletonList(serviceRoleMapping));

        Set<String> serviceCode = new HashSet<>();
        serviceCode.add(serviceRoleMapping.getSerivceId());

        assertThat(idamRoleAssocResponse.getStatusCode()).isEqualTo(201);
        assertThat(idamRoleAssocResponse.getMessage())
                .isEqualTo(CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS +
                        serviceCode.toString());
    }

    @Test(expected = IdamRolesMappingException.class)
    public void test_buildIdamRoleMappings_exception() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder().build();
        doThrow(new RuntimeException("Exception message"))
                .when(idamRoleMappingService).buildIdamRoleAssociation(any());
        caseWorkerServiceImpl.buildIdamRoleMappings(Collections.singletonList(serviceRoleMapping));
    }
}