package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRolesMappingResponse;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CaseWorkerRefControllerTest {

    @InjectMocks
    private CaseWorkerRefController caseWorkerRefController;

    CaseWorkerService caseWorkerServiceMock;

    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequest = new ArrayList<>();
    CaseWorkersProfileCreationRequest cwRequest;
    CaseWorkerProfileCreationResponse cwProfileCreationResponse;
    CaseWorkerProfileCreationResponse cwResponse;
    ResponseEntity<Object> responseEntity;

    @Before
    public void setUp() throws Exception {

        caseWorkerServiceMock = mock(CaseWorkerService.class);
        cwResponse = new CaseWorkerProfileCreationResponse("Case Worker Profiles Created.");
        responseEntity = new ResponseEntity<>(
                cwResponse,
                null,
                HttpStatus.OK
        );
        Set<String> roles = new HashSet<>();
        roles.add("tribunal_case_worker");

        List<CaseWorkerRoleRequest> caseWorkerRoleRequests = new ArrayList<>();
        CaseWorkerRoleRequest cwRoleRequest = new CaseWorkerRoleRequest("role", true);
        caseWorkerRoleRequests.add(cwRoleRequest);

        List<CaseWorkerLocationRequest> caseWorkeLocationRequests = new ArrayList<>();

        CaseWorkerLocationRequest cwLocRequest = new CaseWorkerLocationRequest(1,"location",
                true);
        caseWorkeLocationRequests.add(cwLocRequest);

        List<CaseWorkerWorkAreaRequest> caseWorkeAreaRequests = new ArrayList<>();
        CaseWorkerWorkAreaRequest cwAreaRequest = new CaseWorkerWorkAreaRequest("areaOfwork",
                "serviceCode");
        caseWorkeAreaRequests.add(cwAreaRequest);

        cwRequest = new CaseWorkersProfileCreationRequest("firstName",
                "lastName","test@gmail.com",1,"userType","region",
                false,roles,caseWorkerRoleRequests,caseWorkeLocationRequests,caseWorkeAreaRequests);
        caseWorkersProfileCreationRequest.add(cwRequest);
        cwProfileCreationResponse = new CaseWorkerProfileCreationResponse("");
        MockitoAnnotations.openMocks(this);

    }

    @Test
    public void createCaseWorkerProfilesTest() {

        when(caseWorkerServiceMock.processCaseWorkerProfiles(caseWorkersProfileCreationRequest))
             .thenReturn(responseEntity);
        ResponseEntity<?> actual = caseWorkerRefController.createCaseWorkerProfiles(caseWorkersProfileCreationRequest);

        assertNotNull(actual);
        verify(caseWorkerServiceMock,times(1))
                .processCaseWorkerProfiles(caseWorkersProfileCreationRequest);
    }

    @Test(expected = InvalidRequestException.class)
    public void createCaseWorkerProfilesShouldThrow400() {

        caseWorkersProfileCreationRequest = null;
        ResponseEntity<?> actual = caseWorkerRefController.createCaseWorkerProfiles(caseWorkersProfileCreationRequest);
    }

    @Test
    public void test_buildIdamRoleMappings_success() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
                .serivceId("BA11")
                .idamRoles("role1")
                .roleId(1)
                .build();

        IdamRolesMappingResponse idamRolesMappingResponse = new IdamRolesMappingResponse(200, "testMessage");
        when(caseWorkerServiceMock.buildIdamRoleMappings(Collections.singletonList(serviceRoleMapping)))
                .thenReturn(idamRolesMappingResponse);
        ResponseEntity<Object> actual = caseWorkerRefController
                .buildIdamRoleMappings(Collections.singletonList(serviceRoleMapping));

        assertNotNull(actual);
        assertThat(actual.getStatusCodeValue()).isEqualTo(200);
        verify(caseWorkerServiceMock,times(1))
                .buildIdamRoleMappings(Collections.singletonList(serviceRoleMapping));
    }

    @Test(expected = InvalidRequestException.class)
    public void should_throw_exception_for_invalid_request() {

        caseWorkerRefController
                .buildIdamRoleMappings(Collections.emptyList());

        verify(caseWorkerServiceMock,times(0))
                .buildIdamRoleMappings(anyList());
    }
}
