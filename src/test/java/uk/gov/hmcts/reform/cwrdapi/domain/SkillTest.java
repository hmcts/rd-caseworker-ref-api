package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("AbbreviationAsWordInName")

class SkillTest {

    @Test
    void testSkill() {
        Skill skill = new Skill();
        skill.setServiceId("BBA3");
        skill.setSkillId(1L);
        skill.setSkillCode("A1");
        skill.setDescription("desc1");
        skill.setUserType("user_type1");
        skill.setCreatedDate(LocalDateTime.now());
        skill.setLastUpdate(LocalDateTime.now());

        CaseWorkerSkill caseWorkerSkill = new CaseWorkerSkill();
        skill.setCaseWorkerSkill(caseWorkerSkill);

        assertNotNull(skill);
        assertNotNull(skill.getCaseWorkerSkill());
        assertThat(skill.getServiceId(), is("BBA3"));
        assertThat(skill.getSkillId(), is(1L));
        assertThat(skill.getSkillCode(), is("A1"));
        assertThat(skill.getDescription(), is("desc1"));
        assertThat(skill.getUserType(), is("user_type1"));
        assertNotNull(skill.getCreatedDate());
        assertNotNull(skill.getLastUpdate());

    }

    @Test
    void testSkillDTO() {
        SkillDTO skill = new SkillDTO();
        skill.setServiceId("BBA3");
        skill.setSkillId(1L);
        skill.setSkillCode("A1");
        skill.setDescription("desc1");
        skill.setUserType("user_type1");

        assertNotNull(skill);
        assertThat(skill.getServiceId(), is("BBA3"));
        assertThat(skill.getSkillId(), is(1L));
        assertThat(skill.getSkillCode(), is("A1"));
        assertThat(skill.getDescription(), is("desc1"));
        assertThat(skill.getUserType(), is("user_type1"));

    }

    @Test
    void testServiceSkill() {
        SkillDTO skill = new SkillDTO();
        skill.setServiceId("BBA3");
        skill.setSkillId(1L);
        skill.setSkillCode("A1");
        skill.setDescription("desc1");
        skill.setUserType("user_type1");
        List<SkillDTO> skills = new ArrayList<>();
        skills.add(skill);
        ServiceSkill serviceSkill = new ServiceSkill();
        serviceSkill.setId("BBA3");
        serviceSkill.setSkills(skills);

        assertNotNull(serviceSkill);
        assertThat(serviceSkill.getId(), is("BBA3"));
        assertNotNull(serviceSkill.getSkills());
        skill = serviceSkill.getSkills().get(0);
        assertThat(skill.getSkillId(), is(1L));
        assertThat(skill.getSkillCode(), is("A1"));
        assertThat(skill.getDescription(), is("desc1"));
        assertThat(skill.getUserType(), is("user_type1"));

    }
}
