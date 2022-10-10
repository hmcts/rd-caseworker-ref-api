package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerSkill;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

import java.util.List;

@Repository
public interface CaseWorkerSkillRepository extends JpaRepository<CaseWorkerSkill, Long> {
    void deleteByCaseWorkerProfileIn(List<CaseWorkerProfile> caseWorkerProfileList);

}
