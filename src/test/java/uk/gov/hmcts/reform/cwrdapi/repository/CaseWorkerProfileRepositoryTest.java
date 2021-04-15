package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.Test;

import java.util.ArrayList;

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
}
