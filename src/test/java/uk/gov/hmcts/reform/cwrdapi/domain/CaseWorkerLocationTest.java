package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CaseWorkerLocationTest {
    @Test
    public void testCaseWorkerLocation() {
        CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setCaseWorkerLocationId(1L);
        caseWorkerLocation.setCaseWorkerId("CWID1");
        caseWorkerLocation.setLocation("TestLocation");
        caseWorkerLocation.setLocationId(13);
        caseWorkerLocation.setPrimaryFlag(false);
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
        assertThat(caseWorkerLocation.getPrimaryFlag(), is(false));
        assertNotNull(caseWorkerLocation.getCreatedDate());
        assertNotNull(caseWorkerLocation.getLastUpdate());

        assertNotNull(caseWorkerLocation.getCaseWorkerProfile());
        assertThat(caseWorkerLocation.getCaseWorkerProfile().getCaseWorkerId(), is("CWID1"));
    }

    @Test
    public void testCaseWorkerLocationWithPrimaryFlagSetToTrue() {
        CaseWorkerLocation caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setPrimaryFlag(true);

        assertTrue(caseWorkerLocation.getPrimaryFlag());

        CaseWorkerLocation caseWorkerLocation1 = new CaseWorkerLocation("caseWorkerId",
                1,"TestLocation", false);

        assertNotNull(caseWorkerLocation1);
        assertThat(caseWorkerLocation1.getCaseWorkerId(), is("caseWorkerId"));
        assertThat(caseWorkerLocation1.getLocation(), is("TestLocation"));
        assertThat(caseWorkerLocation1.getLocationId(), is(1));
        assertThat(caseWorkerLocation1.getPrimaryFlag(), is(false));
    }
}
