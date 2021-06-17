package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StaffReferenceException extends RuntimeException {

    private final HttpStatus status;
    private final String errorMessage;
    private final String errorDescription;

    public StaffReferenceException(HttpStatus status,
                                   String errorMessage,
                                   String errorDescription) {
        super(errorMessage);
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
    }
}
