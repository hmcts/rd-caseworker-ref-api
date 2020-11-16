package uk.gov.hmcts.reform.cwrdapi.config;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorServiceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorServiceImpl.FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorServiceImpl.FILE_PASSWORD_INCORRECT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.service.WorkBookCustomFactory.FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE;

@RunWith(SpringRunner.class)
public class ExcelUploaderTest extends SpringBootIntegrationTest {

    public static String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static String TYPE_XLS = "application/vnd.ms-excel";

    @Autowired
    ExcelValidatorServiceImpl excelValidatorService;

    @Test
    public void sendXlsWithCorrectPasswordTest() throws IOException {
        Workbook workbook = excelValidatorService
                .validateExcelFile(getMultipartFile("src/integrationTest/resources/WithCorrectPassword.xls",
                        TYPE_XLS));
        assertThat(workbook).isNotNull();
    }

    @Test
    public void sendXlsWithIncorrectPasswordSetTest() {
        Assertions.assertThatThrownBy(() -> excelValidatorService.validateExcelFile(
                getMultipartFile("src/integrationTest/resources/WithIncorrectPassword.xls",
                                TYPE_XLS)))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_PASSWORD_INCORRECT_ERROR_MESSAGE);
    }

    @Test
    public void sendXlsWithNoPasswordSetTest() {
        Assertions.assertThatThrownBy(() -> excelValidatorService
                .validateExcelFile(getMultipartFile("src/integrationTest/resources/WithNoPasswordSet.xls",
                                TYPE_XLS)))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE);
    }

    @Test
    public void sendXlsxWithCorrectPasswordTest() throws IOException {
        Workbook workbook = excelValidatorService.validateExcelFile(
                getMultipartFile("src/integrationTest/resources/WithCorrectPassword.xlsx",
                        TYPE_XLSX));
        assertThat(workbook).isNotNull();
    }

    @Test
    public void sendXlsxWithIncorrectPasswordSetTest() {
        Assertions.assertThatThrownBy(() -> excelValidatorService.validateExcelFile(
                getMultipartFile("src/integrationTest/resources/WithIncorrectPassword.xlsx",
                                TYPE_XLSX)))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_PASSWORD_INCORRECT_ERROR_MESSAGE);
    }

    @Test
    public void sendXlsxWithNoPasswordSetTest() {
        Assertions.assertThatThrownBy(() -> excelValidatorService.validateExcelFile(
                getMultipartFile("src/integrationTest/resources/WithNoPasswordSet.xlsx",
                                TYPE_XLSX)))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE);
    }

    @Test
    public void sendTextFileTest() {
        Assertions.assertThatThrownBy(() -> excelValidatorService.validateExcelFile(
                getMultipartFile("src/integrationTest/resources/test.txt",
                                "text/plain")))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE);
    }


    public MultipartFile getMultipartFile(String filePath, String fileType) throws IOException {
        File file = getFile(filePath);
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile("file",
                file.getName(), fileType, IOUtils.toByteArray(input));
    }
}