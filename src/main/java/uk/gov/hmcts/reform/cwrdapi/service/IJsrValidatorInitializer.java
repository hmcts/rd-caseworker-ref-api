package uk.gov.hmcts.reform.cwrdapi.service;

import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;

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
