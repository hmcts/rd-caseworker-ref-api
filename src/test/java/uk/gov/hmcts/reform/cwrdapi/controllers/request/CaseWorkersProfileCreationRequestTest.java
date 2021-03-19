package uk.gov.hmcts.reform.cwrdapi.controllers.request;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class CaseWorkersProfileCreationRequestTest {

    Set<String> idamRoles = new HashSet(Arrays.asList("caseworker"));

    @Test
    public void testUserProfileCreationRequestWithConstructor() {

        CaseWorkersProfileCreationRequest request1 = new CaseWorkersProfileCreationRequest(
                "firstName","lastName", "some@email.com",
                1, UserTypeRequest.INTERNAL.name(), "region", false,
                idamRoles, null, null, null, 1L);

        verify(request1);
    }

    public void verify(CaseWorkersProfileCreationRequest request) {
        assertThat(request.getEmailId()).isEqualTo("some@email.com");
        assertThat(request.getFirstName()).isEqualTo("firstName");
        assertThat(request.getLastName()).isEqualTo("lastName");
        assertThat(request.getRegionId()).isEqualTo(1);
        assertThat(request.getUserType()).isEqualTo(UserTypeRequest.INTERNAL.name());
        assertThat(request.getRegion()).isEqualTo("region");
        assertThat(request.getIdamRoles()).isEqualTo(idamRoles);
    }
}
