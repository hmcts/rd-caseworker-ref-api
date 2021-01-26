package uk.gov.hmcts.reform.cwrdapi.controllers.constants;


public enum ErrorConstants {

    MALFORMED_JSON("1 : Malformed Input Request"),

    UNSUPPORTED_MEDIA_TYPES("2 : Unsupported Media Type"),

    INVALID_REQUEST_EXCEPTION("3 : There is a problem with your request. Please check and try again"),

    EMPTY_RESULT_DATA_ACCESS("4 : Resource not found"),

    UNKNOWN_EXCEPTION("8 : error was caused by an unknown exception"),

    CONFLICT_EXCEPTION("10 : Error was caused by duplicate key exception"),

    ACCESS_EXCEPTION("9 : Access Denied"),
    ERROR_PUBLISHING_TO_TOPIC("10 : There was an error in communication with Role Assignment Service. "
            + "Please try again later.");

    private final String errorMessage;

    ErrorConstants(String  errorMessage) {
        this.errorMessage  = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
