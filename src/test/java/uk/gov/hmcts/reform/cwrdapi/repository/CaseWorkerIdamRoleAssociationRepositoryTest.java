package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseWorkerIdamRoleAssociationRepositoryTest {
    @Mock
    private CaseWorkerIdamRoleAssociationRepository caseWorkerIdamRoleAssociationRepository;

    private CaseWorkerIdamRoleAssociation caseWorkerIdamRoleAssociation;

    @BeforeEach
    void setUp() {
        caseWorkerIdamRoleAssociation = new CaseWorkerIdamRoleAssociation();
        caseWorkerIdamRoleAssociation.setCwIdamRoleAssociationId(2L);
        caseWorkerIdamRoleAssociation.setIdamRole("testIdamRole");
    }

    @Test
    void test_findAll() {
        when(caseWorkerIdamRoleAssociationRepository.findAll())
                .thenReturn(Collections.singletonList(caseWorkerIdamRoleAssociation));

        assertFalse(caseWorkerIdamRoleAssociationRepository.findAll().isEmpty());
        assertThat(caseWorkerIdamRoleAssociationRepository
                .findAll().get(0).getCwIdamRoleAssociationId()).isEqualTo(2L);
    }

    @Test
    void test_findByRoleType() {
        when(caseWorkerIdamRoleAssociationRepository.findByRoleTypeInAndServiceCodeIn(any(), any()))
                .thenReturn(Collections.singletonList(caseWorkerIdamRoleAssociation));

        assertFalse(caseWorkerIdamRoleAssociationRepository.findByRoleTypeInAndServiceCodeIn(any(), any()).isEmpty());
        assertThat(caseWorkerIdamRoleAssociationRepository
                .findByRoleTypeInAndServiceCodeIn(any(), any()).get(0).getIdamRole()).isEqualTo("testIdamRole");

    }
}