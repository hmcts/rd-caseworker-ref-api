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
    public static final String FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE = "File provided in request is not in xls(x) format";
    public static final String FILE_NOT_PRESENT_ERROR_MESSAGE = "File not present";
    public static final String FILE_PASSWORD_PROTECTED_ERROR_MESSAGE =
            "The file is password protected. Please provide a file without password.";
    public static final String FILE_NO_DATA_ERROR_MESSAGE = "No data in Excel File";
    public static final String FILE_NO_VALID_SHEET_ERROR_MESSAGE = "The uploaded file does not contain "
            + "the worksheet named Case Worker Data";
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

    public static final String NO_DATA_FOUND = "The Caseworker data could not be found";

    public static final String BAD_REQUEST = "Bad Request";

    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    public static final String REQUEST_COMPLETED_SUCCESSFULLY = "Request Completed Successfully";

    public static final String REQUIRED_CW_SHEET_NAME = "Case Worker Data";
    public static final String REQUIRED_ROLE_MAPPING_SHEET_NAME = "IDAM Mapping";

    public static final String CASE_WORKER_FILE_NAME = "caseworker";
    public static final String RECORDS_UPLOADED =
            "%s record(s) uploaded";


}
