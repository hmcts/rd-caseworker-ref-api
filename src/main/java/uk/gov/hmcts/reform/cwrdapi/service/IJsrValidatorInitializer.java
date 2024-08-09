package uk.gov.hmcts.reform.cwrdapi.service;

import jakarta.validation.ConstraintViolation;

import java.util.List;
import java.util.Set;

public interface IJsrValidatorInitializer<T> {

    /**
     * JSR validation.
     *
     * @param domains List
     * @return List binder list
     */
    List<T> getInvalidJsrRecords(List<T> domains);

    Set<ConstraintViolation<T>> getConstraintViolations();
}
