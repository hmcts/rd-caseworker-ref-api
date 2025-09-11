package uk.gov.hmcts.reform.cwrdapi.service;


import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileUpdationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;

public interface IJsrValidatorStaffProfile {

    /**
     * JSR validation.
     *
     * @param staffProfile staff profile creation request
     *
     */
    void validateStaffProfile(StaffProfileCreationRequest staffProfile,String operationType);

    void validateCaseWorkerUpdateRequest(CaseWorkersProfileUpdationRequest cwUpdateProfileRequest,
                                         String operationType);

}
