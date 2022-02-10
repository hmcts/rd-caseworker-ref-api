package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CaseWorkerWorkAreaTest {

    @Test
    void testCaseWorkerWorkArea() {
        CaseWorkerWorkArea caseWorkerWorkArea = new CaseWorkerWorkArea();
        caseWorkerWorkArea.setCaseWorkerWorkAreaId(1L);
        caseWorkerWorkArea.setCaseWorkerId("CWID1");
        caseWorkerWorkArea.setAreaOfWork("TestArea");
        caseWorkerWorkArea.setServiceCode("SvcCode1");
        caseWorkerWorkArea.setCreatedDate(LocalDateTime.now());
        caseWorkerWorkArea.setLastUpdate(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerWorkArea.setCaseWorkerProfile(caseWorkerProfile);

        assertNotNull(caseWorkerWorkArea);
        assertThat(caseWorkerWorkArea.getCaseWorkerWorkAreaId(), is(1L));
        assertThat(caseWorkerWorkArea.getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerWorkArea.getAreaOfWork(), is("TestArea"));
        assertThat(caseWorkerWorkArea.getServiceCode(), is("SvcCode1"));
        assertNotNull(caseWorkerWorkArea.getCreatedDate());
        assertNotNull(caseWorkerWorkArea.getLastUpdate());

        assertNotNull(caseWorkerWorkArea.getCaseWorkerProfile());
        assertThat(caseWorkerWorkArea.getCaseWorkerProfile().getCaseWorkerId(), is("CWID1"));

        CaseWorkerWorkArea caseWorkerWorkArea1 = new CaseWorkerWorkArea("caseWorkerId",
                "areaOfWork","serviceCode");
        assertThat(caseWorkerWorkArea1.getCaseWorkerId(), is("caseWorkerId"));
        assertThat(caseWorkerWorkArea1.getAreaOfWork(), is("areaOfWork"));
        assertThat(caseWorkerWorkArea1.getServiceCode(), is("serviceCode"));

    }
}
