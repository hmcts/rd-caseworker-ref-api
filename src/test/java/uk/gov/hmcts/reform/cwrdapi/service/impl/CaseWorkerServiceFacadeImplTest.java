package uk.gov.hmcts.reform.cwrdapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerDomain;
import uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.client.domain.ServiceRoleMapping;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.InvalidRequestException;
import uk.gov.hmcts.reform.cwrdapi.controllers.internal.impl.CaseWorkerInternalApiClientImpl;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.repository.ExceptionCaseWorkerRepository;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerProfileConverter;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelAdaptorService;
import uk.gov.hmcts.reform.cwrdapi.service.ExcelValidatorService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
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
    ValidationServiceFacadeImpl validationServiceFacadeImpl;
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
    @Mock
    ExceptionCaseWorkerRepository exceptionCaseWorkerRepository;

    @Test
    public void shouldProcessCaseWorkerFile() throws IOException {
        MultipartFile multipartFile = createCaseWorkerMultiPartFile("Staff Data Upload.xlsx");

        when(exceptionCaseWorkerRepository.findByJobId(anyLong())).thenReturn(new ArrayList<>());

        ResponseEntity<Object> responseEntity =
            caseWorkerServiceFacade.processFile(multipartFile);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldProcessCaseWorkerFileWithoutInvalidRecords() throws IOException {
        MultipartFile multipartFile = createCaseWorkerFileWithoutInvalidRecords("Staff Data Upload.xlsx");

        when(exceptionCaseWorkerRepository.findByJobId(anyLong())).thenReturn(new ArrayList<>());

        ResponseEntity<Object> responseEntity =
                caseWorkerServiceFacade.processFile(multipartFile);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void shouldProcessCaseWorkerFileWithPartialSuccess() throws IOException {


        MultipartFile multipartFile = createCaseWorkerMultiPartFile("Staff Data Upload With Jsr.xlsx");

        List<ExceptionCaseWorker> exceptionCaseWorkers =
            ImmutableList.of(ExceptionCaseWorker.builder().errorDescription("Up Failed").excelRowId("1").build());
        when(exceptionCaseWorkerRepository.findByJobId(anyLong())).thenReturn(exceptionCaseWorkers);
        ResponseEntity<Object> responseEntity = caseWorkerServiceFacade.processFile(multipartFile);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test(expected = Exception.class)
    public void shouldProcessCaseWorkerFileFailure() throws IOException {
        MultipartFile multipartFile = createCaseWorkerMultiPartFile("Staff Data Upload.xlsx");
        when(exceptionCaseWorkerRepository.findByJobId(anyLong())).thenThrow(new RuntimeException("Failure test"));
        caseWorkerServiceFacade.processFile(multipartFile);
        verify(caseWorkerServiceFacade).processFile(multipartFile);

    }


    @Test
    public void shouldProcessServiceRoleMappingFile() throws IOException {
        ServiceRoleMapping serviceRoleMapping = ServiceRoleMapping
            .builder()
            .roleId(1)
            .build();

        List<ServiceRoleMapping> serviceRoleMappings = new ArrayList<>();
        serviceRoleMappings.add(serviceRoleMapping);

        CaseWorkerProfile invalidCaseWorker = CaseWorkerProfile.builder()
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

        when(exceptionCaseWorkerRepository.findByJobId(anyLong())).thenReturn(new ArrayList<>());

        ResponseEntity<Object> responseEntity =
            caseWorkerServiceFacade.processFile(multipartFile);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test(expected = InvalidRequestException.class)
    public void shouldProcessServiceRoleMappingFileFailure() throws IOException {

        List<ServiceRoleMapping> serviceRoleMappings = new ArrayList<>();

        serviceRoleMappings.add(ServiceRoleMapping.builder().roleId(1).serviceId("BBA1").build());
        serviceRoleMappings.add(ServiceRoleMapping.builder().roleId(1).serviceId("BBA2").build());

        MultipartFile multipartFile =
            getMultipartFile("src/test/resources/ServiceRoleMapping_Multiple_Ids.xls", TYPE_XLS);
        when(excelValidatorService.validateExcelFile(multipartFile))
            .thenReturn(workbook);
        when(excelAdaptorService
            .parseExcel(workbook, ServiceRoleMapping.class))
            .thenReturn(serviceRoleMappings);
        caseWorkerServiceFacade.processFile(multipartFile);
    }

    private MultipartFile getMultipartFile(String filePath, String fileType) throws IOException {
        File file = getFile(filePath);
        FileInputStream input = new FileInputStream(file);
        return new MockMultipartFile("file",
            file.getName(), fileType, IOUtils.toByteArray(input));
    }

    @NotNull
    private MultipartFile createCaseWorkerMultiPartFile(String fileName) throws IOException {
        CaseWorkerProfile caseWorkerProfile1 = CaseWorkerProfile.builder()
            .firstName("first name")
            .build();

        CaseWorkerProfile caseWorkerProfile2 = CaseWorkerProfile.builder()
            .firstName("first name1")
            .build();
        List<CaseWorkerProfile> caseWorkerDomains = new ArrayList<>();
        caseWorkerDomains.add(caseWorkerProfile1);
        caseWorkerDomains.add(caseWorkerProfile2);
        List<CaseWorkerDomain> caseWorkerList = ImmutableList.of(caseWorkerProfile1);
        when(validationServiceFacadeImpl.getInvalidRecords(anyList())).thenReturn(caseWorkerList);
        MultipartFile multipartFile =
            getMultipartFile("src/test/resources/" + fileName, TYPE_XLS);
        when(excelValidatorService.validateExcelFile(multipartFile))
            .thenReturn(workbook);
        return multipartFile;
    }

    @NotNull
    private MultipartFile createCaseWorkerFileWithoutInvalidRecords(String fileName) throws IOException {
        when(validationServiceFacadeImpl.getInvalidRecords(anyList())).thenReturn(Collections.emptyList());
        MultipartFile multipartFile =
                getMultipartFile("src/test/resources/" + fileName, TYPE_XLS);
        when(excelValidatorService.validateExcelFile(multipartFile))
                .thenReturn(workbook);
        return multipartFile;
    }

    @Test
    public void shouldProcessCaseWorkerFileWithSuspendedRowFailed() throws IOException {
        CaseWorkerProfile caseWorkerProfile1 = CaseWorkerProfile.builder()
            .firstName("test").lastName("test")
            .officialEmail("test@justice.gov.uk")
            .regionId(1)
            .regionName("test")
            .userType("testUser")
            .build();

        CaseWorkerProfile caseWorkerProfile2 = CaseWorkerProfile.builder()
            .firstName("test1").lastName("test1")
            .officialEmail("test1@justice.gov.uk")
            .regionId(1)
            .regionName("test1")
            .userType("testUser")
            .build();
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();

        caseWorkerProfiles.add(caseWorkerProfile1);
        caseWorkerProfiles.add(caseWorkerProfile2);
        MultipartFile multipartFile = createCaseWorkerMultiPartFile("Staff Data Upload.xls");

        List<ExceptionCaseWorker> exceptionCaseWorkers =
            ImmutableList.of(ExceptionCaseWorker.builder().errorDescription("Up Failed").excelRowId("1").build());
        when(exceptionCaseWorkerRepository.findByJobId(anyLong())).thenReturn(exceptionCaseWorkers);
        when(excelAdaptorService
            .parseExcel(workbook, CaseWorkerProfile.class))
            .thenReturn(caseWorkerProfiles);
        when(validationServiceFacadeImpl.getInvalidRecords(anyList())).thenReturn(Collections.emptyList());
        when(caseWorkerProfileConverter.getSuspendedRowIds()).thenReturn(List.of(1L));
        ResponseEntity<Object> responseEntity = caseWorkerServiceFacade.processFile(multipartFile);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        ObjectMapper mapper = new ObjectMapper();
        String responseString = mapper.writeValueAsString(responseEntity.getBody());
        assertTrue(responseString.contains("1 record(s) failed validation and 1 record(s) uploaded"));
    }

    @Test
    public void shouldProcessCaseWorkerFileWithUploadedAndSuspendedMessage() throws IOException {
        CaseWorkerProfile caseWorkerProfile1 = CaseWorkerProfile.builder()
            .firstName("test").lastName("test")
            .officialEmail("test@justice.gov.uk")
            .regionId(1)
            .regionName("test")
            .userType("testUser")
            .build();

        CaseWorkerProfile caseWorkerProfile2 = CaseWorkerProfile.builder()
            .firstName("test1").lastName("test1")
            .officialEmail("test1@justice.gov.uk")
            .regionId(1)
            .regionName("test1")
            .userType("testUser")
            .build();
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();

        caseWorkerProfiles.add(caseWorkerProfile1);
        caseWorkerProfiles.add(caseWorkerProfile2);
        MultipartFile multipartFile = createCaseWorkerMultiPartFile("Staff Data Upload.xls");

        when(excelAdaptorService
            .parseExcel(workbook, CaseWorkerProfile.class))
            .thenReturn(caseWorkerProfiles);
        when(validationServiceFacadeImpl.getInvalidRecords(anyList())).thenReturn(Collections.emptyList());
        when(caseWorkerProfileConverter.getSuspendedRowIds()).thenReturn(List.of(1L));
        ResponseEntity<Object> responseEntity = caseWorkerServiceFacade.processFile(multipartFile);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        ObjectMapper mapper = new ObjectMapper();
        String responseString = mapper.writeValueAsString(responseEntity.getBody());
        assertTrue(responseString.contains("1 record(s) uploaded and 1 record(s) suspended"));
    }

    @Test
    public void shouldProcessCaseWorkerFileWithSuspendedFailedMessage() throws IOException {
        CaseWorkerProfile caseWorkerProfile1 = CaseWorkerProfile.builder()
            .firstName("test").lastName("test")
            .officialEmail("test@justice.gov.uk")
            .regionId(1)
            .regionName("test")
            .userType("testUser")
            .build();

        CaseWorkerProfile caseWorkerProfile2 = CaseWorkerProfile.builder()
            .firstName("test1").lastName("test1")
            .officialEmail("test1@justice.gov.uk")
            .regionId(1)
            .regionName("test1")
            .userType("testUser")
            .build();

        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();
        caseWorkerProfiles.add(caseWorkerProfile1);
        caseWorkerProfiles.add(caseWorkerProfile2);
        MultipartFile multipartFile = createCaseWorkerMultiPartFile("Staff Data Upload.xls");

        when(excelAdaptorService
            .parseExcel(workbook, CaseWorkerProfile.class))
            .thenReturn(caseWorkerProfiles);
        when(validationServiceFacadeImpl.getInvalidRecords(anyList())).thenReturn(Collections.emptyList());
        List<ExceptionCaseWorker> exceptionCaseWorkers =
            ImmutableList.of(ExceptionCaseWorker.builder().errorDescription("Up Failed").excelRowId("1").build());
        when(exceptionCaseWorkerRepository.findByJobId(anyLong())).thenReturn(exceptionCaseWorkers);
        when(caseWorkerProfileConverter.getSuspendedRowIds()).thenReturn(List.of(2L));
        ResponseEntity<Object> responseEntity = caseWorkerServiceFacade.processFile(multipartFile);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        ObjectMapper mapper = new ObjectMapper();
        String responseString = mapper.writeValueAsString(responseEntity.getBody());
        assertTrue(responseString.contains("1 record(s) failed validation and 1 record(s) suspended"));
    }

    @Test
    public void shouldProcessCaseWorkerFileWithUploadedFailedSuspendedMessage() throws IOException {
        CaseWorkerProfile caseWorkerProfile1 = CaseWorkerProfile.builder()
            .firstName("test").lastName("test")
            .officialEmail("test@justice.gov.uk")
            .regionId(1)
            .regionName("test")
            .userType("testUser")
            .build();

        CaseWorkerProfile caseWorkerProfile2 = CaseWorkerProfile.builder()
            .firstName("test1").lastName("test1")
            .officialEmail("test1@justice.gov.uk")
            .regionId(1)
            .regionName("test1")
            .userType("testUser")
            .build();

        CaseWorkerProfile caseWorkerProfile3 = CaseWorkerProfile.builder()
            .firstName("test").lastName("test")
            .officialEmail("test@justice.gov.uk")
            .regionId(1)
            .regionName("test")
            .userType("testUser")
            .build();
        List<CaseWorkerProfile> caseWorkerProfiles = new ArrayList<>();

        caseWorkerProfiles.add(caseWorkerProfile1);
        caseWorkerProfiles.add(caseWorkerProfile2);
        caseWorkerProfiles.add(caseWorkerProfile3);
        MultipartFile multipartFile = createCaseWorkerMultiPartFile("Staff Data Upload.xls");

        List<ExceptionCaseWorker> exceptionCaseWorkers =
            ImmutableList.of(ExceptionCaseWorker.builder().errorDescription("Up Failed").excelRowId("1").build());
        when(exceptionCaseWorkerRepository.findByJobId(anyLong())).thenReturn(exceptionCaseWorkers);
        when(excelAdaptorService
            .parseExcel(workbook, CaseWorkerProfile.class))
            .thenReturn(caseWorkerProfiles);
        when(validationServiceFacadeImpl.getInvalidRecords(anyList())).thenReturn(Collections.emptyList());
        when(caseWorkerProfileConverter.getSuspendedRowIds()).thenReturn(List.of(2L));
        ResponseEntity<Object> responseEntity = caseWorkerServiceFacade.processFile(multipartFile);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        ObjectMapper mapper = new ObjectMapper();
        String responseString = mapper.writeValueAsString(responseEntity.getBody());
        assertTrue(responseString.contains("1 record(s) failed validation, 1 record(s) uploaded, and "
            + "1 record(s) suspended"));
    }
}