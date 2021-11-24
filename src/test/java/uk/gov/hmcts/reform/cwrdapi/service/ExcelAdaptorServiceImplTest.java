package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ExcelAdaptorServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_DATA_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_VALID_SHEET_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUIRED_ROLE_MAPPING_SHEET_NAME;

@ExtendWith(MockitoExtension.class)
public class ExcelAdaptorServiceImplTest {

    @Mock
    IValidationService validationService;

    @InjectMocks
    ExcelAdaptorServiceImpl excelAdaptorServiceImpl;

    @BeforeEach
    public void initialize() {
        List<String> acceptableCaseWorkerHeaders = List.of("First Name","Last Name","Email","Region","Region ID",
                "Primary Base Location Name","Primary Base Location ID","Secondary Location","Secondary Location ID",
                "User type","Primary Role","Secondary Role","Service1","Service1 ID","Service2",
                "Service2 ID","Service2","Service2 ID","Service4","Service4 ID",
                "Service5","Service5 ID","Service6","Service6 ID","Service7",
                "Service7 ID","Service8","Service8 ID","IDAM Roles","Suspended");
        List<String> acceptableServiceRoleMappingHeaders = List.of("Service ID","Role","IDAM Roles");
        ReflectionTestUtils.setField(excelAdaptorServiceImpl, "acceptableCaseWorkerHeaders",
            acceptableCaseWorkerHeaders);
        ReflectionTestUtils.setField(excelAdaptorServiceImpl, "acceptableServiceRoleMappingHeaders",
                acceptableServiceRoleMappingHeaders);
    }

    @Test
    public void parseXlsxShouldThrowExceptionWhenOnlyHeaderPresentTest() throws IOException {
        Workbook workbook = WorkbookFactory
            .create(new File("src/test/resources/Staff Data Upload_WithXlsxOnlyHeader.xlsx"),
                "1234");

        Assertions.assertThatThrownBy(() -> excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class))
            .isExactlyInstanceOf(ExcelValidationException.class)
            .hasMessage(FILE_NO_DATA_ERROR_MESSAGE);
    }

    @Test
    public void parseXlsxShouldThrowExceptionWhenNoValidSheetNamePresentTest() throws IOException {
        Workbook workbook = WorkbookFactory
            .create(new File("src/test/resources/Staff Data Upload_WithNoValidSheetName.xlsx"),
                "1234");

        Assertions.assertThatThrownBy(() -> excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class))
            .isExactlyInstanceOf(ExcelValidationException.class)
            .hasMessage(FILE_NO_VALID_SHEET_ERROR_MESSAGE);
    }

    @Test
    public void parseXlsxWhichHasFormula() throws IOException {
        Workbook workbook = WorkbookFactory
            .create(new File("src/test/resources/Staff Data Upload With Formula.xlsx"));

        List<CaseWorkerProfile> profiles = excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class);
        assertThat(profiles).hasSize(workbook.getSheet(CaseWorkerConstants.REQUIRED_CW_SHEET_NAME)
            .getPhysicalNumberOfRows() - 49);
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
    public void parseXlsxWhichHasFormulaWithEmptyRows() throws IOException {
        Workbook workbook = WorkbookFactory
            .create(new File("src/test/resources/Staff Data Upload.xlsx"));
        List<CaseWorkerProfile> profiles = excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class);
        assertThat(profiles).hasSize(workbook.getSheet(CaseWorkerConstants.REQUIRED_CW_SHEET_NAME)
            .getPhysicalNumberOfRows() - 49);
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
        assertThat(serviceRoleMapping.getServiceId()).isEqualTo("BBA9");
    }

    @Test
    public void sendCwXlsxWithIncorrectHeaders() throws IOException {
        Workbook workbook = WorkbookFactory
            .create(new File("src/test/resources/Staff Data UploadWithInvalidHeaders.xls"));
        when(validationService.getAuditJobId()).thenReturn(1L);

        Assertions.assertThatThrownBy(() -> excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class))
            .isExactlyInstanceOf(ExcelValidationException.class)
            .hasMessage(CaseWorkerConstants.FILE_MISSING_HEADERS);
    }

    @Test
    public void sendServiceRoleMappingXlsxWithIncorrectHeaders() throws IOException {
        Workbook workbook = WorkbookFactory
                .create(new File("src/test/resources/ServiceRoleMapping_InvalidHeaders.xlsx"));
        when(validationService.getAuditJobId()).thenReturn(1L);
        Assertions.assertThatThrownBy(() -> excelAdaptorServiceImpl.parseExcel(workbook, ServiceRoleMapping.class))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(CaseWorkerConstants.FILE_MISSING_HEADERS);
    }

    @Test
    public void parseXlsxShouldThrowExceptionWhenBlankRowsPresentTest() throws IOException {
        Workbook workbook = WorkbookFactory
                .create(new File("src/test/resources/Staff Data Upload With Empty Rows.xlsx"));

        Assertions.assertThatThrownBy(() -> excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class))
                .isExactlyInstanceOf(ExcelValidationException.class)
                .hasMessage(FILE_NO_DATA_ERROR_MESSAGE);
    }
}
