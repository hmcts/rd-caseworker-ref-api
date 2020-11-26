package uk.gov.hmcts.reform.cwrdapi.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorService;

import java.io.IOException;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.BooleanUtils.negate;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ERROR_PARSING_EXCEL_FILE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NOT_PRESENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_PASSWORD_INCORRECT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLSX;
import static uk.gov.hmcts.reform.cwrdapi.util.WorkBookCustomFactory.validatePasswordAndGetWorkBook;

@Slf4j
@Service
public class ExcelValidatorServiceImpl implements ExcelValidatorService {

    @Value("${excel.password}")
    private String excelPassword;

    /**
     * Validates multipart excel file for extension and password first and then creates Workbook object for
     * further processing.
     * @param excelFile multipart file for processing
     * @return workbook for processing
     */
    public Workbook validateExcelFile(MultipartFile excelFile) {
        isTypeExcel(excelFile);
        try {
            return validatePasswordAndGetWorkBook(excelFile, excelPassword);
        } catch (IOException | EncryptedDocumentException exception) {
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            String errorMessage = ERROR_PARSING_EXCEL_FILE_ERROR_MESSAGE;
            if (exception instanceof EncryptedDocumentException) {
                status = HttpStatus.BAD_REQUEST;
                errorMessage = FILE_PASSWORD_INCORRECT_ERROR_MESSAGE;
            }
            throw new ExcelValidationException(status, errorMessage);
        }
    }

    /**
     * Validates extension and type of file is excel or not.
     * @param file multipart file for processing
     */
    public static void isTypeExcel(MultipartFile file) {
        if (nonNull(file)) {
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            if (nonNull(fileName) && nonNull(contentType)) {
                //sonar mandates to check null, as for multipart file name and content will never be null
                if ((negate(fileName.endsWith(".xlsx") || fileName.endsWith(".xls"))
                        || negate((TYPE_XLS.equals(contentType) || TYPE_XLSX.equals(contentType))))) {
                    throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE);
                }
            } else {
                throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NOT_PRESENT_ERROR_MESSAGE);
            }
        } else {
            throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NOT_PRESENT_ERROR_MESSAGE);
        }
    }
}
