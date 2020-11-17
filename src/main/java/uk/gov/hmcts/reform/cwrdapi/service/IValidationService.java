package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;

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
    void auditJsr(long jobId);
}
