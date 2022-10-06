package uk.gov.hmcts.reform.cwrdapi.service;


import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;

import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import java.util.List;

@Service
public interface StaffRefDataService {


    ResponseEntity<List<SearchStaffUserResponse>> retrieveStaffUserByName(String searchString, PageRequest pageRequest);


    /**
     * Get List of Service skills.
     * @return StaffWorkerSkillResponse
     */
    StaffWorkerSkillResponse getServiceSkills();


    List<UserType> fetchUserTypes();


    List<RoleType> getJobTitles();

}
