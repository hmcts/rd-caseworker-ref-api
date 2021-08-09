package uk.gov.hmcts.reform.cwrdapi;

import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.FAILURE;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLSX;

public class CaseWorkerCreateUserTest extends FileUploadTest {

    String expectedSuspendFailureResponse = "{\"message\":\"Request completed with partial success. "
            + "Some records failed during validation and were ignored.\","
            + "\"message_details\":\"%s record(s) failed validation\","
            + "\"error_details\":[{\"row_id\":\"%s\",\"error_description\":\"An update to the user is not possible"
            + " at this moment. Please try again later.\"}]}";

    @Test
    public void shouldCreateCaseWorkerAuditFailureOnConflict() throws IOException {
        //create invalid stub of UP for Exception validation
        userProfileService.resetAll();
        userProfileService.stubFor(post(urlEqualTo("/v1/userprofile")));
        uploadCaseWorkerFile("Staff Data Upload.xlsx",
                CaseWorkerConstants.TYPE_XLSX, "500", cwdAdmin);
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(FAILURE.getStatus());
        assertThat(exceptionCaseWorkers.size()).isEqualTo(2);
        assertNotNull(exceptionCaseWorkers.get(0).getErrorDescription());
    }

    @Test
    public void shouldCreateCaseWorkerAuditFailure() throws IOException {
        //create invalid stub of UP for Exception validation
        userProfileService.resetAll();
        userProfileService.stubFor(post(urlEqualTo("/v1/userprofile")));
        uploadCaseWorkerFile("Staff Data Upload.xlsx",
                TYPE_XLSX, "500", cwdAdmin);
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
                + "\"error_details\":[{\"row_id\":\"2\","
                + "\"error_description\":\"User creation is not possible at this moment. "
                + "Please try again later or check with administrator.\"}]}";

        response = uploadCaseWorkerFile("Staff Data Upload.xlsx",
                TYPE_XLSX, "200 OK", cwdAdmin);
        String json = getJsonResponse(response);
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
                .isEqualTo(objectMapper.readValue(exceptedResponse, CaseWorkerFileCreationResponse.class));
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(PARTIAL_SUCCESS.getStatus());
        userProfileService.resetAll();
    }

    @Test
    public void shouldCreateCaseWorkerUpdateAuditFailureWithWrongStatus() throws Exception {
        validateAuditCaseWorkerCreate();
        modifyUserStatus(400);
        response = uploadCaseWorkerFile("Staff Data Upload Suspended.xlsx",
                CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);
        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
                .isEqualTo(objectMapper.readValue(format(expectedSuspendFailureResponse, 1, 2),
                        CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAuditsUpdate = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAuditsUpdate.size()).isEqualTo(2);
    }

    public Map<String, Object> uploadCaseWorkerFile(String fileName,
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

}
