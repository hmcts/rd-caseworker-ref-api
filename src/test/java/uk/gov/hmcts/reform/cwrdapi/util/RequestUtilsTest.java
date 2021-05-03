package uk.gov.hmcts.reform.cwrdapi.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.trimIdamRoles;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateAndBuildPaginationObject;

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

    @Test
    public void testValidateAndBuildPaginationObject() {
        PageRequest pageRequest =
                validateAndBuildPaginationObject(0, 1,
                        "caseWorkerId", "ASC", "test",
                        20, "id");
        assertEquals(pageRequest.first().getPageNumber(), 0);
        assertEquals(pageRequest.first().getPageSize(), 1);
    }

    @Test(expected = InvalidRequestException.class)
    public void testInvalidRequestExceptionForInvalidPageNumber() {
        validateAndBuildPaginationObject(-1, 1,
                "caseWorkerId", "ASC", "test",
                20, "id");
    }

    @Test(expected = InvalidRequestException.class)
    public void testInvalidRequestExceptionForInvalidPageSize() {
        validateAndBuildPaginationObject(0, -1,
                "caseWorkerId", "ASC", "test",
                20, "id");
    }

    @Test(expected = InvalidRequestException.class)
    public void testInvalidRequestExceptionForInvalidSortDirection() {
        validateAndBuildPaginationObject(0, 1,
                "caseWorkerId", "Invalid", "test",
                20, "id");
    }

    @Test
    public void testConfigValueWhenPaginationParametersNotProvided() {
        PageRequest pageRequest =
                validateAndBuildPaginationObject(null, null,
                        null, null, "test",
                        20, "caseWorkerId");
        assertEquals(0, pageRequest.getPageNumber());
        assertEquals(20, pageRequest.getPageSize());
        assertTrue(pageRequest.getSort().get().anyMatch(i -> i.getDirection().isAscending()));
        assertTrue(pageRequest.getSort().get().anyMatch(i -> i.getProperty().equals("caseWorkerId")));
    }
}
