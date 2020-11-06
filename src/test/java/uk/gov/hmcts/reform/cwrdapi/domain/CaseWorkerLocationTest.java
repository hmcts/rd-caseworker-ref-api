package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertNotNull;
import static org.hamcrest.junit.MatcherAssert.assertThat;

public class CaseWorkerLocationTest {
    @Test
    public void testCaseWorkerLocation() {
        CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setCaseWorkerLocationId(1L);
        caseWorkerLocation.setCaseWorkerId("CWID1");
        caseWorkerLocation.setLocation("TestLocation");
        caseWorkerLocation.setLocationId(13);
        caseWorkerLocation.setPrimaryFlag(false);
        caseWorkerLocation.setCreatedDate(LocalDate.now());
        caseWorkerLocation.setLastUpdate(LocalDate.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerLocation.setCaseWorkerProfile(caseWorkerProfile);

        assertNotNull(caseWorkerLocation);
        assertThat(caseWorkerLocation.getCaseWorkerLocationId(), 1L);
        assertThat(caseWorkerLocation.getCaseWorkerId(), "CWID1");
        assertThat(caseWorkerLocation.getLocation(), "TestLocation");
        assertThat(caseWorkerLocation.getLocationId(), 13);
        assertThat(caseWorkerLocation.getPrimaryFlag(), false);
        assertThat(caseWorkerLocation.getCreatedDate(), LocalDate.now());
        assertThat(caseWorkerLocation.getLastUpdate(), LocalDate.now());

        assertNotNull(caseWorkerLocation.getCaseWorkerProfile());
        assertThat(caseWorkerLocation.getCaseWorkerProfile().getCaseWorkerId(), "CWID1");
    }
}
