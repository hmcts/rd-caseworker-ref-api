package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StaffProfileCreationResponseTest {

    private final StaffProfileCreationResponse staffProfileCreationResponse = new StaffProfileCreationResponse();
    private final String testUuid = UUID.randomUUID().toString();

    @Test
    void test_has_mandatory_fields_specified_not_null() {
        staffProfileCreationResponse.setCaseWorkerId(testUuid);

        assertThat(staffProfileCreationResponse.getCaseWorkerId()).isEqualTo(testUuid);
    }
}