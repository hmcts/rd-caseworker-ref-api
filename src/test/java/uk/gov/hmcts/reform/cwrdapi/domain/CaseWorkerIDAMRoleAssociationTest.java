package uk.gov.hmcts.reform.cwrdapi.domain;
import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CaseWorkerIDAMRoleAssociationTest {

    @Test
    public void testCaseWorkerIDAMRoleAssociation() {
        CaseWorkerIDAMRoleAssociation caseWorkerIDAMRoleAssociation = new CaseWorkerIDAMRoleAssociation();
        caseWorkerIDAMRoleAssociation.setCaseWorkerIDAMRoleAssociation(1);
        caseWorkerIDAMRoleAssociation.setRoleId(1);
        caseWorkerIDAMRoleAssociation.setServiceCode("SVCCode1");
        caseWorkerIDAMRoleAssociation.setIDAMRole("IDAMRole");
        caseWorkerIDAMRoleAssociation.setCreatedDate(LocalDate.now());
        caseWorkerIDAMRoleAssociation.setLastUpdate(LocalDate.now());

        assertNotNull(caseWorkerIDAMRoleAssociation);

        assertThat(caseWorkerIDAMRoleAssociation.getCaseWorkerIDAMRoleAssociation(), 1);
        assertThat(caseWorkerIDAMRoleAssociation.getRoleId(), 1);
        assertThat(caseWorkerIDAMRoleAssociation.getServiceCode(), "SVCCode1");
        assertThat(caseWorkerIDAMRoleAssociation.getIDAMRole(), "IDAMRole");
        assertThat(caseWorkerIDAMRoleAssociation.getCreatedDate(), LocalDate.now());
        assertThat(caseWorkerIDAMRoleAssociation.getLastUpdate(), LocalDate.now());
    }
}
