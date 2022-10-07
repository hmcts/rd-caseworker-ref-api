package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillTest {

    @Test
    void testSkill() {
        Skill skill = new Skill();
        skill.setSkillId(1L);
        skill.setSkillCode("1");
        skill.setDescription("Test Skill");

        assertNotNull(skill);
        assertThat(skill.getSkillId(), is(1L));
        assertThat(skill.getSkillCode(), is("1"));
        assertThat(skill.getDescription(), is("Test Skill"));
    }

    @Test
    void testSkillBuilder() {
        Skill skill = Skill.builder()
                .skillId(1L)
                .skillCode("1")
                .description("Test Skill")
                .build();

        assertNotNull(skill);
        assertThat(skill.getSkillId(), is(1L));
        assertThat(skill.getSkillCode(), is("1"));
        assertThat(skill.getDescription(), is("Test Skill"));

        String skillString = Skill.builder()
                .skillId(1L).toString();

        assertTrue(skillString.contains("Skill.SkillBuilder(skillId=" + 1L));
    }

}
