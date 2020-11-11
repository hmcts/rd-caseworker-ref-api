package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CwrdApiException extends RuntimeException {

    private final HttpStatus httpStatus;

    private final String errorMessage;

    public CwrdApiException(HttpStatus httpStatus, String errorMessage) {
        super(errorMessage);
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
