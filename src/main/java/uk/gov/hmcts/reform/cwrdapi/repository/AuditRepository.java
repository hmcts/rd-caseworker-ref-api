package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;

@Repository
public interface AuditRepository extends JpaRepository<CaseWorkerAudit, String> {

    @Query(value = "select count(*) from case_worker_audit where job_start_time\\:\\:date "
        + " >= current_date - 1  and authenticated_user_id = :authenticatedUserId  and status = :status",
        nativeQuery = true)
    long findByAuthenticatedUserIdAndStatus(String authenticatedUserId, String status);
}
