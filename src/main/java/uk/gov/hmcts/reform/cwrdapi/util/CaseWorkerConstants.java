package uk.gov.hmcts.reform.cwrdapi.util;

public final class CaseWorkerConstants {

    private CaseWorkerConstants() {
        super();
    }

    public static final String PARTIAL_SUCCESS = "PARTIAL SUCCESS";

    // excel adapter related
    public static final String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String TYPE_XLS = "application/vnd.ms-excel";
    public static final String CLASS_XSSF_WORKBOOK_FACTORY = "org.apache.poi.xssf.usermodel.XSSFWorkbookFactory";
    public static final String CLASS_HSSF_WORKBOOK_FACTORY = "org.apache.poi.hssf.usermodel.HSSFWorkbookFactory";
    public static final String IS_PRIMARY_FIELD = "isPrimary";
    public static final String DELIMITER_COMMA = ",";

    //excel adapter related error messages
    public static final String FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE = "You can only upload xlsx or xls files."
        + " Check it’s saved in the correct format and try again.";
    public static final String FILE_NOT_PRESENT_ERROR_MESSAGE = "You can only upload xlsx or xls files."
        + " Check it’s saved in the correct format and try again.";
    public static final String FILE_PASSWORD_PROTECTED_ERROR_MESSAGE =
        "The file is password protected. Please provide a file without password.";
    public static final String FILE_NO_VALID_SHEET_ERROR_MESSAGE = "The uploaded file does not "
        + "contain the ‘Staff data’ worksheet.";
    public static final String ERROR_FILE_PARSING_ERROR_MESSAGE = "Error while parsing ";
    public static final String INVALID_EXCEL_FILE_ERROR_MESSAGE = "Excel File is invalid";
    public static final String ERROR_PARSING_EXCEL_FILE_ERROR_MESSAGE = "Excel File is invalid";

    public static final String IDAM_ROLE_MAPPINGS_SUCCESS =
        "Successfully built the idam role mappings for the service id provided";

    public static final String DELETE_RECORD_FOR_SERVICE_ID = "deleted all the records for the service id provided";

    public static final String IDAM_ROLE_MAPPINGS_FAILURE =
        "failed to build the idam role mappings for the service id provided";

    public static final String UNAUTHORIZED_ERROR =
        "Unauthorized Error : The requested resource is restricted and requires authentication";

    public static final String FORBIDDEN_ERROR = "Forbidden Error: Access denied for invalid permissions";
    public static final String MULTIPLE_SERVICE_CODES = "The file must contain mapping for 1 Service Code."
        + " Please update the Service Code for all the rows and re-upload the file";

    public static final String NO_DATA_FOUND = "The Caseworker data could not be found";

    public static final String BAD_REQUEST = "Bad Request";

    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    public static final String REQUEST_COMPLETED_SUCCESSFULLY = "Request Completed Successfully";

    public static final String REQUIRED_CW_SHEET_NAME = "Case Worker Data";
    public static final String REQUIRED_ROLE_MAPPING_SHEET_NAME = "Service to CW Roles Mapping";

    public static final String CASE_WORKER_FILE_NAME = "staff data";

    public static final String RECORDS_UPLOADED = "%s record(s) uploaded";

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";

    public static final String IDAM_STATUS_SUSPENDED = "SUSPENDED";

    public static final String ORIGIN_EXUI = "EXUI";

    public static final String UP_FAILURE_ROLES = "can't modify roles for user in UP";

    public static final String SUSPEND_USER_FAILED = "suspend user in UP failed";

    public static final String STATUS_ACTIVE = "ACTIVE";

    public static final String ALREADY_SUSPENDED_ERROR_MESSAGE = "user is suspended already";

    public static final String UP_CREATION_FAILED = "Failed to create in UP with response status %s";

    public static final String REQUEST_FAILED_FILE_UPLOAD_JSR =
        "Request completed with partial success. Some records failed during validation and were ignored.";

    public static final String RECORDS_FAILED = "%s record(s) failed validation";
    public static final String RECORDS_SUSPENDED = "%s record(s) suspended";

    public static final String DOMAIN_JUSTICE_GOV_UK = "justice.gov.uk";
    public static final String USER_NAME_PATTERN = "^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*";
    public static final String INVALID_EMAIL = "You must add a valid justice.gov.uk email"
        + " address and upload the file again";

    public static final String FILE_NO_DATA_ERROR_MESSAGE = "There is no data in the file uploaded."
        + " Upload a valid file in xlsx or xls format";
    public static final String FILE_MISSING_HEADERS =  "The file is missing some column headers."
        + " Review and upload again";

    public static final String NO_LOCATION_PRESENT = "You must add location details and upload the file again.";
    public static final String NO_ROLE_PRESENT = "You must add role details and upload the file again";
    public static final String NO_WORK_AREA_PRESENT = "You must add details of at least one area of work"
        + " and upload the file again";
    public static final String NO_USER_TYPE_PRESENT = "You must add a user type and upload the file again";
    public static final String FIRST_NAME_MISSING = "You must add a first name and upload the file again";
    public static final String LAST_NAME_MISSING = "You must add a last name and upload the file again";
    public static final String MISSING_REGION = "You must add a region and upload the file again";
    public static final String NO_USER_TO_SUSPEND = "There is no user present for row id %s to suspend. "
        + "Please try again or check with HMCTS Support Team";
    public static final String ROLE_CWD_USER = "cwd-user";

    public static final String DUPLICATE_PRIMARY_AND_SECONDARY_ROLES = "Primary and Secondary Roles Should "
        + "not be same.";
    public static final String DUPLICATE_PRIMARY_AND_SECONDARY_LOCATIONS = "Primary and Secondary Locations Should not "
        + "be same";
    public static final String DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK = "Duplicate Service Codes in Area of Work";
    public static final String ROLE_FIELD = "Role";
    public static final String LOCATION_FIELD = "Location";
    public static final String AREA_OF_WORK_FIELD = "Area Of Work";
    public static final String AND = "and";
}
