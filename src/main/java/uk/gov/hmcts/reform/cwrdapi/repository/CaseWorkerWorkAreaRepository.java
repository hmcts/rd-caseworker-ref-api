package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;

import java.util.List;

@Repository
public interface CaseWorkerWorkAreaRepository extends JpaRepository<CaseWorkerWorkArea, Long> {
    void deleteByCaseWorkerProfileIn(List<CaseWorkerProfile> caseWorkerProfileList);

    void deleteByCaseWorkerProfile(CaseWorkerProfile caseWorkerProfileList);
}
