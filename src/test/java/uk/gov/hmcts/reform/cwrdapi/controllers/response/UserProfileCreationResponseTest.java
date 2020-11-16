package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UserProfileCreationResponseTest {

    private UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
    private String testUuid = UUID.randomUUID().toString();

    @Test
    public void test_has_mandatory_fields_specified_not_null() {
        userProfileCreationResponse.setIdamId(testUuid);
        userProfileCreationResponse.setIdamRegistrationResponse(201);

        assertThat(userProfileCreationResponse.getIdamId()).isEqualTo(testUuid);
        assertThat(userProfileCreationResponse.getIdamRegistrationResponse()).isEqualTo(201);
    }
}