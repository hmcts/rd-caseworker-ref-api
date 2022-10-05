package uk.gov.hmcts.reform.cwrdapi.service;

import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

import java.util.List;

public interface StaffProfileService {

    StaffProfileCreationResponse processStaffProfileCreation(StaffProfileCreationRequest staffProfileRequest);

    StaffProfileCreationResponse updateStaffProfile(StaffProfileCreationRequest staffProfileRequest);


    /**
     * Prepare StaffProfile data to be published as a message to topic.
     *
     *
     */
    void publishStaffProfileToTopic(StaffProfileCreationResponse staffProfileCreationResponse);

    List<CaseWorkerProfile> updateStaffProfiles(List<StaffProfileCreationRequest>
                                                              caseWorkersProfileCreationRequest);

}
