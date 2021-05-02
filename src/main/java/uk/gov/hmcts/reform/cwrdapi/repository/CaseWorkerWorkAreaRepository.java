package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerWorkArea;

import java.util.List;
import java.util.Set;

@Repository
public interface CaseWorkerWorkAreaRepository extends JpaRepository<CaseWorkerWorkArea, Long> {
    void deleteByCaseWorkerProfileIn(List<CaseWorkerProfile> caseWorkerProfileList);

    Page<CaseWorkerWorkArea> findByServiceCodeIn(Set<String> serviceCode, Pageable pageable);

}
