package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionCaseWorkerTest {

    @Test
    void testCaseWorkerException() {
        ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
        exceptionCaseWorker.setId(1L);
        exceptionCaseWorker.setJobId(1L);
        exceptionCaseWorker.setExcelRowId("ExcelRow1");
        exceptionCaseWorker.setKeyField("Test@test.com");
        exceptionCaseWorker.setFieldInError("Field1");
        exceptionCaseWorker.setErrorDescription("Invalid Email");
        exceptionCaseWorker.setUpdatedTimeStamp(LocalDateTime.now());

        assertNotNull(exceptionCaseWorker);
        assertThat(exceptionCaseWorker.getId(), is(1L));
        assertThat(exceptionCaseWorker.getJobId(), is(1L));
        assertThat(exceptionCaseWorker.getExcelRowId(), is("ExcelRow1"));
        assertThat(exceptionCaseWorker.getKeyField(), is("Test@test.com"));
        assertThat(exceptionCaseWorker.getFieldInError(), is("Field1"));
        assertThat(exceptionCaseWorker.getErrorDescription(), is("Invalid Email"));
        assertNotNull(exceptionCaseWorker.getUpdatedTimeStamp());
    }

    @Test
    void testCaseWorkerContainingAudit() {
        CaseWorkerAudit caseWorkerAudit = CaseWorkerAudit.builder().jobId(1L).build();
        ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
        exceptionCaseWorker.setCaseWorkerAudit(caseWorkerAudit);
        assertNotNull(exceptionCaseWorker.getCaseWorkerAudit());
    }

    @Test
    void testExceptionCaseWorkerBuilder() {
        ExceptionCaseWorker exceptionCaseWorker = ExceptionCaseWorker.builder()
                .id(1L)
                .jobId(1L)
                .excelRowId("ExcelRow1")
                .keyField("Test@test.com")
                .fieldInError("Field1")
                .errorDescription("Invalid Email")
                .updatedTimeStamp(LocalDateTime.now())
                .caseWorkerAudit(CaseWorkerAudit.builder().jobId(1L).build())
                .build();

        assertNotNull(exceptionCaseWorker);
        assertThat(exceptionCaseWorker.getId(), is(1L));
        assertThat(exceptionCaseWorker.getJobId(), is(1L));
        assertThat(exceptionCaseWorker.getExcelRowId(), is("ExcelRow1"));
        assertThat(exceptionCaseWorker.getKeyField(), is("Test@test.com"));
        assertThat(exceptionCaseWorker.getFieldInError(), is("Field1"));
        assertThat(exceptionCaseWorker.getErrorDescription(), is("Invalid Email"));
        assertNotNull(exceptionCaseWorker.getUpdatedTimeStamp());
        assertNotNull(exceptionCaseWorker.getCaseWorkerAudit());

        String exceptionCaseWorkerString = ExceptionCaseWorker.builder().id(1L).toString();

        assertTrue(exceptionCaseWorkerString.contains("ExceptionCaseWorker.ExceptionCaseWorkerBuilder(id=1"));

    }
}
