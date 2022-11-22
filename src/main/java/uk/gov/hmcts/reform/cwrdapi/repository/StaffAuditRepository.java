package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.StaffAudit;

@Repository
public interface StaffAuditRepository extends JpaRepository<StaffAudit, Long> {

}
