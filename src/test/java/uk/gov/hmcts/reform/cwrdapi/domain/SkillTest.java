package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SkillTest {
    @Test
    void testSkill() {
        Skill skill1 = new Skill();
        skill1.setSkillId(1L);
        skill1.setDescription("Test Description");
        skill1.setSkillCode("Training");
        skill1.setServiceId("Service");
        skill1.setUserType("userType");
        skill1.setCreatedDate(LocalDateTime.now());
        skill1.setLastUpdate(LocalDateTime.now());

        assertNotNull(skill1);
        assertThat(skill1.getSkillId(), is(1L));
        assertThat(skill1.getDescription(), is("Test Description"));
        assertThat(skill1.getSkillCode(), is("Training"));
        assertThat(skill1.getServiceId(), is("Service"));
        assertThat(skill1.getUserType(), is("userType"));
        assertNotNull(skill1.getCreatedDate());
        assertNotNull(skill1.getLastUpdate());

    }

}
