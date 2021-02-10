package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<CaseWorkerAudit, String> {

    List<CaseWorkerAudit> findByAuthenticatedUserId(String authenticatedUserId);
}
