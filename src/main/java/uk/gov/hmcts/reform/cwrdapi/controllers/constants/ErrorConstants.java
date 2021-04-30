package uk.gov.hmcts.reform.cwrdapi.controllers.constants;


public enum ErrorConstants {

    MALFORMED_JSON("1 : Malformed Input Request"),

    UNSUPPORTED_MEDIA_TYPES("2 : Unsupported Media Type"),

    INVALID_REQUEST_EXCEPTION("3 : There is a problem with your request. Please check and try again"),

    EMPTY_RESULT_DATA_ACCESS("4 : Resource not found"),

    UNKNOWN_EXCEPTION("5 : error was caused by an unknown exception"),

    CONFLICT_EXCEPTION("6 : Error was caused by duplicate key exception"),

    ACCESS_EXCEPTION("7 : Access Denied"),

    RUNTIME_EXCEPTION("8 : Sorry, there is a problem with the service. Try again later"),

    FILE_UPLOAD_IN_PROGRESS("9 : File upload is already in progress."
                                + " Please try again once existing file upload is completed"),

    NO_USER_ID_OR_EMAIL_PATTERN_PROVIDED_TO_DELETE("No User ID or Email Pattern provided to delete the User(s)");


    private final String errorMessage;

    ErrorConstants(String  errorMessage) {
        this.errorMessage  = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
