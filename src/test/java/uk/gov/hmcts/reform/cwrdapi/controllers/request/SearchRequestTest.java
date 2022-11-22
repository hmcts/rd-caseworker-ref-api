package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SearchRequestTest {


    @Test
    void testSearchRequestWithConstructor() {

        SearchRequest request1 = new SearchRequest(
                "serviceId","locationId", "userTypeId", "jobTitleId",
                "skillId","roles");

        verify(request1);
    }


    void verify(SearchRequest request) {
        assertThat(request.getServiceCode()).isEqualTo("serviceId");
        assertThat(request.getLocation()).isEqualTo("locationId");
        assertThat(request.getUserType()).isEqualTo("userTypeId");
        assertThat(request.getJobTitle()).isEqualTo("jobTitleId");
        assertThat(request.getSkill()).isEqualTo("skillId");
        assertThat(request.getRole()).isEqualTo("roles");
    }

}
