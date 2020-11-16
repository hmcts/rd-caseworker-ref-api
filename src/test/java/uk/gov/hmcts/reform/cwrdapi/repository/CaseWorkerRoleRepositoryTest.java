package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerRole;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerRoleRepositoryTest {

    @Mock
    private CaseWorkerRoleRepository caseWorkerRoleRepository;

    private CaseWorkerRole caseWorkerRole;

    @Before
    public void setUp() {
        caseWorkerRole = new CaseWorkerRole();
        caseWorkerRole.setPrimaryFlag(true);
        caseWorkerRole.setCaseWorkerId("CWID1");
    }

    @Test
    public void test_findAll() {
        when(caseWorkerRoleRepository.findAll())
                .thenReturn(Collections.singletonList(caseWorkerRole));

        assertFalse(caseWorkerRoleRepository.findAll().isEmpty());
        assertThat(caseWorkerRoleRepository
                .findAll().get(0).getCaseWorkerId()).isEqualTo("CWID1");
        assertTrue(caseWorkerRole.getPrimaryFlag());
    }
}