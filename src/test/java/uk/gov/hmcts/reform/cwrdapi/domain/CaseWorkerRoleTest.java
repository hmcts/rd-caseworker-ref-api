package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CaseWorkerRoleTest {
    @Test
    public void testCaseWorkerRole() {
        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setCaseWorkerRoleId(1L);
        caseWorkerRole.setCaseWorkerId("CWID1");
        caseWorkerRole.setRoleId(1L);
        caseWorkerRole.setPrimaryFlag(false);
        caseWorkerRole.setCreatedDate(LocalDateTime.now());
        caseWorkerRole.setLastUpdate(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerRole.setCaseWorkerProfile(caseWorkerProfile);

        RoleType roleType = new RoleType();
        roleType.setRoleId(1L);
        caseWorkerRole.setRoleType(roleType);

        assertNotNull(caseWorkerRole);
        assertThat(caseWorkerRole.getCaseWorkerRoleId(), is(1L));
        assertThat(caseWorkerRole.getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerRole.getRoleId(), is(1L));
        assertThat(caseWorkerRole.getPrimaryFlag(), is(false));
        assertNotNull(caseWorkerRole.getCreatedDate());
        assertNotNull(caseWorkerRole.getLastUpdate());

        assertNotNull(caseWorkerRole.getCaseWorkerProfile());
        assertThat(caseWorkerRole.getCaseWorkerProfile().getCaseWorkerId(), is("CWID1"));

        assertNotNull(caseWorkerRole.getRoleType());
        assertThat(caseWorkerRole.getRoleType().getRoleId(), is(1L));

    }

    @Test
    public void testCaseWorkerRoleWithPrimaryFlagSetToTrue() {
        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setPrimaryFlag(true);

        assertTrue(caseWorkerRole.getPrimaryFlag());
    }
}
