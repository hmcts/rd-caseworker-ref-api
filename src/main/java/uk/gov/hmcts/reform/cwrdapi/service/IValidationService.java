package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.client.domain.CasWorkerDomain;

import java.util.List;

public interface IValidationService {

    /**
     * Returns invalid record list and JSR Constraint violations pair.
     *
     * @param caseWorkerProfileList List
     * @return CasWorkerDomain list
     */
    List<CasWorkerDomain> getInvalidRecords(List<CasWorkerDomain> caseWorkerProfileList);

    /**
     * Audit JSR exceptions..
     *
     * @param jobId long
     */
    void auditJsr(long jobId);
}
