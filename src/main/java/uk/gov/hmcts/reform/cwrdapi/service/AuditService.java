package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;

import java.util.List;

@Component
public class AuditService {

    @Autowired
    SimpleJpaRepository<CaseWorkerAudit, Long> caseWorkerAuditRepository;

    @Autowired
    SimpleJpaRepository<ExceptionCaseWorker, Long> caseWorkerExceptionRepository;

    /**
     * Capture and log scheduler details with file status.
     *
     * @param audit CaseWorkerAudit
     * @return Long
     */
    public Long auditSchedulerStatus(CaseWorkerAudit audit) {
        return caseWorkerAuditRepository.save(audit).getJobId();
    }

    /**
     * Records JSR Exceptions in batches.
     *
     * @param exceptionCaseWorkers List
     */
    public void auditException(List<ExceptionCaseWorker> exceptionCaseWorkers) {
        caseWorkerExceptionRepository.saveAll(exceptionCaseWorkers);
    }

    /**
     * Records run time failures.
     *
     * @param exceptionCaseWorker ExceptionCaseWorker
     */
    public void auditException(ExceptionCaseWorker exceptionCaseWorker) {
        caseWorkerExceptionRepository.save(exceptionCaseWorker);
    }
}
