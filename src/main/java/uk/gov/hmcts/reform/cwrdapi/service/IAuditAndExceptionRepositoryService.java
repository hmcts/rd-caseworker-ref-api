package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;

import java.util.List;

public interface IAuditAndExceptionRepositoryService {

    /**
     * Capture and log scheduler details with file status.
     *
     * @param audit CaseWorkerAudit
     * @return Long
     */
    Long auditSchedulerStatus(CaseWorkerAudit audit);

    /**
     * Records JSR Exceptions in batches.
     *
     * @param exceptionCaseWorkers List
     */
    void auditException(List<ExceptionCaseWorker> exceptionCaseWorkers);

    /**
     * Records run time failures.
     *
     * @param exceptionCaseWorker ExceptionCaseWorker
     */
    void auditException(ExceptionCaseWorker exceptionCaseWorker);

    List<ExceptionCaseWorker> getAllExceptions(Long jobId);
}
