package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
