package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileCreationResponseTest {

    private final UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
    private final String testUuid = UUID.randomUUID().toString();

    @Test
    void test_has_mandatory_fields_specified_not_null() {
        userProfileCreationResponse.setIdamId(testUuid);
        userProfileCreationResponse.setIdamRegistrationResponse(201);

        assertThat(userProfileCreationResponse.getIdamId()).isEqualTo(testUuid);
        assertThat(userProfileCreationResponse.getIdamRegistrationResponse()).isEqualTo(201);
    }
}