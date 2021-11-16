package uk.gov.hmcts.reform.cwrdapi.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

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
                        "caseWorkerId", "ASC",
                        20, "id", CaseWorkerProfile.class);
        assertEquals(0, pageRequest.first().getPageNumber());
        assertEquals(1, pageRequest.first().getPageSize());
    }

    @Test
    public void testInvalidRequestExceptionForInvalidPageNumber() {
        Assertions.assertThrows(InvalidRequestException.class, () -> {
            validateAndBuildPaginationObject(-1, 1,
                    "caseWorkerId", "ASC",
                    20, "id", CaseWorkerProfile.class);
        });
    }

    @Test
    public void testInvalidRequestExceptionForInvalidPageSize() {
        Assertions.assertThrows(InvalidRequestException.class, () -> {
            validateAndBuildPaginationObject(0, -1,
                    "caseWorkerId", "ASC",
                    20, "id", CaseWorkerProfile.class);
        });
    }

    @Test
    public void testInvalidRequestExceptionForInvalidSortDirection() {
        Assertions.assertThrows(InvalidRequestException.class, () -> {
            validateAndBuildPaginationObject(0, 1,
                    "caseWorkerId", "Invalid",
                    20, "id", CaseWorkerProfile.class);
        });
    }

    @Test
    public void testConfigValueWhenPaginationParametersNotProvided() {
        PageRequest pageRequest =
                validateAndBuildPaginationObject(null, null,
                        null, null,
                        20, "caseWorkerId", CaseWorkerProfile.class);
        assertEquals(0, pageRequest.getPageNumber());
        assertEquals(20, pageRequest.getPageSize());
        assertTrue(pageRequest.getSort().get().anyMatch(i -> i.getDirection().isAscending()));
        assertTrue(pageRequest.getSort().get().anyMatch(i -> i.getProperty().equals("caseWorkerId")));
    }

    @Test
    public void testInvalidRequestExceptionForInvalidSortColumn() {
        Assertions.assertThrows(Exception.class, () -> {
            validateAndBuildPaginationObject(0, 1,
                    "invalid", "ASC",
                    20, "invalid", CaseWorkerProfile.class);
        });
    }
}
