package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class InvalidRequestExceptionTest {

    @Test
    public void test_handle_invalid_request_exception() {
        InvalidRequestException invalidRequestException = new InvalidRequestException("Bad Request");
        assertThat(invalidRequestException).isNotNull();
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), invalidRequestException.getMessage());
    }
}
