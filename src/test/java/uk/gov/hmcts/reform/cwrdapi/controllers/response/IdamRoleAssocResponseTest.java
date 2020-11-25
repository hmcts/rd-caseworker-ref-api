package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IdamRoleAssocResponseTest {

    @Test
    public void test_no_args_constructor() {
        IdamRoleAssocResponse idamRoleAssocResponse = new IdamRoleAssocResponse();
        idamRoleAssocResponse.setMessage("testMessage");
        idamRoleAssocResponse.setStatusCode(200);

        assertThat(idamRoleAssocResponse.getMessage()).isEqualTo("testMessage");
        assertThat(idamRoleAssocResponse.getStatusCode()).isEqualTo(200);

    }


    @Test
    public void test_all_args_constructor() {
        IdamRoleAssocResponse idamRoleAssocResponse = new IdamRoleAssocResponse(200, "testMessage");

        assertThat(idamRoleAssocResponse.getMessage()).isEqualTo("testMessage");
        assertThat(idamRoleAssocResponse.getStatusCode()).isEqualTo(200);

    }

}