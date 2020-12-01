package uk.gov.hmcts.reform.cwrdapi.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ExcelValidationException extends RuntimeException {

    private final HttpStatus httpStatus;

    private final String errorMessage;

    public ExcelValidationException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
