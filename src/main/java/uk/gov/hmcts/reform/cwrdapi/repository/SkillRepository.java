package uk.gov.hmcts.reform.cwrdapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;

import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {


    @Query(value = """
            SELECT staffSkill FROM skill staffSkill
            WHERE staffSkill.serviceId IN (:serviceCodes)
            """)
    List<Skill> getSkillsByServiceCodes(List<String> serviceCodes);

}

