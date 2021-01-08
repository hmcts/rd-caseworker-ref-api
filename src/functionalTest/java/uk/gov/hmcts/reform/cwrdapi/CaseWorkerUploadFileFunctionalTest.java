package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.MultiPartSpecification;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureConditionEvaluation;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.springframework.util.ResourceUtils.getFile;

@RunWith(CustomSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class CaseWorkerUploadFileFunctionalTest extends AuthorizationFunctionalTest {

    public static final String CWD_ADMIN = "cwd-admin";

    public static final String CASEWORKER_FILE_UPLOAD = "CaseWorkerRefController.caseWorkerFileUpload";

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldUploadXlsxFileSuccessfully() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/WithCorrectPassword.xlsx",
                201, CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY,
                CaseWorkerConstants.TYPE_XLSX, CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldUploadXlsFileSuccessfully() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/WithCorrectPassword.xls",
                201, CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY, CaseWorkerConstants.TYPE_XLS,
                CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldReturn400WhenFileFormatIsInvalid() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/test.txt", 400,
                CaseWorkerConstants.FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE, CaseWorkerConstants.TYPE_XLSX,
                CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldReturn400WhenXlsFileHasIncorrectPasswordSet() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/WithInCorrectPasswordSet.xls",
                400, CaseWorkerConstants.FILE_PASSWORD_INCORRECT_ERROR_MESSAGE, CaseWorkerConstants.TYPE_XLS,
                CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldReturn400WhenXlsxFileHasIncorrectPasswordSet() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/WithInCorrectPasswordSet.xlsx",
                400, CaseWorkerConstants.FILE_PASSWORD_INCORRECT_ERROR_MESSAGE, CaseWorkerConstants.TYPE_XLSX,
                CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldReturn400WhenXlsFileIsNotPasswordProtected() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/WithNoPasswordSet.xls",
                400, CaseWorkerConstants.FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE,
                CaseWorkerConstants.TYPE_XLS, CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldReturn400WhenXlsxFileIsNotPasswordProtected() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/WithNoPasswordSet.xlsx",
                400, CaseWorkerConstants.FILE_NOT_PASSWORD_PROTECTED_ERROR_MESSAGE,
                CaseWorkerConstants.TYPE_XLSX, CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldReturn400WhenFileHasNoData() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/WithXlsxOnlyHeader.xlsx",
                400, CaseWorkerConstants.FILE_NO_DATA_ERROR_MESSAGE, CaseWorkerConstants.TYPE_XLSX,
                CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldReturn400WhenFileHasNoValidSheetName() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/WithInvalidSheetName.xlsx",
                400, CaseWorkerConstants.FILE_NO_VALID_SHEET_ERROR_MESSAGE, CaseWorkerConstants.TYPE_XLSX,
                CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldReturn400WhenContentTypeIsInvalid() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/WithCorrectPassword.xlsx",
                400, CaseWorkerConstants.FILE_NOT_EXCEL_TYPE_ERROR_MESSAGE,
                "application/octet-stream", CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldReturn401WhenAuthenticationInvalid() throws IOException {
        Response response = caseWorkerApiClient.withUnauthenticatedRequest()
                .post("/refdata/case-worker/upload-file/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(401);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldReturn403WhenRoleIsInvalid() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/WithCorrectPassword.xlsx",
                403, null,
                CaseWorkerConstants.TYPE_XLSX, "Invalid");
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = false)
    public void should_retrieve_403_when_upload_file_api_toggled_off() throws IOException {

        String exceptionMessage = CustomSerenityRunner.getFeatureFlagName().concat(" ")
                .concat(FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD);

        uploadCaseWorkerFile("src/functionalTest/resources/WithCorrectPassword.xlsx",
                403, exceptionMessage,
                CaseWorkerConstants.TYPE_XLSX, CWD_ADMIN);
    }

    private void uploadCaseWorkerFile(String filePath,
                                      int statusCode,
                                      String messageBody,
                                      String header,
                                      String role) throws IOException {
        MultiPartSpecification multiPartSpec =  getMultipartFile(filePath, header);

        Response response = caseWorkerApiClient.getMultiPartWithAuthHeaders(role)
                .multiPart(multiPartSpec)
                .post("/refdata/case-worker/upload-file/")
                .andReturn();
        String responseBody = response.then()
                .assertThat()
                .statusCode(statusCode)
                .extract()
                .asString();
        if (StringUtils.isNotBlank(messageBody)) {
            assertTrue(responseBody.contains(messageBody));
        }
    }


    private MultiPartSpecification getMultipartFile(String filePath,
                                                    String headerValue) throws IOException {
        File file = getFile(filePath);
        FileInputStream input = new FileInputStream(file);
        MultiPartSpecBuilder multiPartSpecBuilder =  new MultiPartSpecBuilder(IOUtils.toByteArray(input))
                .fileName(file.getName())
                .header("Content-Type",
                        headerValue);
        return multiPartSpecBuilder.build();
    }
}
