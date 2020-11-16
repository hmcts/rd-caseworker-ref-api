package uk.gov.hmcts.reform.cwrdapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

@Component
@Slf4j
public class JsrValidatorInitializer<T> {

    private Validator validator;

    private Set<ConstraintViolation<T>> constraintViolations;

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

        log.info("{}:: JsrValidatorInitializer data processing validate starts::", logComponentName);
        this.constraintViolations = new LinkedHashSet<>();
        List<T> invalidList = new ArrayList<>();
        domains.forEach(domain -> {
            Set<ConstraintViolation<T>> constraintViolations = validator.validate(domain);
            if (constraintViolations.size() > 0) {
                invalidList.add(domain);
            }
            this.constraintViolations.addAll(constraintViolations);
        });

        log.info("{}:: JsrValidatorInitializer data processing validate complete::", logComponentName);
        return invalidList;
    }

    public Set<ConstraintViolation<T>> getConstraintViolations() {
        return constraintViolations;
    }

}

