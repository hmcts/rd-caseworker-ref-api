package uk.gov.hmcts.reform.cwrdapi.controllers.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdamRolesMappingResponseTest {

    @Test
    void test_no_args_constructor() {
        IdamRolesMappingResponse idamRolesMappingResponse = new IdamRolesMappingResponse();
        idamRolesMappingResponse.setMessage("testMessage");
        idamRolesMappingResponse.setStatusCode(200);

        assertThat(idamRolesMappingResponse.getMessage()).isEqualTo("testMessage");
        assertThat(idamRolesMappingResponse.getStatusCode()).isEqualTo(200);

    }


    @Test
    void test_all_args_constructor() {
        IdamRolesMappingResponse idamRolesMappingResponse = new IdamRolesMappingResponse(200, "testMessage");

        assertThat(idamRolesMappingResponse.getMessage()).isEqualTo("testMessage");
        assertThat(idamRolesMappingResponse.getStatusCode()).isEqualTo(200);

    }

}