package uk.gov.hmcts.reform.cwrdapi.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.trimIdamRoles;

public class RequestUtilsTest {

    @Test
    public void testTrimIdamRoles() {
        Set<String> roles = ImmutableSet.of(" tribunal_case_worker ", " caseworker ", " ");
        List<CaseWorkersProfileCreationRequest> requests = ImmutableList.of(CaseWorkersProfileCreationRequest
                        .caseWorkersProfileCreationRequest().idamRoles(roles).build());
        trimIdamRoles(requests);
        assertThat(requests.get(0).getIdamRoles()).hasSize(2);
        assertThat(requests.get(0).getIdamRoles()).contains("tribunal_case_worker", "caseworker");
    }
}
