package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.MultiPartSpecification;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.poi.util.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.IdamRolesMappingResponse;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureConditionEvaluation;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.ResourceUtils.getFile;

@RunWith(CustomSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
public class CaseWorkerUploadFileFunctionalTest extends AuthorizationFunctionalTest {

    public static final String CWD_ADMIN = "cwd-admin";

    public static final String CASEWORKER_FILE_UPLOAD = "CaseWorkerRefController.caseWorkerFileUpload";

    @Test
    @Ignore(value = "Need to delete the case worker ids in IDAM, UP and in CRD so that next time when the test runs "
            + "it would create new users instead of updating. Deleting the ids requires a lot of effort. "
            + "So ignoring the test for now and it will be worked as a separate task")
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldUploadXlsxFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/CaseWorkerUserWithNoPassword.xlsx",
                201, CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY,
                CaseWorkerConstants.TYPE_XLSX, CWD_ADMIN);
        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerProfileCreationResponse.class);
        assertEquals(CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY,
                caseWorkerProfileCreationResponse.getCaseWorkerRegistrationResponse());
        assertFalse(caseWorkerProfileCreationResponse.getCaseWorkerIds().isEmpty());
    }

    @Test
    @Ignore(value = "Need to delete the case worker ids in IDAM, UP and in CRD so that when the test runs next time,"
            + " it would create new users instead of updating. Deleting the ids requires a lot of effort. "
            + "So ignoring the test for now and it will be worked as a separate task")
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldUploadXlsFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/CaseWorkerUserXlsWithNoPassword.xls",
                201, CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY, CaseWorkerConstants.TYPE_XLS,
                CWD_ADMIN);

        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerProfileCreationResponse.class);
        assertEquals(CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY,
                caseWorkerProfileCreationResponse.getCaseWorkerRegistrationResponse());
        assertFalse(caseWorkerProfileCreationResponse.getCaseWorkerIds().isEmpty());
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldUploadServiceRoleMappingXlsxFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/ServiceRoleMapping_BBA9.xlsx",
                201, CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS, CaseWorkerConstants.TYPE_XLS,
                CWD_ADMIN);

        IdamRolesMappingResponse caseWorkerProfileCreationResponse = uploadCaseWorkerFileResponse
                .as(IdamRolesMappingResponse.class);
        assertTrue(caseWorkerProfileCreationResponse.getMessage()
                .contains(CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS));
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    public void shouldUploadServiceRoleMappingXlsFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/ServiceRoleMapping_BBA9.xls",
                201, CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS, CaseWorkerConstants.TYPE_XLS,
                CWD_ADMIN);

        IdamRolesMappingResponse caseWorkerProfileCreationResponse = uploadCaseWorkerFileResponse
                .as(IdamRolesMappingResponse.class);
        assertTrue(caseWorkerProfileCreationResponse.getMessage()
                .contains(CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS));
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
        uploadCaseWorkerFile("src/functionalTest/resources/CaseWorkerUserWithPassword.xlsx",
                403, null,
                CaseWorkerConstants.TYPE_XLSX, "Invalid");
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = false)
    public void shouldReturn403WhenUploadFileApiToggledOff() throws IOException {

        String exceptionMessage = CustomSerenityRunner.getFeatureFlagName().concat(" ")
                .concat(FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD);

        uploadCaseWorkerFile("src/functionalTest/resources/CaseWorkerUserWithNoPassword.xlsx",
                403, exceptionMessage,
                CaseWorkerConstants.TYPE_XLSX, CWD_ADMIN);
    }

    private ExtractableResponse<Response> uploadCaseWorkerFile(String filePath,
                                                               int statusCode,
                                                               String messageBody,
                                                               String header,
                                                               String role) throws IOException {
        MultiPartSpecification multiPartSpec =  getMultipartFile(filePath, header);

        Response response = caseWorkerApiClient.getMultiPartWithAuthHeaders(role)
                .multiPart(multiPartSpec)
                .post("/refdata/case-worker/upload-file")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(statusCode);

        return response.then()
                .extract();
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
