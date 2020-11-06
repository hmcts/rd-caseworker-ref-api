package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
public class CaseWorkerAuditTest {
    @Test
    public void testCaseWorkerAudit() {
        CaseWorkerAudit caseWorkerAudit = new CaseWorkerAudit();

        caseWorkerAudit.setJobId(1);
        caseWorkerAudit.setAuthenticatedUserId("AuthId");
        caseWorkerAudit.setJobStartTime(LocalDate.now());
        caseWorkerAudit.setFileName("Test_File_Name");
        caseWorkerAudit.setJobEndTime(LocalDate.now();
        caseWorkerAudit.setStatus("Test Status");
        caseWorkerAudit.setComments("Test Comments");

        assertNotNull(caseWorkerAudit);
        assertThat(caseWorkerAudit.getAuthenticatedUserId(), "AuthId");
        assertThat(caseWorkerAudit.getJobStartTime(), LocalDate.now());
        assertThat(caseWorkerAudit.getFileName(), "Test_File_Name");
        assertThat(caseWorkerAudit.getJobEndTime(), LocalDate.now());
        assertThat(caseWorkerAudit.getStatus(), "Test Status");
        assertThat(caseWorkerAudit.getComments(), "Test Comments");

    }
}
