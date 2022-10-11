package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaseWorkerProfileRepositoryTest {

    CaseWorkerProfileRepository cwProfileRepository = mock(CaseWorkerProfileRepository.class);

    @Test
    void findByServiceCodeTest() {
        when(cwProfileRepository.findByEmailIdIn(any())).thenReturn(new ArrayList<>());
        assertNotNull(cwProfileRepository.findByEmailIdIn(any()));
        verify(cwProfileRepository, times(1)).findByEmailIdIn(any());
    }

    @Test
    void findByEmailIdIgnoreCaseContaining() {
        when(cwProfileRepository.findByEmailIdIgnoreCaseContaining(any())).thenReturn(new ArrayList<>());
        assertNotNull(cwProfileRepository.findByEmailIdIgnoreCaseContaining(any()));
        verify(cwProfileRepository, times(1)).findByEmailIdIgnoreCaseContaining(any());
    }

    @Test
    void findByCaseWorkerId() {
        when(cwProfileRepository.findByCaseWorkerId(any())).thenReturn(Optional.of(new CaseWorkerProfile()));
        assertNotNull(cwProfileRepository.findByCaseWorkerId(any()));
        verify(cwProfileRepository, times(1)).findByCaseWorkerId(any());
    }

    @Test
    void findByCaseWorkerIds() {
        List<CaseWorkerProfile> caseworkerProfiles = Collections.singletonList(new CaseWorkerProfile());
        List<String> Ids = Collections.singletonList("id_abcd");
        when(cwProfileRepository.findByCaseWorkerIdIn(Ids)).thenReturn(caseworkerProfiles);
        assertNotNull(cwProfileRepository.findByCaseWorkerIdIn(Ids));
        verify(cwProfileRepository, times(1)).findByCaseWorkerIdIn(any());
    }

    @Test
    void findByEmailId() {
        when(cwProfileRepository.findByEmailId(any())).thenReturn(new CaseWorkerProfile());
        assertNotNull(cwProfileRepository.findByEmailId(any()));
        verify(cwProfileRepository, times(1)).findByEmailId(any());
    }
}
