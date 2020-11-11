package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.List;

public interface CaseWorkerService {

    int createCaseWorkerUserProfiles(List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequest);

}

