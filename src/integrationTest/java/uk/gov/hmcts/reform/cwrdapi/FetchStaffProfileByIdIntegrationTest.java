package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserByIdResponse;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.Map;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.UpdateStaffReferenceProfileTest.ROLE_STAFF_ADMIN;

public class FetchStaffProfileByIdIntegrationTest extends AuthorizationEnabledIntegrationTest {

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
    void should_return_status_code_404_when_profile_not_found_in_cw() throws JsonProcessingException {
        String searchString = "?id=123";
        String path = "/profile/search-by-id";

        Map<String, Object> response = (Map<String, Object>) caseworkerReferenceDataClient
            .fetchStaffUserById(SearchStaffUserByIdResponse.class, path + searchString, ROLE_STAFF_ADMIN);
        assertThat(response).containsEntry("http_status", "404");
        assertThat(response.get("response_body").toString()).contains(" is invalid");
    }




}
