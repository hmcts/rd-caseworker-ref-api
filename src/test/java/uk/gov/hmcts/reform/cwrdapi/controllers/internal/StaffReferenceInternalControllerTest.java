package uk.gov.hmcts.reform.cwrdapi.controllers.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.util.RequestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffReferenceInternalControllerTest {
    @InjectMocks
    private StaffReferenceInternalController staffReferenceInternalController;
    @Mock
    CaseWorkerService caseWorkerServiceMock;
    ResponseEntity<Object> responseEntity;

    @Test
    void shouldFetchStaffByCcdServiceNames() {
        responseEntity = ResponseEntity.ok().body(null);
        when(caseWorkerServiceMock.fetchStaffProfilesForRoleRefresh(any(), any()))
                .thenReturn(responseEntity);

        PageRequest pageRequest = RequestUtils.validateAndBuildPaginationObject(0, 1,
                "caseWorkerId", "ASC",
                20, "id", CaseWorkerProfile.class);

        ResponseEntity<?> actual = staffReferenceInternalController
                .fetchStaffByCcdServiceNames("cmc", 1, 0,
                        "ASC", "caseWorkerId");

        assertNotNull(actual);
        verify(caseWorkerServiceMock, times(1))
                .fetchStaffProfilesForRoleRefresh("cmc", pageRequest);
    }

    @Test
    void shouldThrowInvalidRequestExceptionForEmptyServiceName() {
        Assertions.assertThrows(InvalidRequestException.class, () ->
            staffReferenceInternalController
                    .fetchStaffByCcdServiceNames("", 1, 0,
                            "ASC", "caseWorkerId"));
    }
}