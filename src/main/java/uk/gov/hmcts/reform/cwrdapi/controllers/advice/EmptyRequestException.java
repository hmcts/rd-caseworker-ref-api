package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

public class EmptyRequestException
    extends RuntimeException {

    public EmptyRequestException(String message) {
        super(message);
    }


}
