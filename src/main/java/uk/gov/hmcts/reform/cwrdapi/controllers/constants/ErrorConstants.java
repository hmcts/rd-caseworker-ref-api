package uk.gov.hmcts.reform.cwrdapi.controllers.constants;


public enum ErrorConstants {

    MALFORMED_JSON("1 : Malformed Input Request"),

    UNSUPPORTED_MEDIA_TYPES("2 : Unsupported Media Type"),

    INVALID_REQUEST_EXCEPTION("3 : There is a problem with your request. Please check and try again"),

    EMPTY_RESULT_DATA_ACCESS("4 : Resource not found"),

    UNKNOWN_EXCEPTION("8 : error was caused by an unknown exception"),

    CONFLICT_EXCEPTION("10 : Error was caused by duplicate key exception"),

    ACCESS_EXCEPTION("9 : Access Denied"),

    RUNTIME_EXCEPTION("10 : Sorry, there is a problem with the service. Try again later");

    private final String errorMessage;

    ErrorConstants(String  errorMessage) {
        this.errorMessage  = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
