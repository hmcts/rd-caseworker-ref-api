package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

public class ResourceNotFoundException extends RuntimeException{
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
