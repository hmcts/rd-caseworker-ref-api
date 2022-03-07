package uk.gov.hmcts.reform.cwrdapi.controllers.constants;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.EMPTY_RESULT_DATA_ACCESS;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.INVALID_REQUEST_EXCEPTION;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.MALFORMED_JSON;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.UNSUPPORTED_MEDIA_TYPES;

class ErrorConstantsTest {

    @Test
    void test_shouldReturnMsgWhenMsgPassed() {
        ErrorConstants mailFormedJson = MALFORMED_JSON;
        assertThat(mailFormedJson).isNotNull();
        assertThat(mailFormedJson.getErrorMessage()).isEqualTo(MALFORMED_JSON.getErrorMessage());
        ErrorConstants mediaTypes = UNSUPPORTED_MEDIA_TYPES;
        assertThat(mediaTypes).isNotNull();
        assertThat(mediaTypes.getErrorMessage()).isEqualTo("2 : Unsupported Media Type");
        ErrorConstants invalidExp = INVALID_REQUEST_EXCEPTION;
        assertThat(invalidExp).isNotNull();
        assertThat(invalidExp.getErrorMessage())
                .isEqualTo("3 : There is a problem with your request. Please check and try again");
        ErrorConstants emptyResultDataAccess = EMPTY_RESULT_DATA_ACCESS;
        assertThat(emptyResultDataAccess).isNotNull();
        assertThat(emptyResultDataAccess.getErrorMessage()).isEqualTo("4 : Resource not found");
    }
}
