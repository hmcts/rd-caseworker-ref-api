package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRolesMappingResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

import java.util.List;

@Service
public interface CaseWorkerService {

    List<CaseWorkerProfile> processCaseWorkerProfiles(List<CaseWorkersProfileCreationRequest>
                                                          caseWorkersProfileCreationRequest);

    /**
     * Builds the idam role mappings for case worker roles.
     *
     * @param serviceRoleMappings list of ServiceRoleMapping
     * @return IdamRoleAssocResponse
     */
    IdamRolesMappingResponse buildIdamRoleMappings(List<ServiceRoleMapping> serviceRoleMappings);

    /**
     * Prepare caseworker data to be published as a message to topic.
     *
     * @param caseWorkerData list containing caseworker data
     */
    void publishCaseWorkerDataToTopic(List<CaseWorkerProfile> caseWorkerData);

    /**
     * Returns the caseworker details.
     *
     * @param caseWorkerIds list
     * @return CaseWorkerProfile
     */
    ResponseEntity<Object> fetchCaseworkersById(List<String> caseWorkerIds);
}

