package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserProfileRolesResponseTest {

    @Test
    void testLocation() {
        AttributeResponse attributeResponse = new AttributeResponse();
        attributeResponse.setIdamMessage("OK");
        attributeResponse.setIdamStatusCode(200);
        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse();
        roleDeletionResponse.setRoleName("role");
        roleDeletionResponse.setIdamMessage("message");
        roleDeletionResponse.setIdamStatusCode("OK");
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamMessage("OK");
        roleAdditionResponse.setIdamStatusCode("200");

        List<RoleDeletionResponse> roleDeletionResponses = new ArrayList<>();
        roleDeletionResponses.add(roleDeletionResponse);
        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileRolesResponse.setAttributeResponse(attributeResponse);
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);
        userProfileRolesResponse.setRoleDeletionResponse(roleDeletionResponses);

        assertNotNull(userProfileRolesResponse);
        assertFalse(attributeResponse.getIdamMessage().isEmpty());
        assertThat(userProfileRolesResponse.getAttributeResponse(), is(attributeResponse));
        assertThat(userProfileRolesResponse.getRoleAdditionResponse(), is(roleAdditionResponse));
        assertThat(userProfileRolesResponse.getRoleDeletionResponse(), is(roleDeletionResponses));
    }

}
