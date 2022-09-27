package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.service.StaffProfileService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffRefDataControllerTest {

    @Mock
    StaffProfileService staffProfileService;

    @InjectMocks
    private StaffRefDataController staffRefDataController;

    StaffProfileCreationRequest request;
    StaffProfileCreationResponse response;
    ResponseEntity<StaffProfileCreationResponse> responseEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = StaffProfileCreationRequest.staffProfileCreationRequest().build();

        response = StaffProfileCreationResponse.builder()
                .caseWorkerId(UUID.randomUUID().toString())
                .build();

        responseEntity = new ResponseEntity<>(
                response,
                null,
                HttpStatus.OK
        );
    }

    @Test
    void should_return_staffCreateResponse_with_status_code_200() {
        when(staffProfileService.processStaffProfileCreation(request))
                .thenReturn(response);

        ResponseEntity<StaffProfileCreationResponse> actual = staffRefDataController
                .createStaffUserProfile(request);
        assertThat(actual.getStatusCodeValue()).isEqualTo(201);
    }

}