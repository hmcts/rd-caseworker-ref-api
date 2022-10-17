package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorStaffProfile;
import uk.gov.hmcts.reform.cwrdapi.service.IStaffProfileAuditService;
import uk.gov.hmcts.reform.cwrdapi.util.AuditStatus;

import java.util.Set;
import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

/**
 * The type Jsr validator staff profile.
 */
@Component
@Slf4j
public class JsrValidatorStaffProfile implements IJsrValidatorStaffProfile {

    private Validator validator;

    @Value("${logging-component-name}")
    private String logComponentName;

    /**
     * Initialize factory.
     */
    @PostConstruct
    public void initializeFactory() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * The IStaffProfileAuditService.
     */
    @Autowired
    IStaffProfileAuditService staffProfileAuditService;

    @Override
    public void validateStaffProfile(StaffProfileCreationRequest profileRequest, String operationType) {

        log.info("{}:: JsrValidatorStaffProfile data processing validate starts::", logComponentName);
        Set<ConstraintViolation<StaffProfileCreationRequest>> constraintErrors = validator.validate(profileRequest);
        if (isNotEmpty(constraintErrors)) {
            constraintErrors.forEach(constraintError -> {
                String errorMsg =    constraintError.getMessage();
                staffProfileAuditService.saveStaffAudit(AuditStatus.FAILURE,errorMsg,
                            StringUtils.EMPTY,profileRequest,operationType);
                throw new InvalidRequestException(errorMsg);
            });
        }
        log.info("{}:: JsrValidatorStaffProfile data processing validate complete::", logComponentName);
    }
}


