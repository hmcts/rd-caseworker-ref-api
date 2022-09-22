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
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.ServiceSkill;
import uk.gov.hmcts.reform.cwrdapi.service.StaffRefDataService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StaffRefDataControllerTest {

    @Mock
    StaffRefDataService staffRefDataService;

    @InjectMocks
    private StaffRefDataController staffRefDataController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_return_service_skills_with_status_code_200() {
        StaffWorkerSkillResponse staffWorkerSkillResponse =
                new StaffWorkerSkillResponse();
        when(staffRefDataService.getServiceSkills()).thenReturn(staffWorkerSkillResponse);
        ResponseEntity<StaffWorkerSkillResponse> responseEntity =
                staffRefDataController.retrieveAllServiceSkills();

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(staffRefDataService, times(1))
                .getServiceSkills();
    }

    @Test
    void should_return_200_when_no_skills_found() {
        StaffWorkerSkillResponse staffWorkerSkillResponse =
                new StaffWorkerSkillResponse();
        List<ServiceSkill> serviceSkills = new ArrayList<>();
        staffWorkerSkillResponse.setServiceSkills(serviceSkills);
        when(staffRefDataService.getServiceSkills()).thenReturn(staffWorkerSkillResponse);
        ResponseEntity<StaffWorkerSkillResponse> responseEntity =
                staffRefDataController.retrieveAllServiceSkills();

        assertNotNull(responseEntity);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(responseEntity.getBody().getServiceSkills().size(), 0);
    }
}
