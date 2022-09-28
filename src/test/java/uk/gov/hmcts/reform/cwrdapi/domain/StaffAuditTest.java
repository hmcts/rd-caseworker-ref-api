package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StaffAuditTest {

    @Test
    void testStaffAudit() {
        StaffAudit staffAudit = new StaffAudit();
        staffAudit.setId(1L);
        staffAudit.setAuthenticatedUserId("Test User");
        staffAudit.setStatus("SUCCESS");
        staffAudit.setErrorDescription("No Error");
        staffAudit.setCaseWorkerId("CWR1");
        staffAudit.setOperationType("CREATE");
        staffAudit.setRequestTimeStamp(LocalDateTime.now());

        assertNotNull(staffAudit);
        assertThat(staffAudit.getId(), is(1L));
        assertThat(staffAudit.getAuthenticatedUserId(), is("Test User"));
        assertThat(staffAudit.getStatus(), is("SUCCESS"));
        assertThat(staffAudit.getErrorDescription(), is("No Error"));
        assertThat(staffAudit.getCaseWorkerId(), is("CWR1"));
        assertThat(staffAudit.getOperationType(), is("CREATE"));
        assertNotNull(staffAudit.getRequestTimeStamp());

    }

}
