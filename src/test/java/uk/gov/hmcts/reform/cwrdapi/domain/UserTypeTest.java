package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class UserTypeTest {

    @Test
    public void testUserType() {

        UserType userType = new UserType();
        userType.setUserTypeId(1L);
        userType.setDescription("Test Description");
        userType.setCreatedDate(LocalDateTime.now());
        userType.setLastUpdate(LocalDateTime.now());

        assertNotNull(userType);
        assertThat(userType.getUserTypeId(), is(1L));
        assertThat(userType.getDescription(), is("Test Description"));
        assertNotNull(userType.getCreatedDate());
        assertNotNull(userType.getLastUpdate());

    }
}
