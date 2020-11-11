package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
