package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface ExcelValidatorService {
    Workbook validateExcelFile(MultipartFile excelFile);
}
