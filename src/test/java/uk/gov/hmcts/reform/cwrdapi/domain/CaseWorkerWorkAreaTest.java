package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDate;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CaseWorkerWorkAreaTest {

    @Test
    public void testCaseWorkerWorkArea() {
        CaseWorkerWorkAreaTest caseWorkerWorkArea = new CaseWorkerWorkAreaTest();
        caseWorkerWorkArea.setCaseWorkerWorkAreaId(1L);
        caseWorkerWorkArea.setCaseWorkerId("CWID1");
        caseWorkerWorkArea.setAreaOfWork("TestArea");
        caseWorkerWorkArea.setServiceCode("SvcCode1");
        caseWorkerWorkArea.setCreatedDate(LocalDate.now());
        caseWorkerWorkArea.setLastUpdate(LocalDate.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerWorkArea.setCaseWorkerProfile(caseWorkerProfile);

        assertNotNull(caseWorkerWorkArea);
        assertThat(caseWorkerWorkArea.getCaseWorkerWorkAreaId(), 1L);
        assertThat(caseWorkerWorkArea.getCaseWorkerId(), "CWID1");
        assertThat(caseWorkerWorkArea.getAreaOfWork(), "TestArea");
        assertThat(caseWorkerWorkArea.getServiceCode(), "SvcCode1");
        assertThat(caseWorkerWorkArea.getCreatedDate(), LocalDate.now());
        assertThat(caseWorkerWorkArea.getLastUpdate(), LocalDate.now());

        assertNotNull(caseWorkerWorkArea.getCaseWorkerProfile());
        assertThat(caseWorkerWorkArea.getCaseWorkerProfile().getCaseWorkerId(), "CWID1");
    }
}
