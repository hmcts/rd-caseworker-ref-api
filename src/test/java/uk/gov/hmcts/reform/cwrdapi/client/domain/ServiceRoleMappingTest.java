package uk.gov.hmcts.reform.cwrdapi.client.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ServiceRoleMappingTest {

    @Test
    public void testServiceRoleMapping() {
        ServiceRoleMapping serviceRoleMapping = new ServiceRoleMapping();
        serviceRoleMapping.setServiceId("1");
        serviceRoleMapping.setRoleId(1);
        serviceRoleMapping.setIdamRoles("role");

        assertNotNull(serviceRoleMapping);
        assertThat(serviceRoleMapping.getServiceId(), is("1"));
        assertThat(serviceRoleMapping.getRoleId(), is(1));
        assertThat(serviceRoleMapping.getIdamRoles(), is("role"));
    }

    @Test
    public void testServiceRoleMappingBuilder() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
                .serviceId("1")
                .roleId(1)
                .idamRoles("role")
                .build();

        assertNotNull(serviceRoleMapping);
        assertThat(serviceRoleMapping.getServiceId(), is("1"));
        assertThat(serviceRoleMapping.getRoleId(), is(1));
        assertThat(serviceRoleMapping.getIdamRoles(), is("role"));

        String serviceRoleMappingString = ServiceRoleMapping.builder()
                .serviceId("1").toString();
        assertTrue(serviceRoleMappingString.contains("ServiceRoleMapping.ServiceRoleMappingBuilder(serviceId=1"));
    }

}
