package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testCaseWorkerAuditContainingExceptionCaseWorkers() {
        ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
        exceptionCaseWorker.setId(1L);

        CaseWorkerAudit caseWorkerAudit = new CaseWorkerAudit();
        caseWorkerAudit.setExceptionCaseWorkers(singletonList(exceptionCaseWorker));
        assertFalse(caseWorkerAudit.getExceptionCaseWorkers().isEmpty());
    }

    @Test
    public void testCaseWorkerAuditBuilder() {

        CaseWorkerAudit caseWorkerAudit = CaseWorkerAudit.builder()
                .jobId(1L)
                .authenticatedUserId("AuthId")
                .jobStartTime(LocalDateTime.now())
                .jobEndTime(LocalDateTime.now())
                .fileName("Test_File_Name")
                .status("Test Status")
                .comments("Test Comments")
                .exceptionCaseWorkers(singletonList(new ExceptionCaseWorker()))
                .build();

        assertNotNull(caseWorkerAudit);
        assertThat(caseWorkerAudit.getJobId(), is(1L));
        assertThat(caseWorkerAudit.getAuthenticatedUserId(), is("AuthId"));
        assertNotNull(caseWorkerAudit.getJobStartTime());
        assertThat(caseWorkerAudit.getFileName(), is("Test_File_Name"));
        assertNotNull(caseWorkerAudit.getJobEndTime());
        assertThat(caseWorkerAudit.getStatus(), is("Test Status"));
        assertThat(caseWorkerAudit.getComments(), is("Test Comments"));
        assertFalse(caseWorkerAudit.getExceptionCaseWorkers().isEmpty());

        String caseWorkerAuditString = CaseWorkerAudit.builder().jobId(1L).toString();

        assertTrue(caseWorkerAuditString.contains("CaseWorkerAudit.CaseWorkerAuditBuilder(jobId=1"));


    }
}
