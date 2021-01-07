package uk.gov.hmcts.reform.cwrdapi;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.cwrdapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.ResourceUtils.getFile;

@RunWith(SpringIntegrationSerenityRunner.class)
public class CaseWorkerUploadFileIntegrationTest extends AuthorizationEnabledIntegrationTest {
    @Test
    public void shouldUploadXlsxFileSuccessfully() throws IOException {
        uploadCaseWorkerFile("WithCorrectPassword.xlsx",
                CaseWorkerConstants.TYPE_XLSX, "201 CREATED", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenFileFormatIsInvalid() throws IOException {
        uploadCaseWorkerFile("test.txt",
                CaseWorkerConstants.TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenXlsFileHasIncorrectPasswordSet() throws IOException {
        uploadCaseWorkerFile("WithIncorrectPassword.xls",
                CaseWorkerConstants.TYPE_XLS, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenXlsxFileHasIncorrectPasswordSet() throws IOException {
        uploadCaseWorkerFile("WithIncorrectPassword.xlsx",
                CaseWorkerConstants.TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenXlsFileIsNotPasswordProtected() throws IOException {
        uploadCaseWorkerFile("WithNoPasswordSet.xls",
                CaseWorkerConstants.TYPE_XLS, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenXlsxFileIsNotPasswordProtected() throws IOException {
        uploadCaseWorkerFile("WithNoPasswordSet.xlsx",
                CaseWorkerConstants.TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenFileHasNoData() throws IOException {
        uploadCaseWorkerFile("WithXlsxOnlyHeader.xlsx",
                CaseWorkerConstants.TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenContentTypeIsInvalid() throws IOException {
        uploadCaseWorkerFile("WithXlsxOnlyHeader.xlsx",
                "application/octet-stream", "400", cwdAdmin);
    }

    @Test
    public void shouldReturn403WhenRoleIsInvalid() throws IOException {
        uploadCaseWorkerFile("WithXlsxOnlyHeader.xlsx",
                CaseWorkerConstants.TYPE_XLSX, "403", "invalid");
    }

    private void uploadCaseWorkerFile(String fileName,
                                      String fileType,
                                      String status,
                                      String role) throws IOException {
        File file = getFile("src/integrationTest/resources/" + fileName);
        FileInputStream input = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile("file",
                file.getName(),null, IOUtils.toByteArray(input));

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

        Map<String, Object> response = caseworkerReferenceDataClient
                .uploadCwFile(body, role);

        assertThat(response).containsEntry("http_status", status);
    }
}
