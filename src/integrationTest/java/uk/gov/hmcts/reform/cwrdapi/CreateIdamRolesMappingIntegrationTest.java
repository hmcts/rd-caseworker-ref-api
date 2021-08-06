package uk.gov.hmcts.reform.cwrdapi;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

public class CreateIdamRolesMappingIntegrationTest extends AuthorizationEnabledIntegrationTest {
    @Autowired
    CaseWorkerIdamRoleAssociationRepository cwIdamRoleAssocRepository;

    @Before
    public void setUpClient() {

        super.setUpClient();
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

        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        Map<String, Object> response = caseworkerReferenceDataClient
            .createIdamRolesAssoc(Collections.emptyList(), "invalid role");

        assertThat(response).containsEntry("http_status", "403");
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @Test
    public void returns_400_when_request_invalid() {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        Map<String, Object> response = caseworkerReferenceDataClient
            .createIdamRolesAssoc(Collections.emptyList(), cwdAdmin);

        assertThat(response).containsEntry("http_status", "400");
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
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