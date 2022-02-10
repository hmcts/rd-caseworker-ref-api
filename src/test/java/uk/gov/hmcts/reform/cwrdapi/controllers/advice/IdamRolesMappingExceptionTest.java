package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class IdamRolesMappingExceptionTest {

    @Test
    void test_handle_idam_role_mapping_exception() {
        IdamRolesMappingException idamRolesMappingException = new IdamRolesMappingException("Exception Message");
        assertThat(idamRolesMappingException).isNotNull();
        assertEquals("Exception Message", idamRolesMappingException.getMessage());
    }
}