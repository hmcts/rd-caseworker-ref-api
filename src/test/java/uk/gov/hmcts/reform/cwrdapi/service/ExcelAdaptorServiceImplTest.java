package uk.gov.hmcts.reform.cwrdapi.service;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.cwrdapi.advice.ExcelValidationException;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.impl.ExcelAdaptorServiceImpl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_DATA_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FILE_NO_VALID_SHEET_ERROR_MESSAGE;

@RunWith(MockitoJUnitRunner.class)
public class ExcelAdaptorServiceImplTest {

    @InjectMocks
    ExcelAdaptorServiceImpl excelAdaptorServiceImpl;

    @Test
    public void parseXlsxShouldReturnWorkbookObjectTest() throws IOException {
        Workbook workbook = WorkbookFactory
                .create(new File("src/test/resources/CaseWorkerUserWithPassword.xlsx"), "1234");

        List<CaseWorkerProfile> profiles = excelAdaptorServiceImpl.parseExcel(workbook, CaseWorkerProfile.class);
        assertThat(profiles).hasSize(workbook.getSheetAt(1).getPhysicalNumberOfRows() - 1);
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
        assertThat(caseWorkerProfile.getWorkAreas()).hasSize(8);
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
}
