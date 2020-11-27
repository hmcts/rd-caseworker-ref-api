package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IdamRolesMappingResponseTest {

    @Test
    public void test_no_args_constructor() {
        IdamRolesMappingResponse idamRolesMappingResponse = new IdamRolesMappingResponse();
        idamRolesMappingResponse.setMessage("testMessage");
        idamRolesMappingResponse.setStatusCode(200);

        assertThat(idamRolesMappingResponse.getMessage()).isEqualTo("testMessage");
        assertThat(idamRolesMappingResponse.getStatusCode()).isEqualTo(200);

    }


    @Test
    public void test_all_args_constructor() {
        IdamRolesMappingResponse idamRolesMappingResponse = new IdamRolesMappingResponse(200, "testMessage");

        assertThat(idamRolesMappingResponse.getMessage()).isEqualTo("testMessage");
        assertThat(idamRolesMappingResponse.getStatusCode()).isEqualTo(200);

    }

}