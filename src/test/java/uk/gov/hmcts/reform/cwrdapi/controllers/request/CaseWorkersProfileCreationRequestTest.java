package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CaseWorkersProfileCreationRequestTest {

    Set<String> idamRoles = Set.of("caseworker");

    @Test
    void testUserProfileCreationRequestWithConstructor() {

        CaseWorkersProfileCreationRequest request1 = new CaseWorkersProfileCreationRequest(
                "firstName","lastName", "some@email.com",
                1, UserTypeRequest.INTERNAL.name(), "region", false,
                idamRoles, null, null, null, 1L, false,
                false);

        verify(request1);
    }

    void verify(CaseWorkersProfileCreationRequest request) {
        assertThat(request.getEmailId()).isEqualTo("some@email.com");
        assertThat(request.getFirstName()).isEqualTo("firstName");
        assertThat(request.getLastName()).isEqualTo("lastName");
        assertThat(request.getRegionId()).isEqualTo(1);
        assertThat(request.getUserType()).isEqualTo(UserTypeRequest.INTERNAL.name());
        assertThat(request.getRegion()).isEqualTo("region");
        assertThat(request.getIdamRoles()).isEqualTo(idamRoles);
        assertFalse(request.isCaseAllocator());
        assertFalse(request.isTaskSupervisor());
        assertThat(request.getIdamRoles()).isEqualTo(idamRoles);
    }
}
