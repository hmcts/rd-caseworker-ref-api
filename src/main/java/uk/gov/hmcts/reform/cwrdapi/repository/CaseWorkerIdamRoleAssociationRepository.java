package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;

import java.util.List;
import java.util.Set;
import javax.transaction.Transactional;

@Repository
@Transactional
public interface CaseWorkerIdamRoleAssociationRepository extends JpaRepository<CaseWorkerIdamRoleAssociation,Long> {

    List<CaseWorkerIdamRoleAssociation> findByRoleTypeInAndServiceCodeIn(
            List<RoleType> roleType, List<String> serviceCodes);

    /**
     * Deletes the record for all the service code.
     * @param serviceCode set of service code
     */
    void deleteByServiceCodeIn(Set<String> serviceCode);
}
