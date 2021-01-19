package uk.gov.hmcts.reform.cwrdapi.service.impl;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.internal.impl.CaseWorkerInternalApiClientImpl;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerProfileConverter;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorService;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLS;

@RunWith(MockitoJUnitRunner.class)
public class CaseWorkerServiceFacadeImplTest {
    @Mock
    ExcelAdaptorService excelAdaptorService;
    @Mock
    ExcelValidatorService excelValidatorService;
    @Mock
    ValidationService validationService;
    @Mock
    MultipartFile multipartFile;
    @Mock
    Workbook workbook;
    @Mock
    CaseWorkerInternalApiClientImpl caseWorkerInternalApiClient;
    @Mock
    CaseWorkerProfileConverter caseWorkerProfileConverter;
    @InjectMocks
    CaseWorkerServiceFacadeImpl caseWorkerServiceFacade;

    @Test
    public void shouldProcessCaseWorkerFile() throws IOException {
        CaseWorkerProfile caseWorkerProfile1 =  CaseWorkerProfile.builder()
                .firstName("first name")
                .build();

        CaseWorkerProfile caseWorkerProfile2 =  CaseWorkerProfile.builder()
                .firstName("first name1")
                .build();
        List<CaseWorkerProfile> caseWorkerDomains = new ArrayList<>();
        caseWorkerDomains.add(caseWorkerProfile1);
        caseWorkerDomains.add(caseWorkerProfile2);

        CaseWorkerProfile invalidCaseWorker =  CaseWorkerProfile.builder()
                .firstName("first name")
                .build();
        MultipartFile multipartFile =
                getMultipartFile("src/test/resources/CaseWorkerUserXlsWithNoPassword.xls", TYPE_XLS);
        when(excelValidatorService.validateExcelFile(multipartFile))
                .thenReturn(workbook);
        when(caseWorkerInternalApiClient.postRequest(any(), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
        when(excelAdaptorService
                .parseExcel(workbook, CaseWorkerProfile.class))
                .thenReturn(caseWorkerDomains);
        when(caseWorkerProfileConverter.convert(anyList()))
                .thenReturn(anyList());

        ResponseEntity<Object> responseEntity =
                caseWorkerServiceFacade.processFile(multipartFile);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void shouldProcessServiceRoleMappingFile() throws IOException {
        ServiceRoleMapping serviceRoleMapping =  ServiceRoleMapping
                .builder()
                .roleId(1)
                .build();

        List<ServiceRoleMapping> serviceRoleMappings = new ArrayList<>();
        serviceRoleMappings.add(serviceRoleMapping);

        CaseWorkerProfile invalidCaseWorker =  CaseWorkerProfile.builder()
                .firstName("first name")
                .build();
        MultipartFile multipartFile =
                getMultipartFile("src/test/resources/ServiceRoleMapping_BBA9.xlsx", TYPE_XLS);
        when(excelValidatorService.validateExcelFile(multipartFile))
                .thenReturn(workbook);
        when(caseWorkerInternalApiClient.postRequest(any(), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));
        when(excelAdaptorService
                .parseExcel(workbook, ServiceRoleMapping.class))
                .thenReturn(serviceRoleMappings);

        ResponseEntity<Object> responseEntity =
                caseWorkerServiceFacade.processFile(multipartFile);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private MultipartFile getMultipartFile(String filePath, String fileType) throws IOException {
        File file = getFile(filePath);
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile("file",
                file.getName(), fileType, IOUtils.toByteArray(input));
    }
}