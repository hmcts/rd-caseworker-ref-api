package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
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
        caseWorkerLocation.setPrimary(false);
        caseWorkerLocation.setCreatedDate(LocalDateTime.now());
        caseWorkerLocation.setLastUpdate(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerLocation.setCaseWorkerProfile(caseWorkerProfile);

        assertNotNull(caseWorkerLocation);
        assertThat(caseWorkerLocation.getCaseWorkerLocationId(), is(1L));
        assertThat(caseWorkerLocation.getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerLocation.getLocation(), is("TestLocation"));
        assertThat(caseWorkerLocation.getLocationId(), is(13));
        assertThat(caseWorkerLocation.getPrimary(), is(false));
        assertNotNull(caseWorkerLocation.getCreatedDate());
        assertNotNull(caseWorkerLocation.getLastUpdate());

        assertNotNull(caseWorkerLocation.getCaseWorkerProfile());
        assertThat(caseWorkerLocation.getCaseWorkerProfile().getCaseWorkerId(), is("CWID1"));
    }
}
