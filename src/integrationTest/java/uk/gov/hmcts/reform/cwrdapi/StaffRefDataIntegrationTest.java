package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserType;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserTypesResponse;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.List;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("AbbreviationAsWordInName")
public class StaffRefDataIntegrationTest extends AuthorizationEnabledIntegrationTest {

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
    void should_getUserType_return_status_code_404()
            throws JsonProcessingException {
        String path = "/user-type";
        String role = "staff-admin";
        final var refDataUserTypesResponse = (StaffRefDataUserTypesResponse) caseworkerReferenceDataClient
                .retrieveAllServiceSkills(StaffRefDataUserTypesResponse.class, path, role);
        assertThat(refDataUserTypesResponse).isNotNull();

        List<StaffRefDataUserType> userTypes = refDataUserTypesResponse.getUserTypes();

        assertThat(userTypes).isNotNull();

        StaffRefDataUserType type = userTypes.get(0);

        assertThat(type.getId()).isNotNull();

        assertThat(type.getCode()).isNotNull();
    }

}