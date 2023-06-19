package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.client.domain.SkillResponse;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SkillResponseTest {


    @Test
    void testSkillResposne_for_valid_data() {

        SkillResponse skillResponse = new SkillResponse();
        skillResponse.setSkillId(123L);
        skillResponse.setDescription("testresponsedescription");

        assertNotNull(skillResponse);
        assertThat(skillResponse.getSkillId(),is(123L));
        assertThat(skillResponse.getDescription(), is("testresponsedescription"));

    }

    @Test
    void testSkillResposne_for_empty_data() {

        SkillResponse skillResponse = new SkillResponse();
        skillResponse.setSkillId(123L);
        skillResponse.setDescription(" ");

        assertNotNull(skillResponse);
        assertThat(skillResponse.getSkillId(),is(123L));
        assertThat(skillResponse.getDescription(), is(" "));

    }


    @Test
    void testSkillResposne_for_tostring_builder() {

        SkillResponse skillResponse = SkillResponse.builder()
                .skillId(123L)
                .description("testresponsedescription")
                .build();

        assertNotNull(skillResponse);
        assertEquals(123L, skillResponse.getSkillId());
        assertEquals("testresponsedescription", skillResponse.getDescription());


        String description = SkillResponse.builder().description("test").toString();

        assertTrue(description.contains("SkillResponse.SkillResponseBuilder(skillId=null, description=test)"));

    }

}
