package uk.gov.hmcts.reform.cwrdapi.util;

import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;

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
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_PRIMARY_LOCATION_PRESENT_PROFILE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_FIELD;

public class StaffProfileChildListValidator implements ConstraintValidator<ValidateStaffProfileChildren,
        StaffProfileCreationRequest> {

    @Override
    public boolean isValid(StaffProfileCreationRequest staffProfileCreationRequest,
                           ConstraintValidatorContext context) {
        //disable existing violation message
        context.disableDefaultConstraintViolation();
        return (isValidRoles(staffProfileCreationRequest, context)
            && isValidLocations(staffProfileCreationRequest, context)
            && isValidAreaOfWk(staffProfileCreationRequest, context));
    }

    public boolean isValidAreaOfWk(StaffProfileCreationRequest staffProfileCreationRequest,
                                    ConstraintValidatorContext context) {
        boolean isValidAreaOfWk = true;
        if (isNotEmpty(staffProfileCreationRequest.getServices())
            && staffProfileCreationRequest.getServices().size() > 1
            && staffProfileCreationRequest.getServices().stream()
            .map(CaseWorkerServicesRequest::getServiceCode)
            .collect(Collectors.toSet()).size()
            != staffProfileCreationRequest.getServices().size()) {
            isValidAreaOfWk = false;
            context.buildConstraintViolationWithTemplate(DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK)
                .addPropertyNode(AREA_OF_WORK_FIELD)
                .addConstraintViolation();
        }
        return isValidAreaOfWk;
    }

    public boolean isValidLocations(StaffProfileCreationRequest staffProfileCreationRequest,
                                    ConstraintValidatorContext context) {
        boolean isValidLocations = true;
        if (isNotEmpty(staffProfileCreationRequest.getBaseLocations())
            && staffProfileCreationRequest.getBaseLocations().size() > 1) {
            //@TO do remove getLocationName with Id problem with excel sheet
            isValidLocations = negate(staffProfileCreationRequest.getBaseLocations().get(0).getLocation()
                .equalsIgnoreCase(staffProfileCreationRequest.getBaseLocations().get(1).getLocation()));
            if (FALSE.equals(isValidLocations)) {
                context.buildConstraintViolationWithTemplate(DUPLICATE_PRIMARY_AND_SECONDARY_LOCATIONS)
                    .addPropertyNode(LOCATION_FIELD)
                    .addConstraintViolation();
            }
        } else if (isEmpty(staffProfileCreationRequest.getBaseLocations())
                || staffProfileCreationRequest.getBaseLocations()
                .stream()
                .noneMatch(CaseWorkerLocationRequest::isPrimaryFlag)) {

            isValidLocations = false;
            context.buildConstraintViolationWithTemplate(NO_PRIMARY_LOCATION_PRESENT_PROFILE)
                   .addPropertyNode(LOCATION_FIELD)
                   .addConstraintViolation();
        }
        return isValidLocations;
    }

    public boolean isValidRoles(StaffProfileCreationRequest staffProfileCreationRequest,
                                 ConstraintValidatorContext context) {
        boolean isValidRoles = true;
        if (isNotEmpty(staffProfileCreationRequest.getRoles())
            && staffProfileCreationRequest.getRoles().size() > 1
            && nonNull(staffProfileCreationRequest.getRoles().get(0).getRole())) {
            isValidRoles = negate(staffProfileCreationRequest.getRoles().get(0).getRole()
                .equalsIgnoreCase(staffProfileCreationRequest.getRoles().get(1).getRole()));
            if (FALSE.equals(isValidRoles)) {
                context.buildConstraintViolationWithTemplate(DUPLICATE_PRIMARY_AND_SECONDARY_ROLES)
                    .addPropertyNode(ROLE_FIELD)
                    .addConstraintViolation();
            }
        }
        return isValidRoles;
    }
}
