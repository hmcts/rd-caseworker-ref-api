package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaseWorkerProfileCreationResponseTest {

    @Test
    void test_has_mandatory_fields_specified_not_null() {
        List<String> ids = new ArrayList<>();
        ids.add("1234");
        CaseWorkerProfileCreationResponse response = new CaseWorkerProfileCreationResponse();
        response.setCaseWorkerRegistrationResponse("response");

        assertThat(response).isNotNull();
        assertThat(response.getCaseWorkerRegistrationResponse()).isEqualTo("response");

        CaseWorkerProfileCreationResponse response1 =
                new CaseWorkerProfileCreationResponse("response",
                        "response details", ids);
        assertThat(response1).isNotNull();
        assertThat(response1.getCaseWorkerRegistrationResponse()).isEqualTo("response");
        assertThat(response1.getMessageDetails()).isEqualTo("response details");
        assertThat(response1.getCaseWorkerIds().get(0)).isEqualTo("1234");
    }
}


