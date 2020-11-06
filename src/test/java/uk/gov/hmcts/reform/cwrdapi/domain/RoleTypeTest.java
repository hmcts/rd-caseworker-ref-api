package uk.gov.hmcts.reform.cwrdapi.domain;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RoleTypeTest {
    @Test
    public void testRoleType() {
        RoleType roleType = new RoleType();
        roleType.setRoleId(1);
        roleType.setDescription("Test Description");
        roleType.setCreatedDate(LocalDate.now());
        roleType.setLastUpdate(LocalDate.now());

        CaseWorkerRoleTest caseWorkerRole = new CaseWorkerRoleTest();
        caseWorkerRole.setCaseWorkerRoleId(1);
        caseWorkerRole.setCaseWorkerId("CWID1");
        caseWorkerRole.setRoleId("Role1");
        caseWorkerRole.setPrimaryFlag(false);

        roleType.setCaseWorkerRole(Collections.singletonList(caseWorkerRole));

        assertNotNull(roleType);
        assertThat(roleType.getRoleId(), 1);
        assertThat(roleType.getDescription(), 1);
        assertThat(roleType.getCreatedDate(), LocalDate.now());
        assertThat(roleType.getLastUpdate(), LocalDate.now());

    }

}
