package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;

public interface ExcelAdaptorService {
    List<Object> parseExcel(Workbook workbook, Class classType);
}
