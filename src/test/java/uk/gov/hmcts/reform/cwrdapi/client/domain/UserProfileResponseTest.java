package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserProfileResponseTest {

    @Test
    void testUserProfileResponse() {
        List<String> roles = new ArrayList<>();
        roles.add("role1");
        roles.add("role2");

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setIdamId("1");
        userProfileResponse.setFirstName("first");
        userProfileResponse.setLastName("last");
        userProfileResponse.setEmail("email@email.com");
        userProfileResponse.setIdamStatus("OK");
        userProfileResponse.setIdamStatusCode("200");
        userProfileResponse.setRoles(roles);

        assertNotNull(userProfileResponse);
        assertThat(userProfileResponse.getIdamId(), is("1"));
        assertThat(userProfileResponse.getFirstName(), is("first"));
        assertThat(userProfileResponse.getLastName(), is("last"));
        assertThat(userProfileResponse.getEmail(), is("email@email.com"));
        assertThat(userProfileResponse.getIdamStatus(), is("OK"));
        assertThat(userProfileResponse.getIdamStatusCode(), is("200"));
        assertFalse(userProfileResponse.getRoles().isEmpty());
        assertThat(userProfileResponse.getRoles().size(), is(2));
    }

}
