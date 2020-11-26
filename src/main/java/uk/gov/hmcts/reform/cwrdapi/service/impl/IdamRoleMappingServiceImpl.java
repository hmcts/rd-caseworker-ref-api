package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IdamRoleMappingService;

import java.util.List;
import java.util.Set;
import javax.transaction.Transactional;

@Service
public class IdamRoleMappingServiceImpl implements IdamRoleMappingService {
    @Autowired
    CaseWorkerIdamRoleAssociationRepository cwIdamRoleAssocRepository;

    /**
     * Builds the idam role mappings for case worker roles.
     * @param caseWorkerIdamRoleAssociations list of CaseWorkerIdamRoleAssociation
     */
    @Override
    public void buildIdamRoleAssociation(
            List<CaseWorkerIdamRoleAssociation> caseWorkerIdamRoleAssociations) {
        cwIdamRoleAssocRepository.saveAll(caseWorkerIdamRoleAssociations);
    }

    /**
     * Deletes all the records for the service code provided.
     * @param serviceCode set of service code
     */
    @Override
    @Transactional
    public void deleteExistingRecordForServiceCode(Set<String> serviceCode) {
        cwIdamRoleAssocRepository.deleteByServiceCodeIn(serviceCode);
    }
}
