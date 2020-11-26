package uk.gov.hmcts.reform.cwrdapi;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringIntegrationSerenityRunner.class)
public class CreateIdamRolesMappingTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void returns_200_when_idam_roles_mapping_created_successfully() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
                .roleId(1)
                .idamRoles("testRole")
                .serivceId("BBAA1")
                .build();
        Map<String, Object> response = caseworkerReferenceDataClient
                .createIdamRolesAssoc(Collections.singletonList(serviceRoleMapping), cwdAdmin);

        assertThat(response).containsEntry("http_status", 200);
    }

    @Test
    public void returns_400_when_request_invalid() {

        Map<String, Object> response = caseworkerReferenceDataClient
                .createIdamRolesAssoc(Collections.emptyList(), cwdAdmin);

        assertThat(response).containsEntry("http_status", 400);
    }

    @Test
    public void returns_500_when_exception_occurs() {

        Map<String, Object> response = caseworkerReferenceDataClient
                .createIdamRolesAssoc(Collections.singletonList(null), cwdAdmin);

        assertThat(response).containsEntry("http_status", 500);

    }
}
