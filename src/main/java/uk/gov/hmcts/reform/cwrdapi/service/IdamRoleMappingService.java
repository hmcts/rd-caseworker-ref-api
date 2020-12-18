package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;

import java.util.List;
import java.util.Set;

public interface IdamRoleMappingService {
    /**
     * Builds the idam role mappings for case worker roles.
     * @param caseWorkerIdamRoleAssociations list of CaseWorkerIdamRoleAssociation
     */
    void buildIdamRoleAssociation(
            List<CaseWorkerIdamRoleAssociation> caseWorkerIdamRoleAssociations);

    /**
     * Deletes all the records for the service code provided.
     * @param serviceCode set of service code
     */
    void deleteExistingRecordForServiceCode(Set<String> serviceCode);
}
