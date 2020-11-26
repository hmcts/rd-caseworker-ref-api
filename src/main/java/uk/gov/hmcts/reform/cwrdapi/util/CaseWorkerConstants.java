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
    public static final String FILE_PASSWORD_INCORRECT_ERROR_MESSAGE =
            "Failed to open the file. Please provide the file with a valid password.";
    public static final String FILE_NO_DATA_ERROR_MESSAGE = "No data in Excel File";
    public static final String ERROR_FILE_PARSING_ERROR_MESSAGE = "Error while parsing ";
    public static final String FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE = "File is not password protected";
    public static final String INVALID_EXCEL_FILE_ERROR_MESSAGE = "Excel File is invalid";
    public static final String ERROR_PARSING_EXCEL_FILE_ERROR_MESSAGE = "Excel File is invalid";

}
