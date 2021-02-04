package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.poi.util.IOUtils;
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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.SUCCESS;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class FileUploadTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    protected SimpleJpaRepository<CaseWorkerAudit, Long> caseWorkerAuditRepository;

    @Autowired
    protected SimpleJpaRepository<ExceptionCaseWorker, Long> caseWorkerExceptionRepository;

    @Autowired
    protected ObjectMapper objectMapper;

    protected Map<String, Object> response = new HashMap<>();

    protected String exceptedResponse = "{\"message\":\"Request Completed Successfully\","
        + "\"message_details\":\"%s record(s) uploaded\"}";


    public void validateAuditCaseWorkerCreate() throws IOException {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        response = uploadCaseWorkerFile("CaseWorkerUserXlsxWithNoPassword.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);

        String json = getJsonResponse(response);
        assertThat(objectMapper.readValue(json, CaseWorkerFileCreationResponse.class))
            .isEqualTo(objectMapper.readValue(format(exceptedResponse, 1), CaseWorkerFileCreationResponse.class));
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(SUCCESS.getStatus());
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(exceptionCaseWorkers).isEmpty();
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

    protected String getJsonResponse(Map<String, Object> response) {
        Gson gson = new Gson();
        String json = gson.toJson(response.get("body"));
        return json;
    }
}
