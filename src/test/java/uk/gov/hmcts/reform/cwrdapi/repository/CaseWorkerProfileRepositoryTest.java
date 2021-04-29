package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.Test;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CaseWorkerProfileRepositoryTest {

    public CaseWorkerProfileRepository cwProfileRepository = mock(CaseWorkerProfileRepository.class);

    @Test
    public void findByServiceCodeTest() {
        when(cwProfileRepository.findByEmailIdIn(any())).thenReturn(new ArrayList<>());
        assertNotNull(cwProfileRepository.findByEmailIdIn(any()));
        verify(cwProfileRepository, times(1)).findByEmailIdIn(any());
    }

    @Test
    public void findByEmailIdIgnoreCaseContaining() {
        when(cwProfileRepository.findByEmailIdIgnoreCaseContaining(any())).thenReturn(new ArrayList<>());
        assertNotNull(cwProfileRepository.findByEmailIdIgnoreCaseContaining(any()));
        verify(cwProfileRepository, times(1)).findByEmailIdIgnoreCaseContaining(any());
    }

    @Test
    public void findByCaseWorkerId() {
        when(cwProfileRepository.findByCaseWorkerId(any())).thenReturn(Optional.of(new CaseWorkerProfile()));
        assertNotNull(cwProfileRepository.findByCaseWorkerId(any()));
        verify(cwProfileRepository, times(1)).findByCaseWorkerId(any());
    }
}
