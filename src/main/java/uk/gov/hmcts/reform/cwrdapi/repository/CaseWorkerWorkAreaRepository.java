package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;

@Repository
public interface CaseWorkerWorkAreaRepository extends JpaRepository<CaseWorkerWorkArea,Long> {


}
