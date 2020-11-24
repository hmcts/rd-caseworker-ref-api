package uk.gov.hmcts.reform.cwrdapi.service;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorServiceImpl.FILE_NO_DATA_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorServiceImpl.FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorServiceImpl.FILE_PASSWORD_INCORRECT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.service.WorkBookCustomFactory.FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE;

@RunWith(SpringIntegrationSerenityRunner.class)
public class ExcelUploaderTest extends AuthorizationEnabledIntegrationTest {

    public static String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static String TYPE_XLS = "application/vnd.ms-excel";

    @Autowired
    ExcelValidatorServiceImpl excelValidatorService;

    @Autowired
    ExcelAdaptorServiceImpl excelAdaptorService;

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

    @Test
    public void parseXlsxShouldReturnWorkbookObjectTest() throws IOException {
        Workbook workbook = excelValidatorService.validateExcelFile(
                getMultipartFile("src/integrationTest/resources/WithCorrectPassword.xlsx",
                        TYPE_XLSX));
        List<Object> profiles = excelAdaptorService.parseExcel(workbook, CaseWorkerProfile.class);
        assertThat(profiles).hasSize(workbook.getSheetAt(1).getPhysicalNumberOfRows() - 1);
        CaseWorkerProfile caseWorkerProfile = (CaseWorkerProfile) profiles.get(0);
        assertThat(caseWorkerProfile.getFirstName()).isNotBlank();
        assertThat(caseWorkerProfile.getLastName()).isNotBlank();
        assertThat(caseWorkerProfile.getOfficialEmail()).isNotBlank();
        assertThat(caseWorkerProfile.getRegionName()).isNotBlank();
        assertThat(caseWorkerProfile.getUserType()).isNotBlank();
        assertThat(caseWorkerProfile.getIdamRoles()).isNotBlank();
        assertThat(caseWorkerProfile.getDeleteFlag()).isNotBlank();
    }

    @Test
    public void parseXlsxShouldThrowExceptionWhenOnlyHeaderPresentTest() throws IOException {
        Workbook workbook = excelValidatorService.validateExcelFile(
                getMultipartFile("src/integrationTest/resources/WithXlsxOnlyHeader.xlsx",
                        TYPE_XLSX));

        Assertions.assertThatThrownBy(() -> excelAdaptorService.parseExcel(workbook, CaseWorkerProfile.class))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NO_DATA_ERROR_MESSAGE);
    }
}