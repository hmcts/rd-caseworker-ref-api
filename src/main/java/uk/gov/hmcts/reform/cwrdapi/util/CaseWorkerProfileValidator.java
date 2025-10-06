package uk.gov.hmcts.reform.cwrdapi.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileUpdationRequest;


public class CaseWorkerProfileValidator implements ConstraintValidator<ValidateCaseWorkerProfile,
    CaseWorkersProfileUpdationRequest> {

    @Override
    public boolean isValid(CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest,
                           ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();

        return true;
    }
}
