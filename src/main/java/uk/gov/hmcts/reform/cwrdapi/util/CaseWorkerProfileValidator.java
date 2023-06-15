package uk.gov.hmcts.reform.cwrdapi.util;

import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileUpdationRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CaseWorkerProfileValidator implements ConstraintValidator<ValidateCaseWorkerProfile,
    CaseWorkersProfileUpdationRequest> {

    @Override
    public boolean isValid(CaseWorkersProfileUpdationRequest caseWorkersProfileUpdationRequest,
                           ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();

        return true;
    }
}
