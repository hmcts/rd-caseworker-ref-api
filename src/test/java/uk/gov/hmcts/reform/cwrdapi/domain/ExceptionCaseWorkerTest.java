package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class ExceptionCaseWorkerTest {

    @Test
    public void testCaseWorkerException() {
        ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
        exceptionCaseWorker.setId(1L);
        exceptionCaseWorker.setJobId(1L);
        exceptionCaseWorker.setExcelRowId("ExcelRow1");
        exceptionCaseWorker.setEmailId("Test@test.com");
        exceptionCaseWorker.setFieldInError("Field1");
        exceptionCaseWorker.setErrorDescription("Invalid Email");
        exceptionCaseWorker.setUpdatedTimeStamp(LocalDateTime.now());

        assertNotNull(exceptionCaseWorker);
        assertThat(exceptionCaseWorker.getId(), is(1L));
        assertThat(exceptionCaseWorker.getJobId(), is(1L));
        assertThat(exceptionCaseWorker.getExcelRowId(), is("ExcelRow1"));
        assertThat(exceptionCaseWorker.getEmailId(), is("Test@test.com"));
        assertThat(exceptionCaseWorker.getFieldInError(), is("Field1"));
        assertThat(exceptionCaseWorker.getErrorDescription(), is("Invalid Email"));
        assertNotNull(exceptionCaseWorker.getUpdatedTimeStamp());
    }

    @Test
    public void testCaseWorkerContainingAudit() {
        CaseWorkerAudit caseWorkerAudit = new CaseWorkerAudit();
        caseWorkerAudit.setJobId(1L);

        ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
        exceptionCaseWorker.setCaseWorkerAudit(caseWorkerAudit);

        assertNotNull(exceptionCaseWorker.getCaseWorkerAudit());

    }
}
