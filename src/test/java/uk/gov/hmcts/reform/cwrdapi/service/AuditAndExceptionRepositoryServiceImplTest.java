package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.repository.ExceptionCaseWorkerRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.AuditAndExceptionRepositoryServiceImpl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ReflectionUtils.setField;

public class AuditAndExceptionRepositoryServiceImplTest {

    @SuppressWarnings("unchecked")
    SimpleJpaRepository<CaseWorkerAudit, Long> caseWorkerAuditRepository = mock(SimpleJpaRepository.class);

    @SuppressWarnings("unchecked")
    ExceptionCaseWorkerRepository caseWorkerExceptionRepository = mock(ExceptionCaseWorkerRepository.class);

    @SuppressWarnings("unchecked")
    AuditAndExceptionRepositoryServiceImpl auditAndExceptionRepositoryServiceImpl =
        spy(new AuditAndExceptionRepositoryServiceImpl());

    @Before
    public void init() throws Exception {
        Field caseWorkerAuditRepositoryField = auditAndExceptionRepositoryServiceImpl.getClass()
            .getDeclaredField("caseWorkerAuditRepository");
        caseWorkerAuditRepositoryField.setAccessible(true);
        setField(caseWorkerAuditRepositoryField, auditAndExceptionRepositoryServiceImpl, caseWorkerAuditRepository);

        Field caseWorkerExceptionRepositoryField = auditAndExceptionRepositoryServiceImpl.getClass()
            .getDeclaredField("exceptionCaseWorkerRepository");
        caseWorkerExceptionRepositoryField.setAccessible(true);
        setField(caseWorkerExceptionRepositoryField, auditAndExceptionRepositoryServiceImpl,
            caseWorkerExceptionRepository);
    }

    @Test
    public void testAuditSchedulerStatus() {
        CaseWorkerAudit audit = new CaseWorkerAudit();
        audit.setJobId(1L);
        when(caseWorkerAuditRepository.save(audit)).thenReturn(audit);
        assertEquals(auditAndExceptionRepositoryServiceImpl.auditSchedulerStatus(audit), audit.getJobId());
        verify(auditAndExceptionRepositoryServiceImpl).auditSchedulerStatus(audit);
    }

    @Test
    public void testAuditException() {
        List<ExceptionCaseWorker> exceptionCaseWorkerList = new ArrayList<>();
        when(caseWorkerExceptionRepository.saveAll(exceptionCaseWorkerList)).thenReturn(new ArrayList<>());
        auditAndExceptionRepositoryServiceImpl.auditException(exceptionCaseWorkerList);
        verify(auditAndExceptionRepositoryServiceImpl).auditException(exceptionCaseWorkerList);
    }

    @Test
    public void testSaveAuditException() {
        ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
        when(caseWorkerExceptionRepository.save(exceptionCaseWorker)).thenReturn(exceptionCaseWorker);
        auditAndExceptionRepositoryServiceImpl.auditException(exceptionCaseWorker);
        verify(auditAndExceptionRepositoryServiceImpl).auditException(exceptionCaseWorker);
    }

    @Test
    public void testGetAllExceptions() {
        List<ExceptionCaseWorker> exceptionCaseWorkers = new ArrayList<>();
        ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
        exceptionCaseWorker.setJobId(1L);
        exceptionCaseWorkers.add(exceptionCaseWorker);

        when(caseWorkerExceptionRepository.save(exceptionCaseWorker)).thenReturn(exceptionCaseWorker);
        when(caseWorkerExceptionRepository.findByJobId(1L)).thenReturn(exceptionCaseWorkers);

        auditAndExceptionRepositoryServiceImpl.auditException(exceptionCaseWorker);
        List<ExceptionCaseWorker> caseWorkers =
                auditAndExceptionRepositoryServiceImpl.getAllExceptions(1L);

        assertThat(caseWorkers).isNotEmpty();

        verify(auditAndExceptionRepositoryServiceImpl).getAllExceptions(1L);
    }
}
