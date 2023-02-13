package uk.gov.hmcts.reform.cwrdapi;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.Map;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_CWD_SYSTEM_USER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SORT_DIRECTION;

public class FetchStaffProfileByCcdServiceNamesIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @BeforeEach
    public void setUpClient() {
        super.setUpClient();
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
    public void shouldReturn400ForEmptyServiceName() {
        mockJwtToken(ROLE_CWD_SYSTEM_USER);
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
        mockJwtToken(ROLE_CWD_SYSTEM_USER);
        Map<String, Object> response = caseworkerReferenceDataClient
                .fetchStaffProfileByCcdServiceName("cmc", -1, null,
                        "", "", "cwd-system-user");
        assertThat(response).containsEntry("http_status", "400");
        assertTrue(response.get("response_body").toString()
                .contains(String.format(INVALID_FIELD, PAGE_SIZE)));
    }

    @Test
    public void shouldReturn400ForInvalidPageNumber() {
        mockJwtToken(ROLE_CWD_SYSTEM_USER);
        Map<String, Object> response = caseworkerReferenceDataClient
                .fetchStaffProfileByCcdServiceName("cmc", 1, -1,
                        "", "", "cwd-system-user");
        assertThat(response).containsEntry("http_status", "400");
        assertTrue(response.get("response_body").toString()
                .contains(String.format(INVALID_FIELD, PAGE_NUMBER)));
    }

    @Test
    public void shouldReturn400ForInvalidSortDirection() {
        mockJwtToken(ROLE_CWD_SYSTEM_USER);
        Map<String, Object> response = caseworkerReferenceDataClient
                .fetchStaffProfileByCcdServiceName("cmc", 1, 1,
                        "Invalid", "", "cwd-system-user");
        assertThat(response).containsEntry("http_status", "400");
        assertTrue(response.get("response_body").toString()
                .contains(String.format(INVALID_FIELD, SORT_DIRECTION)));
    }
}
