package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RoleTypeTest {
    @Test
    void testRoleType() {
        RoleType roleType = new RoleType();
        roleType.setRoleId(1L);
        roleType.setDescription("Test Description");
        roleType.setCreatedDate(LocalDateTime.now());
        roleType.setLastUpdate(LocalDateTime.now());

        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setCaseWorkerRoleId(1L);
        caseWorkerRole.setCaseWorkerId("CWID1");
        caseWorkerRole.setRoleId(1L);
        caseWorkerRole.setPrimaryFlag(false);

        roleType.setCaseWorkerRoles(Collections.singletonList(caseWorkerRole));

        assertNotNull(roleType);
        assertThat(roleType.getRoleId(), is(1L));
        assertThat(roleType.getDescription(), is("Test Description"));
        assertNotNull(roleType.getCreatedDate());
        assertNotNull(roleType.getLastUpdate());

    }

    @Test
    void testRoleTypeContainingCaseWorkerRoles() {
        CaseWorkerRole caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setRoleId(1L);

        RoleType roleType = new RoleType();
        roleType.setCaseWorkerRoles(Collections.singletonList(caseWorkerRole));

        assertFalse(roleType.getCaseWorkerRoles().isEmpty());

    }

    @Test
    void testRoleTypeContainingCaseWorkerIdamRoleAssoc() {
        CaseWorkerIdamRoleAssociation caseWorkerIdamRoleAssociation = new CaseWorkerIdamRoleAssociation();
        caseWorkerIdamRoleAssociation.setRoleId(1L);

        RoleType roleType = new RoleType();
        roleType.setCaseWorkerIdamRoleAssociations(Collections.singletonList(caseWorkerIdamRoleAssociation));

        assertFalse(roleType.getCaseWorkerIdamRoleAssociations().isEmpty());

    }

}
