package uk.gov.hmcts.reform.cwrdapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.wiremock.WireMockTestEnvironment;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_CWD_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.wiremock.IdamWireMockStubs.stubIdamWithGivenRoleAndStatus;

public class CreateIdamRolesMappingIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private static WireMockServer idamMockServer = WireMockTestEnvironment.idam();
    private static StubMapping invalidRoleStub = null;

    @BeforeAll
    public static void setUp() {
        invalidRoleStub = stubIdamWithGivenRoleAndStatus(idamMockServer, "active", List.of(ROLE_CWD_ADMIN));
    }

    @AfterAll
    public static void cleanUp() {
        idamMockServer.removeStub(invalidRoleStub);
    }

    @Test
    public void returns_200_when_idam_roles_mapping_created_successfully() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
            .roleId(1)
            .idamRoles("testRole")
            .serviceId("BBAA1")
            .build();
        Map<String, Object> response = caseworkerReferenceDataClient
            .createIdamRolesAssoc(Collections.singletonList(serviceRoleMapping), cwdAdmin);

        assertThat(response).containsEntry("http_status", "201 CREATED");
    }

    @Test
    public void returns_403_for_invalid_role() {
        StubMapping invalidRoleStub = stubIdamWithGivenRoleAndStatus(idamMockServer, "active",List.of("invalid-role"));
        Map<String, Object> response = caseworkerReferenceDataClient
            .createIdamRolesAssoc(Collections.emptyList(), "invalid role");

        assertThat(response).containsEntry("http_status", "403");
        idamMockServer.removeStub(invalidRoleStub);
    }

    @Test
    void returns_400_when_request_invalid() {
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

    @Test
    public void returns_200_when_idam_roles_mapping_created_successfully_with_trim() {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping.builder()
                .roleId(1)
                .idamRoles(" test Role ")
                .serviceId(" BB A1 ")
                .build();
        Map<String, Object> response = caseworkerReferenceDataClient
                .createIdamRolesAssoc(Collections.singletonList(serviceRoleMapping), cwdAdmin);

        assertThat(response).containsEntry("http_status", "201 CREATED");
        List<CaseWorkerIdamRoleAssociation> associations = roleAssocRepository.findAll();
        CaseWorkerIdamRoleAssociation association = associations.get(0);
        assertThat(association.getIdamRole()).isEqualTo("test Role");
        assertThat(association.getServiceCode()).isEqualTo("BB A1");
    }
}