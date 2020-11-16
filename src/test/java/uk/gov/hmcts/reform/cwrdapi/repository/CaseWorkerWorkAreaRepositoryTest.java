package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerWorkAreaRepositoryTest {

    @Mock
    private CaseWorkerWorkAreaRepository caseWorkerWorkAreaRepository;

    private CaseWorkerWorkArea caseWorkerWorkArea;

    @Before
    public void setUp() {
        caseWorkerWorkArea = new CaseWorkerWorkArea();
        caseWorkerWorkArea.setAreaOfWork("testAOW");
        caseWorkerWorkArea.setCaseWorkerId("CWD1");
    }

    @Test
    public void test_findAll() {
        when(caseWorkerWorkAreaRepository.findAll())
                .thenReturn(Collections.singletonList(caseWorkerWorkArea));

        assertFalse(caseWorkerWorkAreaRepository.findAll().isEmpty());
        assertThat(caseWorkerWorkAreaRepository
                .findAll().get(0).getAreaOfWork()).isEqualTo("testAOW");
        assertThat(caseWorkerWorkAreaRepository
                .findAll().get(0).getCaseWorkerId()).isEqualTo(caseWorkerWorkArea.getCaseWorkerId());
    }

}