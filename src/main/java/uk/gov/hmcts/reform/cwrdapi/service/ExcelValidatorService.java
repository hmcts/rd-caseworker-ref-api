package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

public interface ExcelValidatorService {
    /**
     * To validate exel file.
     * @param excelFile excelFile
     * @return Workbook
     */
    Workbook validateExcelFile(MultipartFile excelFile);
}
