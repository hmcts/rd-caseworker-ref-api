package uk.gov.hmcts.reform.cwrdapi.domain;
import org.hibernate.usertype.UserType;
import org.junit.Test;

import java.time.LocalDate;

import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class UserTypeTest {

    @Test
    public void testUserType() {

        UserType userType = new UserType();
        userType.setUserTypeId(1);
        userType.setDescription("Test Description");
        userType.setCreatedDate(LocalDate.now());
        userType.setLastUpdate(LocalDate.now());

        assertNotNull(userType);
        assertThat(userType.getUserTyoeId(), 1);
        assertThat(userType.getDescription(), "Test Description");
        assertThat(userType.getCreatedDate(), LocalDate.now());
        assertThat(userType.getLastUpdate(), LocalDate.now());

    }
}
