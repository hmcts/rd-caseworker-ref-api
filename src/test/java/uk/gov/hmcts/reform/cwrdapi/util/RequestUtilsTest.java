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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.removeEmptySpaces;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.trimIdamRoles;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateAndBuildPagination;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateAndBuildPaginationObject;
import static uk.gov.hmcts.reform.cwrdapi.util.RequestUtils.validateSearchString;

class RequestUtilsTest {

    @Test
    void testTrimIdamRoles() {
        Set<String> roles = ImmutableSet.of(" tribunal_case_worker ", " caseworker ", " ");
        List<CaseWorkersProfileCreationRequest> requests = ImmutableList.of(CaseWorkersProfileCreationRequest
                .caseWorkersProfileCreationRequest().idamRoles(roles).build());
        trimIdamRoles(requests);
        assertThat(requests.get(0).getIdamRoles()).hasSize(2);
        assertThat(requests.get(0).getIdamRoles()).contains("tribunal_case_worker", "caseworker");
    }

    @Test
    void testValidateAndBuildPaginationObject() {
        PageRequest pageRequest =
                validateAndBuildPaginationObject(0, 1,
                        "caseWorkerId", "ASC",
                        20, "id", CaseWorkerProfile.class);
        assertEquals(0, pageRequest.first().getPageNumber());
        assertEquals(1, pageRequest.first().getPageSize());
    }

    @Test
    void testInvalidRequestExceptionForInvalidPageNumber() {
        Assertions.assertThrows(InvalidRequestException.class, () ->
            validateAndBuildPaginationObject(-1, 1,
                    "caseWorkerId", "ASC",
                    20, "id", CaseWorkerProfile.class));
    }

    @Test
    void testInvalidRequestExceptionForInvalidPageSize() {
        Assertions.assertThrows(InvalidRequestException.class, () ->
            validateAndBuildPaginationObject(0, -1,
                    "caseWorkerId", "ASC",
                    20, "id", CaseWorkerProfile.class));
    }

    @Test
    void testInvalidRequestExceptionForInvalidSortDirection() {
        Assertions.assertThrows(InvalidRequestException.class, () ->
            validateAndBuildPaginationObject(0, 1,
                    "caseWorkerId", "Invalid",
                    20, "id", CaseWorkerProfile.class));
    }

    @Test
    void testConfigValueWhenPaginationParametersNotProvided() {
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
    void testInvalidRequestExceptionForInvalidSortColumn() {
        Assertions.assertThrows(Exception.class, () ->
            validateAndBuildPaginationObject(0, 1,
                    "invalid", "ASC",
                    20, "invalid", CaseWorkerProfile.class));
    }

    @Test
    void testRemoveEmptySpaces() {
        String emptySpaces = "   TestData   ";

        String result = removeEmptySpaces(emptySpaces);
        assertEquals("TestData",result);
    }

    @Test
    void testValidateAndBuildPagination() {

        PageRequest pageRequest =
                validateAndBuildPagination(20,1,10,1);
        assertEquals(0, pageRequest.first().getPageNumber());
        assertEquals(20, pageRequest.first().getPageSize());
    }

    @Test
    void testInvalidRequestExceptionForValidateAndBuildPaginationPageNumberZero() {
        Assertions.assertThrows(InvalidRequestException.class, () ->
                validateAndBuildPagination(20,0,10,1)
        );
    }

    @Test
    void testInvalidRequestExceptionForValidateAndBuildPaginationPageSizeZero() {
        Assertions.assertThrows(InvalidRequestException.class, () ->
                validateAndBuildPagination(0,1,10,1)
        );
    }

    @Test
    void testInvalidRequestExceptionForValidateSearchStringLenLessthan3() {
        Assertions.assertThrows(InvalidRequestException.class, () ->
                validateSearchString("ab")
        );
    }

    @Test
    void testInvalidRequestExceptionForValidateSearchStringEmpty() {
        Assertions.assertThrows(InvalidRequestException.class, () ->
                validateSearchString(" ")
        );
    }

    @Test
    void testInvalidRequestExceptionForValidateSearchStringNotValid() {
        Assertions.assertThrows(InvalidRequestException.class, () ->
                validateSearchString("abcd123")
        );
    }
}
