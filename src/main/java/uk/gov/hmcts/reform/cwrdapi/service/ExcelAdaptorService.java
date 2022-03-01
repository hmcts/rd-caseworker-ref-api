package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface ExcelAdaptorService {
    /**
     * To Parse a excel.
     * @param workbook workbook
     * @param classType classType
     * @param <T> classType
     * @return List
     */
    <T> List<T>  parseExcel(Workbook workbook, Class<T> classType);
}
