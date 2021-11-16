package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RoleTest {

    @Test
    public void testRole() {
        Role role = new Role();
        role.setRoleId("1");
        role.setRoleName("role name");
        role.setCreatedTime(LocalDateTime.now());
        role.setLastUpdatedTime(LocalDateTime.now());
        role.setPrimary(true);

        assertNotNull(role);
        assertThat(role.getRoleId(), is("1"));
        assertThat(role.getRoleName(), is("role name"));
        assertThat(role.isPrimary(), is(true));
        assertNotNull(role.getLastUpdatedTime());
        assertNotNull(role.getCreatedTime());
    }

    @Test
    public void testRoleBuilder() {
        Role role = Role.builder()
                .roleId("1")
                .roleName("role name")
                .createdTime(LocalDateTime.now())
                .lastUpdatedTime(LocalDateTime.now())
                .isPrimary(false)
                .build();

        assertNotNull(role);
        assertThat(role.getRoleId(), is("1"));
        assertThat(role.getRoleName(), is("role name"));
        assertThat(role.isPrimary(), is(false));
        assertNotNull(role.getLastUpdatedTime());
        assertNotNull(role.getCreatedTime());

        String roleString = Role.builder()
                .roleId("1").toString();

        assertTrue(roleString.contains("Role.RoleBuilder(roleId=1"));
    }
}
