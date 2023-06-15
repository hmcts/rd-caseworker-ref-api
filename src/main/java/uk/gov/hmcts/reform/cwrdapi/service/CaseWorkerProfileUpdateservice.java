package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileUpdationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkersProfileUpdationResponse;

@Service
public interface CaseWorkerProfileUpdateservice {

    CaseWorkersProfileUpdationResponse
        updateCaseWorkerProfile(CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest);
}
