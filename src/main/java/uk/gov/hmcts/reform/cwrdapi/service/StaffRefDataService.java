package uk.gov.hmcts.reform.cwrdapi.service;


import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.SearchRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.SearchStaffUserResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffWorkerSkillResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import java.util.List;

@Service
public interface StaffRefDataService {


    ResponseEntity<List<SearchStaffUserResponse>> retrieveStaffUserByName(String searchString, PageRequest pageRequest);


    /**
     * Get List of Service skills.
     * @param serviceCodes List of service codes
     * @return StaffWorkerSkillResponse
     */
    StaffWorkerSkillResponse getServiceSkills(String serviceCodes);

    List<UserType> fetchUserTypes();


    List<RoleType> getJobTitles();

    StaffProfileCreationResponse processStaffProfileCreation(StaffProfileCreationRequest staffProfileRequest);

    /**
     * Prepare StaffProfile data to be published as a message to topic.
     *
     *
     */
    void publishStaffProfileToTopic(StaffProfileCreationResponse staffProfileCreationResponse);

    ResponseEntity<List<SearchStaffUserResponse>> retrieveStaffProfile(SearchRequest searchRequest,
                                                                       PageRequest pageRequest);



    StaffProfileCreationResponse updateStaffProfile(StaffProfileCreationRequest staffProfileRequest);
}
