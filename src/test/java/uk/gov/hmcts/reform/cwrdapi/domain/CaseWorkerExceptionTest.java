package uk.gov.hmcts.reform.cwrdapi.domain;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CaseWorkerExceptionTest {

    @Test
    public void testCaseWorkerException() {
        CaseWorkerException caseWorkerException = new CaseWorkerException();

        caseWorkerException.setId(1L);
        caseWorkerException.setJobId(1L);
        caseWorkerException.setExcelRowId("ExcelRow1");
        caseWorkerException.setEmailId("Test@test.com");
        caseWorkerException.setFieldInError("Field1");
        caseWorkerException.setErrorDescription("Invalid Email");
        caseWorkerException.setUpdatedTimeStamp(LocalDateTime.now());

        assertNotNull(caseWorkerException);
        assertThat(caseWorkerException.getId(), is(1L));
        assertThat(caseWorkerException.getExcelRowId(), is("ExcelRow1"));
        assertThat(caseWorkerException.getEmailId(), is("Test@test.com"));
        assertThat(caseWorkerException.getFieldInError(), is("Field1"));
        assertThat(caseWorkerException.getErrorDescription(), is("Invalid Email"));
        assertNotNull(caseWorkerException.getUpdatedTimeStamp());
    }
}
