package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorServiceImpl.FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorServiceImpl.FILE_NOT_PRESENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorServiceImpl.FILE_PASSWORD_INCORRECT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.service.WorkBookCustomFactory.FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE;

@RunWith(MockitoJUnitRunner.class)
public class ExcelValidatorServiceImplTest {

    public static String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static String TYPE_XLS = "application/vnd.ms-excel";

    @InjectMocks
    ExcelValidatorServiceImpl excelValidatorServiceImpl;

    @Before
    public void setUpField() {
        ReflectionTestUtils.setField(excelValidatorServiceImpl, "excelPassword", "1234");
    }

    public MultipartFile getMultipartFile(String filePath, String fileType) throws IOException {
        File file = getFile(filePath);
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile("file",
                file.getName(), fileType, IOUtils.toByteArray(input));
    }

    @Test
    public void sendXlsWithCorrectPasswordTest() throws IOException {
        Workbook workbook = excelValidatorServiceImpl
                .validateExcelFile(
                        getMultipartFile("src/test/resources/WithCorrectPassword.xls", TYPE_XLS));
        assertThat(workbook).isNotNull();
    }

    @Test
    public void sendXlsWithIncorrectPasswordSetTest() throws IOException {

        MultipartFile file = getMultipartFile("src/test/resources/WithIncorrectPassword.xls", TYPE_XLS);
        Assertions.assertThatThrownBy(() -> excelValidatorServiceImpl.validateExcelFile(file))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_PASSWORD_INCORRECT_ERROR_MESSAGE);
    }

    @Test
    public void sendXlsWithNoPasswordSetTest() throws IOException {
        MultipartFile file = getMultipartFile("src/integrationTest/resources/WithNoPasswordSet.xls", TYPE_XLS);
        Assertions.assertThatThrownBy(() -> excelValidatorServiceImpl
                .validateExcelFile(file))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE);
    }

    @Test
    public void sendXlsxWithCorrectPasswordTest() throws IOException {
        MultipartFile file = getMultipartFile("src/test/resources/WithCorrectPassword.xlsx", TYPE_XLSX);
        Workbook workbook = excelValidatorServiceImpl.validateExcelFile(file);
        assertThat(workbook).isNotNull();
    }

    @Test
    public void sendXlsxWithIncorrectPasswordSetTest() throws IOException {
        MultipartFile file = getMultipartFile("src/test/resources/WithIncorrectPassword.xlsx", TYPE_XLSX);
        Assertions.assertThatThrownBy(() -> excelValidatorServiceImpl.validateExcelFile(file))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_PASSWORD_INCORRECT_ERROR_MESSAGE);
    }

    @Test
    public void sendXlsxWithNoPasswordSetTest() throws IOException {
        MultipartFile file = getMultipartFile("src/test/resources/WithNoPasswordSet.xlsx", TYPE_XLSX);
        Assertions.assertThatThrownBy(() -> excelValidatorServiceImpl.validateExcelFile(file))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE);
    }

    @Test
    public void sendTextFileTest() throws IOException {
        MultipartFile file = getMultipartFile("src/test/resources/test.txt", "text/plain");
        Assertions.assertThatThrownBy(() -> excelValidatorServiceImpl.validateExcelFile(file))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE);
    }

    @Test
    public void sendNoFileTest() {
        Assertions.assertThatThrownBy(() -> excelValidatorServiceImpl.validateExcelFile(null))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NOT_PRESENT_ERROR_MESSAGE);
    }

    @Test
    public void sendNoFileContentNullTest() throws IOException {
        MultipartFile file = getMultipartFile("src/test/resources/test.txt", null);
        Assertions.assertThatThrownBy(() -> excelValidatorServiceImpl.validateExcelFile(file))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NOT_PRESENT_ERROR_MESSAGE);
    }
}
