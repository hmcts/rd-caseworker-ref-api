package uk.gov.hmcts.reform.cwrdapi.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StaffRefDataControllerTest {
    @InjectMocks
    private StaffRefDataController staffRefDataController;

    @Mock
    StaffRefDataService staffRefDataService;

    @Test
    void testGet_retrieveAllServiceSkills_Returns_200() {

        ResponseEntity<StaffWorkerSkillResponse> responseEntity =
                staffRefDataController.retrieveAllServiceSkills();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(staffRefDataService, times(1))
                .getServiceSkills();
    }
}
