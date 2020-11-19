package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;

import java.util.List;

@Repository
public interface CaseWorkerIdamRoleAssociationRepository extends JpaRepository<CaseWorkerIdamRoleAssociation,Long> {

    List<CaseWorkerIdamRoleAssociation> findByRoleType(RoleType roleType);
}
