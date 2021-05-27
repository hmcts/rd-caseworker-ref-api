package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StaffReferenceException extends RuntimeException {

    private final HttpStatus status;
    private final String errorMessage;
    private final Throwable exception;

    public StaffReferenceException(HttpStatus status,
                                   String errorMessage,
                                   Throwable exception) {
        super(errorMessage, exception);
        this.status = status;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }
}
