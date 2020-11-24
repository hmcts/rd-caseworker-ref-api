package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.List;

public interface CaseWorkerService {

    ResponseEntity<Object> processCaseWorkerProfiles(List<CaseWorkersProfileCreationRequest>
                                                                caseWorkersProfileCreationRequest);

}

