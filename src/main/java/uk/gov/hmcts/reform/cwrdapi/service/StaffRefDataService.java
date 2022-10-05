package uk.gov.hmcts.reform.cwrdapi.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.RoleType;
import uk.gov.hmcts.reform.cwrdapi.domain.UserType;

import java.util.List;

@Service
public interface StaffRefDataService {
    List<UserType> fetchUserTypes();

    List<RoleType> getJobTitles();

    StaffProfileCreationResponse processStaffProfileCreation(StaffProfileCreationRequest staffProfileRequest);

    /**
     * Prepare StaffProfile data to be published as a message to topic.
     *
     *
     */
    void publishStaffProfileToTopic(StaffProfileCreationResponse staffProfileCreationResponse);
}
