package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CwrdCommonRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CwrdCommonRepository cwrdCommonRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFlush() {
        cwrdCommonRepository.flush();

        verify(entityManager, times(1)).flush();
    }

}
