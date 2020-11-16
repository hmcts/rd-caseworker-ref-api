package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CaseWorkerRefControllerTest {

    @InjectMocks
    private CaseWorkerRefController caseWorkerRefController;

    CaseWorkerService caseWorkerServiceMock;

    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequest = new ArrayList<>();
    CaseWorkersProfileCreationRequest cwRequest;
    CaseWorkerProfileCreationResponse cwProfileCreationResponse;

    @Before
    public void setUp() throws Exception {

        caseWorkerServiceMock = mock(CaseWorkerService.class);

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

    @Ignore
    @Test
    public void createCaseWorkerProfilesTest() {

        //when(caseWorkerServiceMock.createCaseWorkerUserProfiles(caseWorkersProfileCreationRequest)).thenReturn();
        ResponseEntity<?> actual = caseWorkerRefController.createCaseWorkerProfiles(caseWorkersProfileCreationRequest);

        verify(caseWorkerServiceMock,times(1))
                .saveOrUpdateOrDeleteCaseWorkerUserProfiles(caseWorkersProfileCreationRequest);
    }
}
