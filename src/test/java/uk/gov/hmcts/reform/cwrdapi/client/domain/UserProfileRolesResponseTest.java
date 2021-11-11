package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class UserProfileRolesResponseTest {

    @Test
    public void testLocation() {
        AttributeResponse attributeResponse = new AttributeResponse();
        attributeResponse.setIdamMessage("OK");
        attributeResponse.setIdamStatusCode(200);

        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamMessage("OK");
        roleAdditionResponse.setIdamStatusCode("200");

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfileRolesResponse.setAttributeResponse(attributeResponse);
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        assertNotNull(userProfileRolesResponse);
        assertFalse(attributeResponse.getIdamMessage().isEmpty());
        assertThat(userProfileRolesResponse.getAttributeResponse(), is(attributeResponse));
        assertThat(userProfileRolesResponse.getRoleAdditionResponse(), is(roleAdditionResponse));
    }

}
