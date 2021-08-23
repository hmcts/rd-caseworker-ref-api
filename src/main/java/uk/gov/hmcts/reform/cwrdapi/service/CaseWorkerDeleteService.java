package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfilesDeletionResponse;

@Service
public interface CaseWorkerDeleteService {

    /**
     * Deletes a caseworker by their ID.
     *
     * @param userId String
     * @return CaseWorkerProfilesDeletionResponse
     */
    CaseWorkerProfilesDeletionResponse deleteByUserId(String userId);

    /**
     * Deletes caseworkers by associated email pattern.
     *
     * @param emailPattern String
     * @return CaseWorkerProfilesDeletionResponse
     */
    CaseWorkerProfilesDeletionResponse deleteByEmailPattern(String emailPattern);
}
