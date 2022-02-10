package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProfileUpdatedDataTest {

    @Test
    void test_should_add_roles_add_when_modified() {

        UserProfileUpdatedData userProfileUpdatedData =  UserProfileUpdatedData.builder().idamStatus("ACTIVE").build();
        assertThat(userProfileUpdatedData.getIdamStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void testUserProfileUpdatedDataBuilder() {
        Set<RoleName> rolesAdd = new HashSet<>();
        rolesAdd.add(new RoleName("Role Name"));

        UserProfileUpdatedData userProfileUpdatedData = UserProfileUpdatedData.builder()
                .idamStatus("ACTIVE")
                .rolesAdd(rolesAdd)
                .build();

        assertThat(userProfileUpdatedData.getIdamStatus()).isEqualTo("ACTIVE");
        assertThat(userProfileUpdatedData.getRolesAdd()).isNotEmpty();

        String userProfileUpdatedDataString = UserProfileUpdatedData.builder().idamStatus("ACTIVE").toString();

        assertTrue(userProfileUpdatedDataString
                .contains("UserProfileUpdatedData.UserProfileUpdatedDataBuilder(idamStatus=ACTIVE"));
    }
}