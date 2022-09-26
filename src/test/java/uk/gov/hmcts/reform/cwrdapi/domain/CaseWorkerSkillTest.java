package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("AbbreviationAsWordInName")
public class CaseWorkerSkillTest {

    @Test
    void testCaseWorkerSkill() {
        CaseWorkerSkill caseWorkerSkill = new CaseWorkerSkill();
        caseWorkerSkill.setCaseWorkerId("case_worker_id");
        caseWorkerSkill.setCaseWorkerSkillId(1L);
        caseWorkerSkill.setSkills(new ArrayList<>());
        caseWorkerSkill.setSkillId(1L);
        caseWorkerSkill.setCreatedDate(LocalDateTime.now());
        caseWorkerSkill.setLastUpdate(LocalDateTime.now());
        caseWorkerSkill.setCaseWorkerProfile(new CaseWorkerProfile());


        assertNotNull(caseWorkerSkill);
        assertNotNull(caseWorkerSkill.getSkills());
        assertNotNull(caseWorkerSkill.getCaseWorkerProfile());
        assertNotNull(caseWorkerSkill.getCreatedDate());
        assertNotNull(caseWorkerSkill.getLastUpdate());

        assertThat(caseWorkerSkill.getCaseWorkerId(), is("case_worker_id"));
        assertThat(caseWorkerSkill.getSkillId(), is(1L));
        assertThat(caseWorkerSkill.getCaseWorkerSkillId(),is(1L));

    }
}
