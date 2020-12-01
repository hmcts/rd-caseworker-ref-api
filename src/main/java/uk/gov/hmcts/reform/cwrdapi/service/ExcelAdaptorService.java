package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface ExcelAdaptorService {
    <T> List<T>  parseExcel(Workbook workbook, Class<T> classType);
}
