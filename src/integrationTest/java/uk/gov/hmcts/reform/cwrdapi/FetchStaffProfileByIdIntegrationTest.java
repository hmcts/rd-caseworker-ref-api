package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;
import org.testcontainers.shaded.com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserByIdResponse;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.CreateStaffReferenceProfileBasicSearchTest.ROLE_CWD_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.UpdateStaffReferenceProfileTest.ROLE_STAFF_ADMIN;

public class FetchStaffProfileByIdIntegrationTest extends AuthorizationEnabledIntegrationTest {

    List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests;


    @BeforeEach
    public void setUpClient() {
        super.setUpClient();
        Set<String> roles = ImmutableSet.of(" tribunal_case_worker ");
        List<CaseWorkerRoleRequest> caseWorkerRoleRequests =
                ImmutableList.of(CaseWorkerRoleRequest.caseWorkerRoleRequest().role(" role ").isPrimaryFlag(true)
                        .build());

        List<CaseWorkerLocationRequest> caseWorkerLocationRequests = ImmutableList.of(CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true).locationId(1)
                .location(" location ").build());

        List<CaseWorkerWorkAreaRequest> caseWorkerAreaRequests = ImmutableList.of(CaseWorkerWorkAreaRequest
                .caseWorkerWorkAreaRequest()
                .areaOfWork(" areaOfWork ").serviceCode(" serviceCode ")
                .build());

        caseWorkersProfileCreationRequests = ImmutableList.of(CaseWorkersProfileCreationRequest
                .caseWorkersProfileCreationRequest()
                .firstName(" firstName ")
                .lastName(" lastName ")
                .emailId("test.inttest@hmcts.gov.uk")
                .regionId(1).userType("CTSC")
                .region("region")
                .suspended(false)
                .taskSupervisor(true)
                .caseAllocator(false)
                .roles(caseWorkerRoleRequests)
                .idamRoles(roles)
                .baseLocations(caseWorkerLocationRequests)
                .workerWorkAreaRequests(caseWorkerAreaRequests)
                .build());
    }

    @BeforeAll
    public static void setup() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @AfterAll
    public static void tearDown() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @Test
    void should_return_status_code_404_when_profile_not_found_in_cw() throws JsonProcessingException {
        String searchString = "?id=123";
        String path = "/profile/search-by-id";

        Map<String, Object> response = (Map<String, Object>) caseworkerReferenceDataClient
                .fetchStaffUserById(SearchStaffUserByIdResponse.class, path + searchString, ROLE_STAFF_ADMIN);
        assertThat(response).containsEntry("http_status", HttpStatus.NOT_FOUND);
        assertThat(((ErrorResponse)response.get("response_body")).getErrorDescription())
                .contains(CaseWorkerConstants.NO_DATA_FOUND);
    }


    @Test
    void should_return_status_code_404_when_profile_not_found_in_up() throws JsonProcessingException {
        caseworkerReferenceDataClient.setBearerToken(EMPTY);
        Map<String, Object> createResponse = caseworkerReferenceDataClient
                .createCaseWorkerProfile(caseWorkersProfileCreationRequests, ROLE_CWD_ADMIN);
        assertThat(createResponse).containsEntry("http_status", "201 CREATED");
        String caseWorkerId = ((List)((Map)createResponse.get("body")).get("case_worker_ids")).get(0).toString();
        Assertions.assertNotNull(caseWorkerId);

        String path = "/profile/search-by-id";
        String searchString = "?id=" + caseWorkerId;
        caseworkerReferenceDataClient.setBearerToken(null);
        userProfileGetUserByIdWireMock(caseWorkerId, 404);
        Map<String, Object> getResponse = (Map<String, Object>) caseworkerReferenceDataClient
                .fetchStaffUserById(SearchStaffUserByIdResponse.class, path + searchString, ROLE_STAFF_ADMIN);
        assertThat(getResponse).containsEntry("http_status", HttpStatus.NOT_FOUND);
        assertThat(((ErrorResponse)getResponse.get("response_body")).getErrorDescription())
                .contains(CaseWorkerConstants.NO_DATA_FOUND);
    }

    @Test
    void should_return_status_code_200_when_profile_found() throws JsonProcessingException {
        Map<String, Object> createResponse = caseworkerReferenceDataClient
                .createCaseWorkerProfile(caseWorkersProfileCreationRequests, ROLE_CWD_ADMIN);
        assertThat(createResponse).containsEntry("http_status", "201 CREATED");
        String caseWorkerId = ((List)((Map)createResponse.get("body")).get("case_worker_ids")).get(0).toString();
        Assertions.assertNotNull(caseWorkerId);
        String path = "/profile/search-by-id";
        String searchString = "?id=" + caseWorkerId;
        caseworkerReferenceDataClient.setBearerToken(null);

        userProfileGetUserByIdWireMock(caseWorkerId, 200);
        SearchStaffUserByIdResponse getResponse =  (SearchStaffUserByIdResponse)caseworkerReferenceDataClient
                .fetchStaffUserById(SearchStaffUserByIdResponse.class, path + searchString, ROLE_STAFF_ADMIN);
        Assertions.assertNotNull(getResponse);
        Assertions.assertEquals(caseWorkerId, getResponse.getCaseWorkerId());
        Assertions.assertNotNull(getResponse.getIdamStatus());
    }

}