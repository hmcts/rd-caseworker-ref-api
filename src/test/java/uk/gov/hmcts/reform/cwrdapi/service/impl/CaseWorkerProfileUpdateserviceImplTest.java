package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileUpdationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkersProfileUpdationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerProfileRepository;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorStaffProfile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseWorkerProfileUpdateserviceImplTest {

    @InjectMocks
    private CaseWorkerProfileUpdateserviceImpl caseWorkerProfileUpdateservice;
    @Mock
    private CaseWorkerProfileRepository caseWorkerProfileRepository;
    private CaseWorkerProfile caseWorkerProfile;

    @Mock
    IJsrValidatorStaffProfile jsrValidatorStaffProfile;

    @Test
    void testCaseWorkerProfileUpdateService() {



        doReturn(Optional.of(buildCaseWorkerProfile()))
            .when(caseWorkerProfileRepository).findByCaseWorkerId("185a0254-ff80-458b-8f62-2a759788afd2");
        CaseWorkerProfile cwProfile = buildCaseWorkerProfile();
        cwProfile.setSuspended(false);
        CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest = CaseWorkersProfileUpdationRequest
            .caseWorkersProfileUpdationRequest().firstName("first_name")
            .lastName("last_name")
            .userId("185a0254-ff80-458b-8f62-2a759788afd2")
            .emailId("CWR-func-test-user@test.com")
            .suspended(false)
            .build();
        when(caseWorkerProfileRepository.save(any())).thenReturn(cwProfile);
        CaseWorkersProfileUpdationResponse caseWorkersProfileUpdationResponse =
            new CaseWorkersProfileUpdationResponse();
        caseWorkersProfileUpdationResponse = caseWorkerProfileUpdateservice
            .updateCaseWorkerProfile(caseWorkersProfileUpdationRequest);
        assertEquals(caseWorkersProfileUpdationResponse.getUserId(),caseWorkersProfileUpdationRequest.getUserId());
        verify(jsrValidatorStaffProfile, times(1)).validateCaseWorkerUpdateRequest(any(), any());
    }

    @Test
    void testCaseWorkerProfileUpdateServiceWhenCwIdNotPresnt() {
        CaseWorkersProfileUpdationResponse caseWorkersProfileUpdationResponse =
            new CaseWorkersProfileUpdationResponse();
        CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest = CaseWorkersProfileUpdationRequest
            .caseWorkersProfileUpdationRequest().firstName("first_name")
            .lastName("last_name")
            .userId("185a0254-ff80-458b-8f62-2a759788afd2")
            .emailId("CWR-func-test-user@test.com")
            .build();
        doReturn(Optional.empty())
            .when(caseWorkerProfileRepository).findByCaseWorkerId(anyString());
        Assertions.assertThrows(ResourceNotFoundException.class, () ->
            caseWorkerProfileUpdateservice.updateCaseWorkerProfile(caseWorkersProfileUpdationRequest));
    }

    CaseWorkerProfile buildCaseWorkerProfile() {

        caseWorkerProfile = new CaseWorkerProfile();
        caseWorkerProfile.setCaseWorkerId("185a0254-ff80-458b-8f62-2a759788afd2");
        caseWorkerProfile.setFirstName("CWFirstName");
        caseWorkerProfile.setLastName("CWLastName");
        caseWorkerProfile.setEmailId("CWR-func-test-user@test.com");
        caseWorkerProfile.setSuspended(true);
        return caseWorkerProfile;
    }
}
