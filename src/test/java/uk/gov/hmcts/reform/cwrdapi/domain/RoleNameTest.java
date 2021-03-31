package uk.gov.hmcts.reform.cwrdapi.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleNameTest {

    @Test
    public void testRoleName() {

        RoleName roleName = new RoleName("Role Name");
        roleName.setName("New Role Name");

        assertThat(roleName.getName()).isEqualTo("New Role Name");
    }
}
