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
import uk.gov.hmcts.reform.cwrdapi.service.impl.ExcelAdaptorServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ExcelValidatorServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_DATA_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_PASSWORD_PROTECTED_ERROR_MESSAGE;

@RunWith(SpringIntegrationSerenityRunner.class)
public class ExcelUploaderTest extends AuthorizationEnabledIntegrationTest {

    public static String TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static String TYPE_XLS = "application/vnd.ms-excel";

    @Autowired
    ExcelValidatorServiceImpl excelValidatorService;

    @Autowired
    ExcelAdaptorServiceImpl excelAdaptorService;

    @Test
    public void sendXlsTest() throws IOException {
        Workbook workbook = excelValidatorService
                .validateExcelFile(getMultipartFile("src/integrationTest/resources/xlsWithNoPassword.xls",
                        TYPE_XLS));
        assertThat(workbook).isNotNull();
    }

    @Test
    public void sendXlsWithPasswordSetTest() throws IOException {
        MultipartFile file = getMultipartFile("src/integrationTest/resources/WithPassword.xls",
                TYPE_XLS);
        Assertions.assertThatThrownBy(() -> excelValidatorService.validateExcelFile(file))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_PASSWORD_PROTECTED_ERROR_MESSAGE);
    }

    @Test
    public void sendXlsxTest() throws IOException {
        MultipartFile file = getMultipartFile("src/integrationTest/resources/xlsxWithNoPassword.xlsx",
                TYPE_XLSX);
        Workbook workbook = excelValidatorService.validateExcelFile(file);
        assertThat(workbook).isNotNull();
    }

    @Test
    public void sendXlsxWithPasswordSetTest() throws IOException {
        MultipartFile file = getMultipartFile("src/integrationTest/resources/WithPassword.xlsx",
                TYPE_XLSX);
        Assertions.assertThatThrownBy(() -> excelValidatorService.validateExcelFile(file))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_PASSWORD_PROTECTED_ERROR_MESSAGE);
    }

    @Test
    public void sendTextFileTest() throws IOException {
        MultipartFile file = getMultipartFile("src/integrationTest/resources/test.txt",
                "text/plain");
        Assertions.assertThatThrownBy(() -> excelValidatorService.validateExcelFile(file))
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
                getMultipartFile("src/integrationTest/resources/xlsxWithNoPassword.xlsx",
                        TYPE_XLSX));
        List<CaseWorkerProfile> profiles = excelAdaptorService.parseExcel(workbook, CaseWorkerProfile.class);
        assertThat(profiles).hasSize(workbook.getSheetAt(1).getPhysicalNumberOfRows() - 1);
        CaseWorkerProfile caseWorkerProfile = profiles.get(0);
        assertThat(caseWorkerProfile.getFirstName()).isNotBlank();
        assertThat(caseWorkerProfile.getLastName()).isNotBlank();
        assertThat(caseWorkerProfile.getOfficialEmail()).isNotBlank();
        assertThat(caseWorkerProfile.getRegionName()).isNotBlank();
        assertThat(caseWorkerProfile.getUserType()).isNotBlank();
        assertThat(caseWorkerProfile.getIdamRoles()).isNotBlank();
        assertThat(caseWorkerProfile.getSuspended()).isNotBlank();
    }

    @Test
    public void parseXlsxShouldThrowExceptionWhenOnlyHeaderPresentTest() throws IOException {
        Workbook workbook = excelValidatorService.validateExcelFile(
                getMultipartFile("src/integrationTest/resources/xlsxWithOnlyHeader.xlsx",
                        TYPE_XLSX));

        Assertions.assertThatThrownBy(() -> excelAdaptorService.parseExcel(workbook, CaseWorkerProfile.class))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NO_DATA_ERROR_MESSAGE);
    }
}