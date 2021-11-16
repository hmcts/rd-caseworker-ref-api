package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CaseWorkerRoleRequestTest {

    @Test
    public void testCaseWorkerRoleRequest() {
        CaseWorkerRoleRequest request = new CaseWorkerRoleRequest("caseworker", true);
        assertThat(request.getRole()).isEqualTo("caseworker");
        assertThat(request.isPrimaryFlag()).isTrue();
    }

}
