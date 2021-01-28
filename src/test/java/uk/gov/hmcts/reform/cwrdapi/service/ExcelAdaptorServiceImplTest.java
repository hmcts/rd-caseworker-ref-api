package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ExcelAdaptorServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_DATA_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_VALID_SHEET_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUIRED_ROLE_MAPPING_SHEET_NAME;

@RunWith(MockitoJUnitRunner.class)
public class ExcelAdaptorServiceImplTest {

    @InjectMocks
    ExcelAdaptorServiceImpl excelAdaptorServiceImpl;

    @Before
    public void initialize() {
        List<String> acceptableHeaders = List.of("First Name", "Last Name", "Email",
                "Region", "User type");
        ReflectionTestUtils.setField(excelAdaptorServiceImpl, "acceptableHeaders",
                acceptableHeaders);
    }

    @Test
    public void parseXlsxShouldThrowExceptionWhenOnlyHeaderPresentTest() throws IOException {
        Workbook workbook = WorkbookFactory
                .create(new File("src/test/resources/CaseWorkerUsers_WithXlsxOnlyHeader.xlsx"),
                        "1234");

        Assertions.assertThatThrownBy(() -> excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NO_DATA_ERROR_MESSAGE);
    }

    @Test
    public void parseXlsxShouldThrowExceptionWhenNoValidSheetNamePresentTest() throws IOException {
        Workbook workbook = WorkbookFactory
                .create(new File("src/test/resources/CaseWorkerUsers_WithNoValidSheetName.xlsx"),
                        "1234");

        Assertions.assertThatThrownBy(() -> excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NO_VALID_SHEET_ERROR_MESSAGE);
    }

    @Test
    public void parseXlsxWhichHasFormula() throws IOException {
        Workbook workbook = WorkbookFactory
                .create(new File("src/test/resources/CaseWorkerUserXlsWithFormula.xlsx"), "1234");

        List<CaseWorkerProfile> profiles = excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class);
        assertThat(profiles).hasSize(workbook.getSheetAt(1).getPhysicalNumberOfRows());
        CaseWorkerProfile caseWorkerProfile = (CaseWorkerProfile) profiles.get(0);
        assertThat(caseWorkerProfile.getFirstName()).isNotBlank();
        assertThat(caseWorkerProfile.getLastName()).isNotBlank();
        assertThat(caseWorkerProfile.getOfficialEmail()).isNotBlank();
        assertThat(caseWorkerProfile.getRegionName()).isNotBlank();
        assertThat(caseWorkerProfile.getUserType()).isNotBlank();
        assertThat(caseWorkerProfile.getIdamRoles()).isNotBlank();
        assertThat(caseWorkerProfile.getSuspended()).isNotBlank();
        assertThat(caseWorkerProfile.getLocations()).hasSize(2);
        assertThat(caseWorkerProfile.getRoles()).hasSize(2);
        assertThat(caseWorkerProfile.getWorkAreas()).hasSize(1);
    }

    @Test
    public void parseServiceRoleMappingXlsx() throws IOException {
        Workbook workbook = WorkbookFactory
                .create(new File("src/test/resources/ServiceRoleMapping_BBA9.xlsx"));

        List<ServiceRoleMapping> profiles = excelAdaptorServiceImpl.parseExcel(workbook, ServiceRoleMapping.class);
        assertThat(profiles).hasSize(workbook.getSheet(REQUIRED_ROLE_MAPPING_SHEET_NAME).getPhysicalNumberOfRows() - 1);
        ServiceRoleMapping serviceRoleMapping = profiles.get(0);
        assertThat(serviceRoleMapping.getRoleId()).isEqualTo(1);
        assertThat(serviceRoleMapping.getIdamRoles()).isEqualTo("caseworker-iac");
        assertThat(serviceRoleMapping.getSerivceId()).isEqualTo("BBA9");

    }

    @Test
    public void sendXlsxWithIncorrectHeaders() throws IOException {
        Workbook workbook = WorkbookFactory
                .create(new File("src/test/resources/CaseWorkerUserXlsWithInvalidHeaders.xls"));

        Assertions.assertThatThrownBy(() -> excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage("File is missing the required column header First Name Please check the file.");
    }

    /*@Test
    public void sendXlsxWithMissingLocation() throws IOException {
        Workbook workbook = WorkbookFactory
                .create(new File("src/test/resources/xlsxWithData_No_Location.xlsx"));
        Assertions.assertThatThrownBy(() -> excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_MISSING_HEADERS);
    }*/

    /*@Test
    public void sendXlsxWithMissingFirstName() throws IOException {
        Workbook workbook = WorkbookFactory
                .create(new File("src/test/resources/xlsxWithData_Missing_First_Name.xlsx"));
        Assertions.assertThatThrownBy(() -> excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_MISSING_HEADERS);
    }*/
}
