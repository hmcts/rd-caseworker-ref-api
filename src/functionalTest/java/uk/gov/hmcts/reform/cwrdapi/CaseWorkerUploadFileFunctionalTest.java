package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import io.restassured.specification.MultiPartSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class CaseWorkerUploadFileFunctionalTest extends AuthorizationFunctionalTest{

    @Test
    public void shouldUploadXlsxFileSuccessfully() throws IOException {
        uploadFile("src/functionalTest/resources/WithCorrectPassword.xlsx",
                201, "Request Completed Successfully");
    }

    @Test
    public void shouldUploadXlsFileSuccessfully() throws IOException {
        uploadFile("src/functionalTest/resources/WithCorrectPassword.xls",
                201, "Request Completed Successfully");
    }

    @Test
    public void shouldReturn400WhenFileFormatIsInvalid() throws IOException {
        uploadFile("src/functionalTest/resources/test.txt", 400,
                CaseWorkerConstants.FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE);
    }

    @Test
    public void shouldReturn400WhenXlsFileHasIncorrectPasswordSet() throws IOException {
        uploadFile("src/functionalTest/resources/WithInCorrectPassword.xls",
                400, CaseWorkerConstants.FILE_PASSWORD_INCORRECT_ERROR_MESSAGE);
    }

    @Test
    public void shouldReturn400WhenXlsxFileHasIncorrectPasswordSet() throws IOException {
        uploadFile("src/functionalTest/resources/WithInCorrectPassword.xlsx",
                400, CaseWorkerConstants.FILE_PASSWORD_INCORRECT_ERROR_MESSAGE);
    }

    @Test
    public void shouldReturn400WhenXlsFileIsNotPasswordProtected() throws IOException {
        uploadFile("src/functionalTest/resources/WithNoPasswordSet.xls",
                400, CaseWorkerConstants.FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE);
    }

    @Test
    public void shouldReturn400WhenXlsxFileIsNotPasswordProtected() throws IOException {
        uploadFile("src/functionalTest/resources/WithNoPasswordSet.xlsx",
                400, CaseWorkerConstants.FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE);
    }

    @Test
    public void shouldReturn400WhenFileHasNoData() throws IOException {
        uploadFile("src/functionalTest/resources/WithXlsxOnlyHeader.xlsx",
                400, CaseWorkerConstants.FILE_NO_DATA_ERROR_MESSAGE);
    }

    private void uploadFile(String filePath,
                            int statusCode,
                            String messageBody) throws IOException {
        MultiPartSpecification multiPartSpec = caseWorkerApiClient
                .getMultipartFile(filePath);

        Response response = caseWorkerApiClient.getMultiPartMultipleAuthHeaders()
                .multiPart(multiPartSpec)
                .post("/refdata/case-worker/upload-file/")
                .andReturn();
        String responseBody = response.then()
                .assertThat()
                .statusCode(statusCode)
                .extract()
                .asString();
        assertTrue(responseBody.contains(messageBody));
    }
}
