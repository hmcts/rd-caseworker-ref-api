package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class StaffReferenceExceptionTest {
    @Test
    public void test_handle_json_feign_response_parsing_exception() {
        StaffReferenceException staffReferenceException =
                new StaffReferenceException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Parsing exception", new RuntimeException());
        assertThat(staffReferenceException).isNotNull();
        assertEquals("Parsing exception", staffReferenceException.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, staffReferenceException.getStatus());
    }
}