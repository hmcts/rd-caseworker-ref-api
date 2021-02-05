package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateCaseWorkerProfilesIntegrationTest extends AuthorizationEnabledIntegrationTest {

    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests;

    @Before
    public void setUpClient() {

        super.setUpClient();

        //Set up Case worker data
        Set<String> roles = ImmutableSet.of("tribunal_case_worker");
        List<CaseWorkerRoleRequest> caseWorkerRoleRequests =
            ImmutableList.of(CaseWorkerRoleRequest.caseWorkerRoleRequest().role("role").isPrimaryFlag(true).build());

        List<CaseWorkerLocationRequest> caseWorkerLocationRequests = ImmutableList.of(CaseWorkerLocationRequest
            .caseWorkersLocationRequest()
            .isPrimaryFlag(true).locationId(1)
            .location("location").build());

        List<CaseWorkerWorkAreaRequest> caseWorkerAreaRequests = ImmutableList.of(CaseWorkerWorkAreaRequest
            .caseWorkerWorkAreaRequest()
            .areaOfWork("areaOfWork").serviceCode("serviceCode")
            .build());

        caseWorkersProfileCreationRequests = ImmutableList.of(CaseWorkersProfileCreationRequest
            .caseWorkersProfileCreationRequest()
            .firstName(" firstName ").lastName(" lastName ").emailId("test@gmail.com").regionId(1).userType("userType")
            .region("region").suspended(false).roles(caseWorkerRoleRequests).idamRoles(roles)
            .baseLocations(caseWorkerLocationRequests).workerWorkAreaRequests(caseWorkerAreaRequests).build());
    }

    @Test
    public void shouldCreateCaseWorker() throws IOException {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> response = caseworkerReferenceDataClient
            .createCaseWorkerProfile(caseWorkersProfileCreationRequests, "cwd-admin");
        assertThat(response).containsEntry("http_status", "201 CREATED");
    }
}
