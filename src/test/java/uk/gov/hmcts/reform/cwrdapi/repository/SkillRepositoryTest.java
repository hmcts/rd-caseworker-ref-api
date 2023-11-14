package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkillRepositoryTest {
    private SkillRepository skillRepository = mock(SkillRepository.class);

    @Test
    void findAllSkillsTest() {
        List<Skill> skills = getSkillsData();
        when(skillRepository.findAll()).thenReturn(skills);
        assertNotNull(skillRepository.findAll());

        assertThat(skills).hasSize(1);
        Skill skill = skills.get(0);
        assertThat(skill.getServiceId()).isEqualTo("BBA3");
        assertThat(skill.getSkillId()).isEqualTo(1L);
        assertThat(skill.getSkillCode()).isEqualTo("A1");
        assertThat(skill.getDescription()).isEqualTo("desc1");
        assertThat(skill.getUserType()).isEqualTo("user_type1");
    }

    private List<Skill> getSkillsData() {
        Skill skill1 = new Skill();
        skill1.setServiceId("BBA3");
        skill1.setSkillId(1L);
        skill1.setSkillCode("A1");
        skill1.setDescription("desc1");
        skill1.setUserType("user_type1");

        List<Skill> skills = List.of(skill1);

        return skills;
    }
}
