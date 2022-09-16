package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;

@Service
public interface StaffRefDataService {
    StaffWorkerSkillResponse getServiceSkills();
}
