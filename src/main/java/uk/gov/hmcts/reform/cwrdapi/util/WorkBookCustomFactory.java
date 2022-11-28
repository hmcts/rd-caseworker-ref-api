package uk.gov.hmcts.reform.cwrdapi.util;

import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;

import java.io.IOException;
import java.io.InputStream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_EXCEL_FILE_ERROR_MESSAGE;


public interface WorkBookCustomFactory  {

    /**
     * Returns Workbook object.
     * If xls file is not password protected then those are OLE2 type.
     * If xlsx file is not password protected then it is of type OOXML.
     *
     * @param file for processing
     * @return Workbook converted after file password authentication done
     * @throws IOException while file parsing failed
     */
    public static Workbook validateAndGetWorkBook(MultipartFile file) throws IOException {

        InputStream is = FileMagic.prepareToCheckMagic(file.getInputStream());
        FileMagic fm = FileMagic.valueOf(is);
        switch (fm) {
            case OLE2:
            case OOXML:
                return WorkbookFactory.create(is);
            default:
                throw new ExcelValidationException(BAD_REQUEST, INVALID_EXCEL_FILE_ERROR_MESSAGE);
        }

    }

}
