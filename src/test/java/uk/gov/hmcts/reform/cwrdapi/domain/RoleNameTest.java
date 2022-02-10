package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleNameTest {

    @Test
    void testRoleName() {

        RoleName roleName = new RoleName("Role Name");
        roleName.setName("New Role Name");

        assertThat(roleName.getName()).isEqualTo("New Role Name");
    }
}
