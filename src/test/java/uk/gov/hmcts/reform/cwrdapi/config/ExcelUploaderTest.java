package uk.gov.hmcts.reform.cwrdapi.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_EXCEL_FILE_ERROR_MESSAGE;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.util.WorkBookCustomFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RunWith(SpringRunner.class)
public class ExcelUploaderTest {

    public static String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static String TYPE_XLS = "application/vnd.ms-excel";


    @Test
    public void sendXlsxParseMultipartFileTest() throws IOException {
        Workbook workbook = WorkBookCustomFactory.
                validateAndGetWorkBook(getMultipartFile("src/test/resources/Staff Data Upload.xlsx",
                        TYPE_XLSX));
        assertThat(workbook).isNotNull();
    }

    @Test
    public void sendXlsParseMultipartFileTest() throws IOException {
        Workbook workbook = WorkBookCustomFactory.
                validateAndGetWorkBook(getMultipartFile("src/test/resources/Test-file.xls",
                        TYPE_XLS));
        assertThat(workbook).isNotNull();
    }

    @Test
    public void sendTextFileTest() {
        Assertions.assertThatThrownBy(() -> WorkBookCustomFactory.
                validateAndGetWorkBook
                        (getMultipartFile("src/integrationTest/resources/test.txt",
                                "text/plain")))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(INVALID_EXCEL_FILE_ERROR_MESSAGE);
    }


    public MultipartFile getMultipartFile (String filePath, String fileType) throws IOException{
        File file = getFile(filePath);
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile("file",
                file.getName(), fileType, IOUtils.toByteArray(input));
    }
}
