package uk.gov.hmcts.reform.cwrdapi.service;


import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;

public interface IJsrValidatorStaffProfile {

    /**
     * JSR validation.
     *
     * @param staffProfile
     *
     */
    void validateStaffProfile(StaffProfileCreationRequest staffProfile,String operationType);

}
