package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CwrdCommonRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CwrdCommonRepository cwrdCommonRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFlush() {
        cwrdCommonRepository.flush();

        verify(entityManager, times(1)).flush();
    }

}
