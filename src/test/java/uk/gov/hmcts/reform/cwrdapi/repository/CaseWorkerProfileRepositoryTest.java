package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.Test;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CaseWorkerProfileRepositoryTest {

    public CaseWorkerProfileRepository cwProfileRepository = mock(CaseWorkerProfileRepository.class);

    @Test
    public void findByServiceCodeTest() {
        when(cwProfileRepository.findByEmailId(anyString())).thenReturn(new CaseWorkerProfile());
        assertNotNull(cwProfileRepository.findByEmailId(anyString()));
        verify(cwProfileRepository, times(1)).findByEmailId(anyString());
    }
}
