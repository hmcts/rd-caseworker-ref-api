package uk.gov.hmcts.reform.cwrdapi;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.Map;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_NUMBER;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.PAGE_SIZE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.SORT_DIRECTION;

public class FetchStaffProfileByCcdServiceNamesIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Before
    public void setUpClient() {
        super.setUpClient();
    }

    @BeforeClass
    public static void setup() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @AfterClass
    public static void tearDown() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @Test
    public void shouldReturn400ForEmptyServiceName() {
        Map<String, Object> response = caseworkerReferenceDataClient
                .fetchStaffProfileByCcdServiceName("", null, null,
                        "", "", "cwd-system-user");
        assertThat(response).containsEntry("http_status", "400");
        assertTrue(response.get("response_body").toString()
                .contains("Required String parameter 'ccd_service_names' is not present"));
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
