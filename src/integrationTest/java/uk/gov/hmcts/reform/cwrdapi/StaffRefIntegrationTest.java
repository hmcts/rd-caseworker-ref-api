package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

class StaffRefIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @BeforeEach
    public void setUpClient() {
        super.setUpClient();
    }

    @Test
    void returns_200_when_users_retrieved_successfully() throws JsonProcessingException {
        //TODO:
    }
}
