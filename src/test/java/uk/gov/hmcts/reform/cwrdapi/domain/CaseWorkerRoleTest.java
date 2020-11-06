package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDate;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CaseWorkerRoleTest {
    @Test
    public void testCaseWorkerRole() {

        CaseWorkerRoleTest caseWorkerRole = new CaseWorkerRoleTest();
        caseWorkerRole.setCaseWorkerRoleId(1);
        caseWorkerRole.setCaseWorkerId("CWID1");
        caseWorkerRole.setRoleId("Role1");
        caseWorkerRole.setPrimaryFlag(false);
        caseWorkerRole.setCreatedDate(LocalDate.now());
        caseWorkerRole.setLastUpdate(LocalDate.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerRole.setCaseWorkerProfile(caseWorkerProfile);

        RoleType roleType = new RoleType();
        roleType.setRoleId(1);
        caseWorkerRole.setRoleType(roleType);

        assertNotNull(caseWorkerRole);
        assertThat(caseWorkerRole.getCaseWorkerRoleId(), 1);
        assertThat(caseWorkerRole.getCaseWorkerId(), "CWID1");
        assertThat(caseWorkerRole.getRoleId(), "Role1");
        assertThat(caseWorkerRole.getPrimaryFlag(), false);
        assertThat(caseWorkerRole.getCreatedDate(), LocalDate.now());
        assertThat(caseWorkerRole.getLastUpdate(), LocalDate.now());

        assertNotNull(caseWorkerRole.getCaseWorkerProfile());
        assertThat(caseWorkerRole.getCaseWorkerProfile().getCaseWorkerId(), "CWID1");

        assertNotNull(caseWorkerRole.getRoleType());
        assertThat(caseWorkerRole.getRoleType().getRoleId(), 1);

    }
}
