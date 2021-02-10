package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;

import java.util.List;

public interface IValidationService {

    /**
     * Returns invalid record list and JSR Constraint violations pair.
     *
     * @param caseWorkerProfileList List
     * @return CasWorkerDomain list
     */
    List<CaseWorkerDomain> getInvalidRecords(List<CaseWorkerDomain> caseWorkerProfileList);

    /**
     * Audit JSR exceptions..
     *
     * @param jobId long
     */
    void saveJsrExceptionsForCaseworkerJob(long jobId);

    /**
     * Create CWR Audit entry with in-progress Status.
     *
     * @param auditStatus AuditStatus
     * @param fileName    String
     * @return JobId long
     */
    long startCaseworkerAuditing(final AuditStatus auditStatus, final String fileName);

    /**
     * Update Audit status with Success/Failure/PartialSuccess.
     *
     * @param auditStatus AuditStatus
     * @param fileName    String
     * @return JobId long
     */
    long updateCaseWorkerAuditStatus(final AuditStatus auditStatus, final String fileName);

    List<ExceptionCaseWorker> getCaseWorkersExceptions();

    long getAuditJobId();

    void logFailures(String message, long rowId);
}
