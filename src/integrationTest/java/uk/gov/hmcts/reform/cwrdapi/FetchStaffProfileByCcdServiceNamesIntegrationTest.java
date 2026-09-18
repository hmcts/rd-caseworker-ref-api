package uk.gov.hmcts.reform.cwrdapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.wiremock.WireMockTestEnvironment;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SORT_DIRECTION;
import static uk.gov.hmcts.reform.cwrdapi.wiremock.IdamWireMockStubs.stubIdamWithGivenRoleAndStatus;

public class FetchStaffProfileByCcdServiceNamesIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private static WireMockServer idamMockServer = WireMockTestEnvironment.idam();
    private static StubMapping cwdSystemUserStub = null;

    @BeforeEach
    public void setUpClient() {
        super.setUpClient();
        cwdSystemUserStub = stubIdamWithGivenRoleAndStatus(idamMockServer, "active", List.of("cwd-system-user"));
    }

    @AfterAll
    public static void cleanUp() {
        idamMockServer.removeStub(cwdSystemUserStub);
    }

    @Test
    public void shouldReturn400ForEmptyServiceName() {
        Map<String, Object> response = caseworkerReferenceDataClient
                .fetchStaffProfileByCcdServiceName("", null, null,
                        "", "", "cwd-system-user");
        assertThat(response).containsEntry("http_status", "400");
        assertTrue(response.get("response_body").toString()
                .contains("Required request parameter 'ccd_service_names'"
                        + " for method parameter type String is not present"));
    }

    @Test
    public void shouldReturn400ForInvalidPageSize() {
        Map<String, Object> response = caseworkerReferenceDataClient
                .fetchStaffProfileByCcdServiceName("cmc", -1, null,
                        "", "", "cwd-system-user");
        assertThat(response).containsEntry("http_status", "400");
        assertTrue(response.get("response_body").toString()
                .contains(String.format(INVALID_FIELD, PAGE_SIZE)));
    }

    @Test
    public void shouldReturn400ForInvalidPageNumber() {
        Map<String, Object> response = caseworkerReferenceDataClient
                .fetchStaffProfileByCcdServiceName("cmc", 1, -1,
                        "", "", "cwd-system-user");
        assertThat(response).containsEntry("http_status", "400");
        assertTrue(response.get("response_body").toString()
                .contains(String.format(INVALID_FIELD, PAGE_NUMBER)));
    }

    @Test
    public void shouldReturn400ForInvalidSortDirection() {
        Map<String, Object> response = caseworkerReferenceDataClient
                .fetchStaffProfileByCcdServiceName("cmc", 1, 1,
                        "Invalid", "", "cwd-system-user");
        assertThat(response).containsEntry("http_status", "400");
        assertTrue(response.get("response_body").toString()
                .contains(String.format(INVALID_FIELD, SORT_DIRECTION)));
    }
}
