package uk.gov.hmcts.reform.cwrdapi.util;

import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;

import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.lang.Boolean.FALSE;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.BooleanUtils.negate;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.AREA_OF_WORK_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_PRIMARY_AND_SECONDARY_LOCATIONS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_PRIMARY_AND_SECONDARY_ROLES;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LOCATION_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_PRIMARY_LOCATION_PRESENT;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_FIELD;

public class CaseWorkerChildListValidator implements ConstraintValidator<ValidateCaseWorkerChildren,
    CaseWorkerProfile> {

    @Override
    public boolean isValid(CaseWorkerProfile caseWorkerProfile, ConstraintValidatorContext context) {
        //disable existing violation message
        context.disableDefaultConstraintViolation();
        return (isValidRoles(caseWorkerProfile, context)
            && isValidLocations(caseWorkerProfile, context)
            && isValidAreaOfWk(caseWorkerProfile, context));
    }

    private boolean isValidAreaOfWk(CaseWorkerProfile caseWorkerProfile, ConstraintValidatorContext context) {
        boolean isValidAreaOfWk = true;
        if (isNotEmpty(caseWorkerProfile.getWorkAreas())
            && caseWorkerProfile.getWorkAreas().size() > 1
            && caseWorkerProfile.getWorkAreas().stream()
            .map(WorkArea::getServiceCode)
            .collect(Collectors.toSet()).size()
            != caseWorkerProfile.getWorkAreas().size()) {
            isValidAreaOfWk = false;
            context.buildConstraintViolationWithTemplate(DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK)
                .addPropertyNode(AREA_OF_WORK_FIELD)
                .addConstraintViolation();
        }
        return isValidAreaOfWk;
    }

    public boolean isValidLocations(CaseWorkerProfile caseWorkerProfile, ConstraintValidatorContext context) {
        boolean isValidLocations = true;
        if (isNotEmpty(caseWorkerProfile.getLocations())
            && caseWorkerProfile.getLocations().size() > 1) {
            //@TO do remove getLocationName with Id problem with excel sheet
            isValidLocations = negate(caseWorkerProfile.getLocations().get(0).getLocationName()
                .equalsIgnoreCase(caseWorkerProfile.getLocations().get(1).getLocationName()));
            if (FALSE.equals(isValidLocations)) {
                context.buildConstraintViolationWithTemplate(DUPLICATE_PRIMARY_AND_SECONDARY_LOCATIONS)
                    .addPropertyNode(LOCATION_FIELD)
                    .addConstraintViolation();
            }
        } else if (isEmpty(caseWorkerProfile.getLocations())
                   || caseWorkerProfile.getLocations()
                      .stream()
                      .noneMatch(Location::isPrimary)) {

            isValidLocations = false;
            context.buildConstraintViolationWithTemplate(NO_PRIMARY_LOCATION_PRESENT)
                   .addPropertyNode(LOCATION_FIELD)
                   .addConstraintViolation();
        }
        return isValidLocations;
    }

    private boolean isValidRoles(CaseWorkerProfile caseWorkerProfile, ConstraintValidatorContext context) {
        boolean isValidRoles = true;
        if (isNotEmpty(caseWorkerProfile.getRoles())
            && caseWorkerProfile.getRoles().size() > 1
            && nonNull(caseWorkerProfile.getRoles().get(0).getRoleName())) {
            isValidRoles = negate(caseWorkerProfile.getRoles().get(0).getRoleName()
                .equalsIgnoreCase(caseWorkerProfile.getRoles().get(1).getRoleName()));
            if (FALSE.equals(isValidRoles)) {
                context.buildConstraintViolationWithTemplate(DUPLICATE_PRIMARY_AND_SECONDARY_ROLES)
                    .addPropertyNode(ROLE_FIELD)
                    .addConstraintViolation();
            }
        }
        return isValidRoles;
    }
}
