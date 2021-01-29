package uk.gov.hmcts.reform.cwrdapi;

import org.junit.Test;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateIdamRolesMappingIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void returns_200_when_idam_roles_mapping_created_successfully() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
                .roleId(1)
                .idamRoles("testRole")
                .serivceId("BBAA1")
                .build();
        Map<String, Object> response = caseworkerReferenceDataClient
                .createIdamRolesAssoc(Collections.singletonList(serviceRoleMapping), cwdAdmin);

        assertThat(response).containsEntry("http_status", "201 CREATED");
    }

    @Test
    public void returns_403_for_invalid_role() {

        Map<String, Object> response = caseworkerReferenceDataClient
                .createIdamRolesAssoc(Collections.emptyList(), "invalid role");

        assertThat(response).containsEntry("http_status", "403");
    }

    @Test
    public void returns_400_when_request_invalid() {
        Map<String, Object> response = caseworkerReferenceDataClient
                .createIdamRolesAssoc(Collections.emptyList(), cwdAdmin);

        assertThat(response).containsEntry("http_status", "400");
    }

    @Test
    public void returns_500_when_exception_occurs() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
                .roleId(0)
                .build();
        Map<String, Object> response = caseworkerReferenceDataClient
                .createIdamRolesAssoc(Collections.singletonList(serviceRoleMapping), cwdAdmin);

        assertThat(response).containsEntry("http_status", "500");

    }


}