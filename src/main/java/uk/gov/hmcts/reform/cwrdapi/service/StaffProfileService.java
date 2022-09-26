package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;

public interface StaffProfileService {

    StaffProfileCreationResponse processStaffProfileCreation(StaffProfileCreationRequest staffProfileRequest);

    /**
     * Prepare caseworker data to be published as a message to topic.
     *
     * @param caseWorkerData list containing caseworker data
     */
    void publishStaffProfileToTopic(StaffProfileCreationResponse staffProfileCreationResponse);

}
