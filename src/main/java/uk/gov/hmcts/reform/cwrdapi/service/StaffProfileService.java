package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

import java.util.List;

public interface StaffProfileService {

    StaffProfileCreationResponse processStaffProfileUpdate(StaffProfileCreationRequest staffProfileRequest);

    /**
     * Prepare caseworker data to be published as a message to topic.
     *
     * @param caseWorkerData list containing caseworker data
     */
    void publishCaseWorkerDataToTopic(List<CaseWorkerProfile> caseWorkerData);

}
