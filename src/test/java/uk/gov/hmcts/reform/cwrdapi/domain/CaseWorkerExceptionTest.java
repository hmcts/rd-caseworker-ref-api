package uk.gov.hmcts.reform.cwrdapi.domain;
import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CaseWorkerExceptionTest {

    @Test
    public void testCaseWorkerException() {
        CaseWorkerException caseWorkerException = new CaseWorkerException();

        caseWorkerException.setId(1);
        caseWorkerException.setJobId(1L);
        caseWorkerException.setExcelRowId("ExcelRow1");
        caseWorkerException.setEmailId("Test@test.com");
        caseWorkerException.setFieldInError("Field1");
        caseWorkerException.setErrorDescription("Invalid Email");
        caseWorkerException.setUpdatedTimeStamp(LocalDate.now());

        assertNotNull(caseWorkerException);
        assertThat(caseWorkerException.getId(), 1L);
        assertThat(caseWorkerException.getExcelRowId(), "ExcelRow1");
        assertThat(caseWorkerException.getEmailId(), "Test@test.com");
        assertThat(caseWorkerException.getFieldInError(), "Field1");
        assertThat(caseWorkerException.getErrorDescription(), "Invalid Email");
        assertThat(caseWorkerException.getUpdatedTimeStamp(), LocalDate.now());
    }
}
