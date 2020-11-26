package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class IdamRolesMappingExceptionTest {

    @Test
    public void test_handle_idam_role_mapping_exception() {
        IdamRolesMappingException idamRolesMappingException = new IdamRolesMappingException("Exception Message");
        assertThat(idamRolesMappingException).isNotNull();
        assertEquals("Exception Message", idamRolesMappingException.getMessage());
    }
}