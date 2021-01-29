package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.FAILURE;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.SUCCESS;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CaseWorkerUploadFileIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    SimpleJpaRepository<CaseWorkerAudit, Long> caseWorkerAuditRepository;

    @Autowired
    SimpleJpaRepository<ExceptionCaseWorker, Long> caseWorkerExceptionRepository;

    @Autowired
    ObjectMapper objectMapper;

    Map<String, Object> response = new HashMap<>();

    @Test
    public void shouldUploadCaseWorkerUsersXlsxFileSuccessfully() throws IOException {
        uploadCaseWorkerFile("CaseWorkerUserXlsxWithNoPassword.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
    }

    @Test
    public void shouldUploadCaseWorkerUsersXlsFileSuccessfully() throws IOException {
        uploadCaseWorkerFile("CaseWorkerUserXlsWithNoPassword.xls",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
    }

    @Test
    public void shouldUploadServiceRoleMappingsXlsxFileSuccessfully() throws IOException {

        String exceptedResponse = "{\"message\":\"Request Completed Successfully\","
            + "\"message_details\":\"4 record(s) uploaded\"}";
        Map<String, Object> response = uploadCaseWorkerFile("ServiceRoleMapping_BBA9.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);

        //Audit & Exception for service Role Mapping
        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(exceptedResponse, CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(SUCCESS.getStatus());
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(exceptionCaseWorkers).isEmpty();
    }

    @Test
    public void shouldReturn400WhenFileFormatIsInvalid() throws IOException {
        uploadCaseWorkerFile("test.txt",
            CaseWorkerConstants.TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenXlsFileIsPasswordProtected() throws IOException {
        uploadCaseWorkerFile("CaseWorkerUserWithPassword.xls",
            CaseWorkerConstants.TYPE_XLS, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenXlsxFileIsPasswordProtected() throws IOException {
        uploadCaseWorkerFile("CaseWorkerUserWithPassword.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenFileHasNoData() throws IOException {
        uploadCaseWorkerFile("CaseWorkerUserXlsxWithOnlyHeader.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenContentTypeIsInvalid() throws IOException {
        uploadCaseWorkerFile("CaseWorkerUserXlsxWithOnlyHeader.xlsx",
            "application/octet-stream", "400", cwdAdmin);
    }

    @Test
    public void shouldReturn403WhenRoleIsInvalid() throws IOException {
        uploadCaseWorkerFile("CaseWorkerUserXlsxWithOnlyHeader.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "403", "invalid");
    }

    @Test
    public void shouldReturn403WhenLdFeatureIsDisabled() throws IOException {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("CaseWorkerRefController.caseWorkerFileUpload",
            "test-flag-1");
        when(featureToggleServiceImpl.isFlagEnabled(anyString(), anyString())).thenReturn(false);
        when(featureToggleServiceImpl.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        uploadCaseWorkerFile("CaseWorkerUserWithPassword.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "403", cwdAdmin);
    }

    private Map<String, Object> uploadCaseWorkerFile(String fileName,
                                                     String fileType,
                                                     String status,
                                                     String role) throws IOException {

        response.clear();
        File file = getFile("src/integrationTest/resources/" + fileName);
        FileInputStream input = new FileInputStream(file);

        MockMultipartFile multipartFile = new MockMultipartFile("file",
            file.getName(), null, IOUtils.toByteArray(input));
        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
            .builder("form-data")
            .name("file")
            .filename(fileName)
            .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        fileMap.add(HttpHeaders.CONTENT_TYPE, fileType);
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(multipartFile.getBytes(), fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        response = caseworkerReferenceDataClient
            .uploadCwFile(body, role);

        assertThat(response).containsEntry("http_status", status);

        return response;
    }

    @Test
    public void shouldCreateCaseWorkerAuditSuccess() throws IOException {

        String exceptedResponse = "{\"message\":\"Request Completed Successfully\","
            + "\"message_details\":\"1 record(s) uploaded\"}";

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        response = uploadCaseWorkerFile("CaseWorkerUserXlsxWithNoPassword.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);

        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(exceptedResponse, CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(SUCCESS.getStatus());
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(exceptionCaseWorkers).isEmpty();
    }


    @Test
    public void shouldCreateCaseWorkerAuditPartialSuccess() throws IOException {

        String exceptedResponse = "{\"message\":\"Request completed with partial success. "
            + "Some records failed during validation and were ignored.\","
            + "\"message_details\":\"1 record(s) failed validation, 1 record(s) uploaded\","
            + "\"error_details\":[{\"row_id\":\"2\",\"field_in_error\":\"lastName\","
            + "\"error_description\":\"must not be empty\"},"
            + "{\"row_id\":\"2\",\"field_in_error\":\"locations\","
            + "\"error_description\":\"no primary or secondary location exists\"}]}";

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        response = uploadCaseWorkerFile("CaseWorkerUserXlsWithJSR.xls",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);

        CaseWorkerFileCreationResponse resultResponse =
            objectMapper.readValue(getJsonResponse(response), CaseWorkerFileCreationResponse.class);
        CaseWorkerFileCreationResponse expectedResponse =
            objectMapper.readValue(exceptedResponse, CaseWorkerFileCreationResponse.class);

        assertThat(expectedResponse.getDetailedMessage()).isEqualTo(resultResponse.getDetailedMessage());
        assertThat(expectedResponse.getErrorDetails()).containsAll(resultResponse.getErrorDetails());
        assertThat(expectedResponse.getMessage()).isEqualTo(resultResponse.getMessage());

        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(PARTIAL_SUCCESS.getStatus());
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(exceptionCaseWorkers).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    public void shouldCreateCaseWorkerAuditFailure() throws IOException {
        //create invalid stub of UP for Exception validation
        userProfileService.resetAll();
        userProfileService.stubFor(post(urlEqualTo("/v1/userprofile")));
        uploadCaseWorkerFile("CaseWorkerUserXlsxWithNoPassword.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "500", cwdAdmin);
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(FAILURE.getStatus());
        assertThat(exceptionCaseWorkers.size()).isEqualTo(2);
        assertNotNull(exceptionCaseWorkers.get(0).getErrorDescription());
    }

    @Test
    public void shouldCreateCaseWorkerAuditUpFailure() throws IOException {
        userProfileService.resetAll();
        String exceptedResponse = "{\"message\":\"Request completed with partial success. "
            + "Some records failed during validation and were ignored.\","
            + "\"message_details\":\"1 record(s) failed validation\","
            + "\"error_details\":[{\"row_id\":\"1\","
            + "\"error_description\":\"Failed to create in UP with response status 404\"}]}";

        response = uploadCaseWorkerFile("CaseWorkerUserXlsxWithNoPassword.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
        String json = getJsonResponse(response);
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(exceptedResponse, CaseWorkerFileCreationResponse.class));
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(PARTIAL_SUCCESS.getStatus());
        userProfileService.resetAll();
    }

    @Test
    public void shouldUploadServiceRoleMappingsXlsxFileWithJsr() throws IOException {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        String exceptedResponse = "{\"message\":\"Request completed with partial success. "
            + "Some records failed during validation and were ignored.\","
            + "\"message_details\":\"2 record(s) failed validation, 2 record(s) uploaded\","
            + "\"error_details\":[{\"row_id\":\"1\",\"field_in_error\":\"serivceId\",\"error_description\":"
            + "\"must not be empty\"},{\"row_id\":\"2\",\"field_in_error\":\"idamRoles\","
            + "\"error_description\":\"must not be empty\"},"
            + "{\"row_id\":\"2\",\"field_in_error\":\"roleId\",\"error_description\":\"must not be null\"}]}";

        response = uploadCaseWorkerFile("ServiceRoleMapping_BBA9WithJSR.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);

        //Audit & Exception for service Role Mapping
        CaseWorkerFileCreationResponse resultResponse =
            objectMapper.readValue(getJsonResponse(response), CaseWorkerFileCreationResponse.class);
        CaseWorkerFileCreationResponse expectedResponse =
            objectMapper.readValue(exceptedResponse, CaseWorkerFileCreationResponse.class);

        assertThat(expectedResponse.getDetailedMessage()).isEqualTo(resultResponse.getDetailedMessage());
        assertThat(expectedResponse.getErrorDetails()).containsAll(resultResponse.getErrorDetails());
        assertThat(expectedResponse.getMessage()).isEqualTo(resultResponse.getMessage());

        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(PARTIAL_SUCCESS.getStatus());
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(exceptionCaseWorkers.size()).isEqualTo(3);
    }

    private String getJsonResponse(Map<String, Object> response) {
        Gson gson = new Gson();
        String json = gson.toJson(response.get("body"));
        return json;
    }
}
