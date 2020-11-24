package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerIdamRoleAssociationRepositoryTest {
    @Mock
    private CaseWorkerIdamRoleAssociationRepository caseWorkerIdamRoleAssociationRepository;

    private CaseWorkerIdamRoleAssociation caseWorkerIdamRoleAssociation;

    @Before
    public void setUp() {
        caseWorkerIdamRoleAssociation = new CaseWorkerIdamRoleAssociation();
        caseWorkerIdamRoleAssociation.setCwIdamRoleAssociationId(2L);
        caseWorkerIdamRoleAssociation.setIdamRole("testIdamRole");
    }

    @Test
    public void test_findAll() {
        when(caseWorkerIdamRoleAssociationRepository.findAll())
                .thenReturn(Collections.singletonList(caseWorkerIdamRoleAssociation));

        assertFalse(caseWorkerIdamRoleAssociationRepository.findAll().isEmpty());
        assertThat(caseWorkerIdamRoleAssociationRepository
                .findAll().get(0).getCwIdamRoleAssociationId()).isEqualTo(2L);
    }

    @Test
    public void test_findByRoleType() {
        when(caseWorkerIdamRoleAssociationRepository.findByRoleType(any()))
                .thenReturn(Collections.singletonList(caseWorkerIdamRoleAssociation));

        assertFalse(caseWorkerIdamRoleAssociationRepository.findByRoleType(any()).isEmpty());
        assertThat(caseWorkerIdamRoleAssociationRepository
                .findByRoleType(any()).get(0).getIdamRole()).isEqualTo("testIdamRole");

    }
}