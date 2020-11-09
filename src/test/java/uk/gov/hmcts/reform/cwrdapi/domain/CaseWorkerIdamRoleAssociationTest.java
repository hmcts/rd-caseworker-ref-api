package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CaseWorkerIdamRoleAssociationTest {

    @Test
    public void testCaseWorkerIdamRoleAssociation() {

        CaseWorkerIdamRoleAssociation caseWorkerIdamRoleAssociation = new CaseWorkerIdamRoleAssociation();
        caseWorkerIdamRoleAssociation.setCaseWorkerIdamRoleAssociationId(1L);
        caseWorkerIdamRoleAssociation.setRoleId(1L);
        caseWorkerIdamRoleAssociation.setServiceCode("SVCCode1");
        caseWorkerIdamRoleAssociation.setIdamRole("IDAMRole");
        caseWorkerIdamRoleAssociation.setCreatedDate(LocalDateTime.now());
        caseWorkerIdamRoleAssociation.setLastUpdate(LocalDateTime.now());

        assertNotNull(caseWorkerIdamRoleAssociation);
        assertThat(caseWorkerIdamRoleAssociation.getCaseWorkerIdamRoleAssociationId(), is(1L));
        assertThat(caseWorkerIdamRoleAssociation.getRoleId(), is(1L));
        assertThat(caseWorkerIdamRoleAssociation.getServiceCode(), is("SVCCode1"));
        assertThat(caseWorkerIdamRoleAssociation.getIdamRole(), is("IDAMRole"));
        assertNotNull(caseWorkerIdamRoleAssociation.getCreatedDate());
        assertNotNull(caseWorkerIdamRoleAssociation.getLastUpdate());
    }
}
