package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CaseWorkerAuditTest {
    @Test
    public void testCaseWorkerAudit() {
        CaseWorkerAudit caseWorkerAudit = new CaseWorkerAudit();

        caseWorkerAudit.setJobId(1L);
        caseWorkerAudit.setAuthenticatedUserId("AuthId");
        caseWorkerAudit.setJobStartTime(LocalDateTime.now());
        caseWorkerAudit.setFileName("Test_File_Name");
        caseWorkerAudit.setJobEndTime(LocalDateTime.now());
        caseWorkerAudit.setStatus("Test Status");
        caseWorkerAudit.setComments("Test Comments");

        assertNotNull(caseWorkerAudit);
        assertThat(caseWorkerAudit.getJobId(), is(1L));
        assertThat(caseWorkerAudit.getAuthenticatedUserId(), is("AuthId"));
        assertNotNull(caseWorkerAudit.getJobStartTime());
        assertThat(caseWorkerAudit.getFileName(), is("Test_File_Name"));
        assertNotNull(caseWorkerAudit.getJobEndTime());
        assertThat(caseWorkerAudit.getStatus(), is("Test Status"));
        assertThat(caseWorkerAudit.getComments(), is("Test Comments"));

    }
}
