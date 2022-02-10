package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseWorkerRoleTest {
    @Test
    void testCaseWorkerRole() {
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

        CaseWorkerRole caseWorkerRole1 = new CaseWorkerRole("caseWorkerId", 1L, true);
        assertNotNull(caseWorkerRole1);
        assertThat(caseWorkerRole1.getCaseWorkerId(), is("caseWorkerId"));
        assertThat(caseWorkerRole1.getRoleId(), is(1L));
        assertThat(caseWorkerRole1.getPrimaryFlag(), is(true));

    }

    @Test
    void testCaseWorkerRoleWithPrimaryFlagSetToTrue() {
        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setPrimaryFlag(true);

        assertTrue(caseWorkerRole.getPrimaryFlag());
    }
}
