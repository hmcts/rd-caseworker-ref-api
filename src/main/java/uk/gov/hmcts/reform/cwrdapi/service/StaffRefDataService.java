package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import java.util.List;
@Service
public interface StaffRefDataService {
    /**
     * Get List of Service skills.
     * @return StaffWorkerSkillResponse
     */
    StaffWorkerSkillResponse getServiceSkills();

    List<UserType> fetchUserTypes();

}
