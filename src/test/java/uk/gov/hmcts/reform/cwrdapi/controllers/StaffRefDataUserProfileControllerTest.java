package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffRefDataUserProfileControllerTest {
    @Mock
    StaffRefDataService staffRefDataService;
    @InjectMocks
    private StaffRefDataController staffRefDataController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldUpdateStaffUserProfileTest() {
        StaffProfileCreationRequest staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                        .build();

        StaffProfileCreationResponse staffProfileCreationResponse = StaffProfileCreationResponse
                .builder().build();

        when(staffRefDataService.updateStaffProfile(staffProfileCreationRequest))
                .thenReturn(staffProfileCreationResponse);

        staffRefDataController.updateStaffUserProfile(staffProfileCreationRequest);

        verify(staffRefDataService,times(1))
                .updateStaffProfile(staffProfileCreationRequest);
    }

    @Test
    void shouldUpdateStaffUserProfileEmptyResponseTest() {
        StaffProfileCreationRequest staffProfileCreationRequest = StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .build();

        StaffProfileCreationResponse staffProfileCreationResponse = null;

        when(staffRefDataService.updateStaffProfile(staffProfileCreationRequest))
                .thenReturn(staffProfileCreationResponse);

        staffRefDataController.updateStaffUserProfile(staffProfileCreationRequest);

        verify(staffRefDataService,times(1))
                .updateStaffProfile(staffProfileCreationRequest);
    }
}
