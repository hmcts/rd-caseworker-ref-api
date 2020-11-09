package uk.gov.hmcts.reform.cwrdapi.domain;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class CaseWorkerIDAMRoleAssociationTest {

    @Test
    public void testCaseWorkerIDAMRoleAssociation() {

        CaseWorkerIDAMRoleAssociation caseWorkerIDAMRoleAssociation = new CaseWorkerIDAMRoleAssociation();
        caseWorkerIDAMRoleAssociation.setCaseWorkerIDAMRoleAssociationId(1L);
        caseWorkerIDAMRoleAssociation.setRoleId(1L);
        caseWorkerIDAMRoleAssociation.setServiceCode("SVCCode1");
        caseWorkerIDAMRoleAssociation.setIdamRole("IDAMRole");
        caseWorkerIDAMRoleAssociation.setCreatedDate(LocalDateTime.now());
        caseWorkerIDAMRoleAssociation.setLastUpdate(LocalDateTime.now());

        assertNotNull(caseWorkerIDAMRoleAssociation);

        assertThat(caseWorkerIDAMRoleAssociation.getCaseWorkerIDAMRoleAssociationId(), is (1L));
        assertThat(caseWorkerIDAMRoleAssociation.getRoleId(), is(1L));
        assertThat(caseWorkerIDAMRoleAssociation.getServiceCode(), is("SVCCode1"));
        assertThat(caseWorkerIDAMRoleAssociation.getIdamRole(), is("IDAMRole"));
        assertNotNull(caseWorkerIDAMRoleAssociation.getCreatedDate());
        assertNotNull(caseWorkerIDAMRoleAssociation.getLastUpdate());
    }
}
