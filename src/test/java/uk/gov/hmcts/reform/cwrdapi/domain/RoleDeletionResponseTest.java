package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.client.domain.RoleDeletionResponse;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RoleDeletionResponseTest {

    @Test
    void testSkillResposne_for_valid_data() {

        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse();

        roleDeletionResponse.setRoleName("role");
        roleDeletionResponse.setIdamStatusCode("active");
        roleDeletionResponse.setIdamMessage("message");

        assertNotNull(roleDeletionResponse);
        assertThat(roleDeletionResponse.getRoleName(),is("role"));
        assertThat(roleDeletionResponse.getIdamMessage(), is("message"));
        assertThat(roleDeletionResponse.getIdamStatusCode(), is("active"));

    }
}
