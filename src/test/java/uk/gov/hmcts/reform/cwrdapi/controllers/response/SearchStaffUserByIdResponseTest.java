package uk.gov.hmcts.reform.cwrdapi.controllers.response;


import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

class SearchStaffUserByIdResponseTest {

    @Test
    void test_has_mandatory_fields_specified_not_null() {

        SearchStaffUserByIdResponse searchStaffUserByIdResponse;

        searchStaffUserByIdResponse = SearchStaffUserByIdResponse.withIdBuilder()
                .firstName("firstName")
                .lastName("lastName")
                .emailId("emailId")
                .services(new ArrayList<>())
                .region("region")
                .regionId(123)
                .roles(new ArrayList<>())
                .taskSupervisor(true)
                .caseAllocator(true)
                .suspended(false)
                .staffAdmin(true)
                .baseLocations(new ArrayList<>())
                .skills(new ArrayList<>())
                .idamStatus("Pending")
                .build();

        assertThat(searchStaffUserByIdResponse.getFirstName()).isEqualTo("firstName");
        assertThat(searchStaffUserByIdResponse.getLastName()).isEqualTo("lastName");
        assertThat(searchStaffUserByIdResponse.getEmailId()).isEqualTo("emailId");
        assertThat(searchStaffUserByIdResponse.getServices()).isEmpty();

        assertThat(searchStaffUserByIdResponse.getRegion()).isEqualTo("region");
        assertThat(searchStaffUserByIdResponse.getRegionId()).isEqualTo(123);
        assertThat(searchStaffUserByIdResponse.getRoles()).isEmpty();
        assertThat(searchStaffUserByIdResponse.isTaskSupervisor()).isTrue();
        assertThat(searchStaffUserByIdResponse.isCaseAllocator()).isTrue();
        assertThat(searchStaffUserByIdResponse.isSuspended()).isFalse();
        assertThat(searchStaffUserByIdResponse.isStaffAdmin()).isTrue();
        assertThat(searchStaffUserByIdResponse.getBaseLocations()).isEmpty();
        assertThat(searchStaffUserByIdResponse.getSkills()).isEmpty();
        assertThat(searchStaffUserByIdResponse.getIdamStatus()).isEqualTo("Pending");

    }
}
