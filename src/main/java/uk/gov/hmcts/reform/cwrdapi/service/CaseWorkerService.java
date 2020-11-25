package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRoleAssocResponse;

import java.util.List;

public interface CaseWorkerService {

    ResponseEntity<Object> processCaseWorkerProfiles(List<CaseWorkersProfileCreationRequest>
                                                                caseWorkersProfileCreationRequest);

    /**
     * Builds the idam role mappings for case worker roles
     * @param serviceRoleMappings list of ServiceRoleMapping
     * @return IdamRoleAssocResponse
     */
    IdamRoleAssocResponse buildIdamRoleMappings(List<ServiceRoleMapping> serviceRoleMappings);
}

