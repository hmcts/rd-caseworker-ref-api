package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.service.IJsrValidatorInitializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static net.logstash.logback.encoder.org.apache.commons.lang3.BooleanUtils.isNotTrue;

@Component
@Slf4j
public class JsrValidatorInitializer<T> implements IJsrValidatorInitializer<T> {

    private Validator validator;

    private Set<ConstraintViolation<T>> constraintViolations = new HashSet<>();

    @Value("${logging-component-name}")
    private String logComponentName;

    @PostConstruct
    public void initializeFactory() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * JSR validation.
     *
     * @param domains List
     * @return List binder list
     */
    public List<T> getInvalidJsrRecords(List<T> domains) {

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

