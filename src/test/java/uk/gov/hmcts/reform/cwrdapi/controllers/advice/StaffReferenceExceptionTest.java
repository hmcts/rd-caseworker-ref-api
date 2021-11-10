package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class StaffReferenceExceptionTest {
    @Test
    public void test_handle_json_feign_response_parsing_exception() {
        StaffReferenceException staffReferenceException =
                new StaffReferenceException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Parsing exception", "Parsing exception");
        assertThat(staffReferenceException).isNotNull();
        assertEquals("Parsing exception", staffReferenceException.getErrorMessage());
        assertEquals("Parsing exception", staffReferenceException.getErrorDescription());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, staffReferenceException.getStatus());
    }
}