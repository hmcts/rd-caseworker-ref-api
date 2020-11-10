package uk.gov.hmcts.reform.cwrdapi.service;

import static org.apache.poi.hssf.record.crypto.Biff8EncryptionKey.setCurrentUserPassword;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import java.io.IOException;
import java.io.InputStream;

public class WorkBookCustomFactory extends WorkbookFactory {

    public static String FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE = "File is not password protected";
    public static String INVALID_EXCEL_FILE_ERROR_MESSAGE = "Excel File is invalid";
    public static String ERROR_PARSING_EXCEL_FILE_ERROR_MESSAGE = "Excel File is invalid";
    public static String CLASS_XSSF_WORKBOOK_FACTORY = "org.apache.poi.xssf.usermodel.XSSFWorkbookFactory";
    public static String CLASS_HSSF_WORKBOOK_FACTORY = "org.apache.poi.hssf.usermodel.HSSFWorkbookFactory";

    /**
     * Authenticate password file and if successful the returns Workbook object.
     * Authentication for xls and xlsx files happens differently.
     * If both type of files are password protected then those are OLE2 type.
     * If xlsx file is not password protected then it is of type OOXML.
     * If xls file is not password protected then it doesn't throw EncryptedDocumentException.
     *
     * @param file for processing
     * @param password for authenticate excel file
     * @return Workbook converted after file password authentication done
     * @throws IOException while file parsing failed
     */
    public static Workbook validatePasswordAndGetWorkBook(MultipartFile file, String password) throws IOException {
        InputStream is = FileMagic.prepareToCheckMagic(file.getInputStream());
        FileMagic fm = FileMagic.valueOf(is);
        switch (fm) {
            case OLE2:
                POIFSFileSystem fs = new POIFSFileSystem(is);
                DirectoryNode directoryNode = fs.getRoot();
                if (directoryNode.hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
                    InputStream stream = null;
                    try {
                        stream = DocumentFactoryHelper.getDecryptedStream(directoryNode, password);
                        initXssf();
                        return createXssfByStream.apply(stream);
                    } finally {
                        IOUtils.closeQuietly(stream);
                        fs.getRoot().getFileSystem().close();
                    }
                } else {
                    try {
                        new HSSFWorkbook(fs).isWriteProtected();
                    } catch (EncryptedDocumentException ede) {
                        setCurrentUserPassword(password);
                        try {
                            initHssf();
                            return createHssfByNode.apply(directoryNode);
                        } finally {
                            setCurrentUserPassword(null);
                        }
                    }
                    throw new ExcelValidationException
                            (HttpStatus.BAD_REQUEST, FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE);
                }
            case OOXML:
                throw new ExcelValidationException(HttpStatus.BAD_REQUEST, FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE);
            default:
                throw new ExcelValidationException(HttpStatus.BAD_REQUEST, INVALID_EXCEL_FILE_ERROR_MESSAGE);
        }
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
            throw new ExcelValidationException(HttpStatus.INTERNAL_SERVER_ERROR, ERROR_PARSING_EXCEL_FILE_ERROR_MESSAGE);
        }
    }
}
