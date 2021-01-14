package uk.gov.hmcts.reform.cwrdapi.util;

import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;

import java.io.IOException;
import java.io.InputStream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CLASS_HSSF_WORKBOOK_FACTORY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.CLASS_XSSF_WORKBOOK_FACTORY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ERROR_PARSING_EXCEL_FILE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_EXCEL_FILE_ERROR_MESSAGE;

public class WorkBookCustomFactory extends WorkbookFactory {

    /**
     * Authenticate password file and if successful the returns Workbook object.
     * Authentication for xls and xlsx files happens differently.
     * If both type of files are password protected then those are OLE2 type.
     * If xlsx file is not password protected then it is of type OOXML.
     * If xls file is not password protected then it doesn't throw EncryptedDocumentException.
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
                System.out.println("Case: OLE2");
                return createWorkBook(is);
            case OOXML:
                System.out.println("Case: OOXML");
                throw new ExcelValidationException(BAD_REQUEST, "Case: OOXML");
            default:
                throw new ExcelValidationException(BAD_REQUEST, INVALID_EXCEL_FILE_ERROR_MESSAGE);
        }
    }

    private static Workbook createWorkBook(InputStream is) throws IOException {
        POIFSFileSystem fs = new POIFSFileSystem(is);
        DirectoryNode directoryNode = fs.getRoot();
/*        WorkbookFactory.create(fs);
        return WorkbookFactory.create(fs);*/
        if (directoryNode.hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
            try {
                initXssf();
                return createXssfByStream.apply(is);
            } finally {
                fs.getRoot().getFileSystem().close();
            }
        } else {
            initHssf();
            return createHssfByNode.apply(directoryNode);

        }
        // throw new ExcelValidationException(BAD_REQUEST, FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE);
        //return null;
    }


    private static void initXssf() {
        if (createXssfFromScratch == null) {
            initFactory(CLASS_XSSF_WORKBOOK_FACTORY);
        }
    }

    private static void initHssf() {
        if (createHssfFromScratch == null) {
            initFactory(CLASS_HSSF_WORKBOOK_FACTORY);
        }
    }

    private static void initFactory(String factoryClass) {
        try {
            Class.forName(factoryClass, true, WorkbookFactory.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ExcelValidationException(INTERNAL_SERVER_ERROR, ERROR_PARSING_EXCEL_FILE_ERROR_MESSAGE);
        }
    }
}
