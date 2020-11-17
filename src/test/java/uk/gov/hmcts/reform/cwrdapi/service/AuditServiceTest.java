package uk.gov.hmcts.reform.cwrdapi.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.service.impl.AuditService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.ReflectionUtils.setField;

public class AuditServiceTest {

    @SuppressWarnings("unchecked")
    SimpleJpaRepository<CaseWorkerAudit, Long> caseWorkerAuditRepository = mock(SimpleJpaRepository.class);

    @SuppressWarnings("unchecked")
    SimpleJpaRepository<ExceptionCaseWorker, Long> caseWorkerExceptionRepository = mock(SimpleJpaRepository.class);

    @SuppressWarnings("unchecked")
    AuditService auditService = spy(new AuditService());

    @Before
    public void init() throws Exception {
        Field caseWorkerAuditRepositoryField = auditService.getClass()
            .getDeclaredField("caseWorkerAuditRepository");
        caseWorkerAuditRepositoryField.setAccessible(true);
        setField(caseWorkerAuditRepositoryField, auditService, caseWorkerAuditRepository);

        Field caseWorkerExceptionRepositoryField = auditService.getClass()
            .getDeclaredField("caseWorkerExceptionRepository");
        caseWorkerExceptionRepositoryField.setAccessible(true);
        setField(caseWorkerExceptionRepositoryField, auditService, caseWorkerExceptionRepository);
    }

    @Test
    public void testAuditSchedulerStatus() {
        CaseWorkerAudit audit = new CaseWorkerAudit();
        audit.setJobId(1L);
        when(caseWorkerAuditRepository.save(audit)).thenReturn(audit);
        assertEquals(auditService.auditSchedulerStatus(audit), audit.getJobId());
        verify(auditService).auditSchedulerStatus(audit);
    }

    @Test
    public void testAuditException() {
        List<ExceptionCaseWorker> exceptionCaseWorkerList = new ArrayList<>();
        when(caseWorkerExceptionRepository.saveAll(exceptionCaseWorkerList)).thenReturn(new ArrayList<>());
        auditService.auditException(exceptionCaseWorkerList);
        verify(auditService).auditException(exceptionCaseWorkerList);
    }

    @Test
    public void testSaveAuditException() {
        ExceptionCaseWorker exceptionCaseWorker = new ExceptionCaseWorker();
        when(caseWorkerExceptionRepository.save(exceptionCaseWorker)).thenReturn(exceptionCaseWorker);
        auditService.auditException(exceptionCaseWorker);
        verify(auditService).auditException(exceptionCaseWorker);
    }

}
