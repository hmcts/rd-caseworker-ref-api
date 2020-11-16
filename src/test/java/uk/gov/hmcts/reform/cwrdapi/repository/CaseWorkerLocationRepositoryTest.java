package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerLocation;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerLocationRepositoryTest {

    @Mock
    private CaseWorkerLocationRepository caseWorkerLocationRepository;

    private CaseWorkerLocation caseWorkerLocation;

    @Before
    public void setUp() {
        caseWorkerLocation = new CaseWorkerLocation();
        caseWorkerLocation.setPrimaryFlag(true);
        caseWorkerLocation.setCaseWorkerId("CWID1");
    }

    @Test
    public void test_findAll() {
        when(caseWorkerLocationRepository.findAll())
                .thenReturn(Collections.singletonList(caseWorkerLocation));

        assertFalse(caseWorkerLocationRepository.findAll().isEmpty());
        assertThat(caseWorkerLocationRepository
                .findAll().get(0).getCaseWorkerId()).isEqualTo("CWID1");
        assertTrue(caseWorkerLocationRepository.findAll().get(0).getPrimaryFlag());
    }

}