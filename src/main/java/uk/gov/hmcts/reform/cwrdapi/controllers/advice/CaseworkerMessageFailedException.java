package uk.gov.hmcts.reform.cwrdapi.controllers.advice;

public class CaseworkerMessageFailedException extends RuntimeException {

    public CaseworkerMessageFailedException(String message) {
        super(message);
    }
}
