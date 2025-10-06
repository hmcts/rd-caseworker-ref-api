package uk.gov.hmcts.reform.cwrdapi.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;
import uk.gov.hmcts.reform.cwrdapi.service.IValidationService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.BooleanUtils.isNotTrue;

@Component
@Slf4j
public class JsrValidatorInitializer<T> implements IJsrValidatorInitializer<T> {

    private Validator validator;

    private Set<ConstraintViolation<T>> constraintViolations;

    @Value("${logging-component-name}")
    private String logComponentName;

    @PostConstruct
    public void initializeFactory() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Autowired
    IValidationService validationServiceFacade;
    /**
     * JSR validation.
     *
     * @param domains List
     * @return List binder list
     */

    public List<T> getInvalidJsrRecords(List<T> domains) {

        constraintViolations = new HashSet<>();

        log.info("{}:: JsrValidatorInitializer data processing validate starts::",
            logComponentName);
        List<T> invalidList = new ArrayList<>();

        domains.forEach(domain -> {
            Set<ConstraintViolation<T>> constraintErrors = validator.validate(domain);
            if (isNotTrue(constraintErrors.isEmpty())) {
                invalidList.add(domain);
            }
            this.constraintViolations.addAll(constraintErrors);
        });

        log.info("{}:: JsrValidatorInitializer data processing validate complete::", logComponentName);
        return invalidList;

    }

    public Set<ConstraintViolation<T>> getConstraintViolations() {
        return constraintViolations;
    }
}

