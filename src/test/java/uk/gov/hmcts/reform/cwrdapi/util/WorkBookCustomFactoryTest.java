package uk.gov.hmcts.reform.cwrdapi.util;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_EXCEL_FILE_ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class WorkBookCustomFactoryTest {

    static String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String TYPE_XLS = "application/vnd.ms-excel";


    @Test
    void sendXlsxParseMultipartFileTest() throws IOException {
        Workbook workbook = WorkBookCustomFactory
                .validateAndGetWorkBook(getMultipartFile("src/test/resources/Staff Data Upload.xlsx",
                        TYPE_XLSX));
        assertThat(workbook).isNotNull();
    }

    @Test
    void sendXlsParseMultipartFileTest() throws IOException {
        Workbook workbook = WorkBookCustomFactory
                .validateAndGetWorkBook(getMultipartFile("src/test/resources/Test-file.xls",
                        TYPE_XLS));
        assertThat(workbook).isNotNull();
    }

    @Test
    void sendTextFileTest() throws IOException {
        MultipartFile multipartFile = getMultipartFile("src/integrationTest/resources/test.txt",
                "text/plain");
        Assertions.assertThatThrownBy(() -> WorkBookCustomFactory.validateAndGetWorkBook(multipartFile))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(INVALID_EXCEL_FILE_ERROR_MESSAGE);
    }

    MultipartFile getMultipartFile(String filePath, String fileType) throws IOException {
        File file = getFile(filePath);
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile("file",
                file.getName(), fileType, IOUtils.toByteArray(input));
    }
}