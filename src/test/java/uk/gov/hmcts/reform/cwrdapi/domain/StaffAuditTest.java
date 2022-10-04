package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        staffAudit.setRequestLog("JSON request");

        assertNotNull(staffAudit);
        assertThat(staffAudit.getId(), is(1L));
        assertThat(staffAudit.getAuthenticatedUserId(), is("Test User"));
        assertThat(staffAudit.getStatus(), is("SUCCESS"));
        assertThat(staffAudit.getErrorDescription(), is("No Error"));
        assertThat(staffAudit.getCaseWorkerId(), is("CWR1"));
        assertThat(staffAudit.getOperationType(), is("CREATE"));
        assertThat(staffAudit.getRequestLog(), is("JSON request"));
        assertNotNull(staffAudit.getRequestTimeStamp());

    }


    @Test
    void testStaffAuditBuilder() {

        StaffAudit staffAudit = StaffAudit.builder()
                .id(1L)
                .authenticatedUserId("AuthId")
                .requestTimeStamp(LocalDateTime.now())
                .status("SUCCESSFUL")
                .errorDescription(null)
                .caseWorkerId("ID created")
                .operationType("CREATE")
                .requestLog("json request")
                .build();

        assertNotNull(staffAudit);
        assertThat(staffAudit.getId(), is(1L));
        assertThat(staffAudit.getAuthenticatedUserId(), is("AuthId"));
        assertNotNull(staffAudit.getRequestTimeStamp());
        assertThat(staffAudit.getStatus(), is("SUCCESSFUL"));
        assertNotNull(staffAudit.getCaseWorkerId());
        assertThat(staffAudit.getOperationType(), is("CREATE"));
        assertThat(staffAudit.getRequestLog(), is("json request"));
        assertNull(staffAudit.getErrorDescription());

        String staffAuditString = StaffAudit.builder().id(1L).toString();

        assertTrue(staffAuditString
                .contains("StaffAudit.StaffAuditBuilder(id=1"));
    }

}
