package uk.gov.hmcts.reform.cwrdapi.repository;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.domain.Skill;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkillRepositoryTest {
    private SkillRepository skillRepository = mock(SkillRepository.class);
    private List<Skill> skills = new ArrayList<>();

    @Test
    void findByServiceCodeTest() {
        when(skillRepository.findAll()).thenReturn(skills);
        assertNotNull(skillRepository.findAll());

    }
}
