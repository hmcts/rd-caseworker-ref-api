package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CaseWorkerSkillTest {
    @Test
    void testCaseWorkerSkill() {
        CaseWorkerSkill caseWorkerSkill = new CaseWorkerSkill();
        caseWorkerSkill.setCaseWorkerSkillId(1L);
        caseWorkerSkill.setCaseWorkerId("CWID1");
        caseWorkerSkill.setSkillId(1L);
        caseWorkerSkill.setCreatedDate(LocalDateTime.now());
        caseWorkerSkill.setLastUpdate(LocalDateTime.now());

        CaseWorkerProfile caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("CWID1");
        caseWorkerSkill.setCaseWorkerProfile(caseWorkerProfile);

        Skill skill = new Skill();
        skill.setSkillId(1L);
        skill.setSkillCode("ABA5");
        skill.setDescription("Training");
        caseWorkerSkill.setSkill(skill);

        assertNotNull(caseWorkerSkill);
        assertThat(caseWorkerSkill.getCaseWorkerSkillId(), is(1L));
        assertThat(caseWorkerSkill.getCaseWorkerId(), is("CWID1"));
        assertThat(caseWorkerSkill.getSkillId(), is(1L));
        assertNotNull(caseWorkerSkill.getCreatedDate());
        assertNotNull(caseWorkerSkill.getLastUpdate());

        assertNotNull(caseWorkerSkill.getCaseWorkerProfile());
        assertThat(caseWorkerSkill.getCaseWorkerProfile().getCaseWorkerId(), is("CWID1"));

        assertNotNull(caseWorkerSkill.getSkill());
        assertThat(caseWorkerSkill.getSkill().getSkillId(), is(1L));

        CaseWorkerSkill caseWorkerSkill1 = new CaseWorkerSkill("caseWorkerId", 1L);
        assertNotNull(caseWorkerSkill1);
        assertThat(caseWorkerSkill1.getCaseWorkerId(), is("caseWorkerId"));
        assertThat(caseWorkerSkill1.getSkillId(), is(1L));

    }
}
