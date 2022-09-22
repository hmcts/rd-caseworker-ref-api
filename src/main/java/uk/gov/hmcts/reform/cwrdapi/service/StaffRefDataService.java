package uk.gov.hmcts.reform.cwrdapi.service;


import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;

@Service
public interface StaffRefDataService {

    ResponseEntity<Object> retrieveStaffUserByName(String searchString, PageRequest pageRequest);

    /**
     * Get List of Service skills.
     * @return StaffWorkerSkillResponse
     */
    StaffWorkerSkillResponse getServiceSkills();

}
