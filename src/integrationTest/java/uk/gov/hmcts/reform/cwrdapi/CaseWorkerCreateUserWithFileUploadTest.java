package uk.gov.hmcts.reform.cwrdapi;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.JsrFileErrors;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.FAILURE;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.AREA_OF_WORK_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_PRIMARY_AND_SECONDARY_LOCATIONS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_PRIMARY_AND_SECONDARY_ROLES;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LOCATION_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RECORDS_FAILED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_FAILED_FILE_UPLOAD_JSR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLSX;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CaseWorkerCreateUserWithFileUploadTest extends FileUploadTest {

    @Test
    public void shouldUploadCaseWorkerUsersXlsxFileSuccessfully() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload.xlsx",
            TYPE_XLSX, "200 OK", cwdAdmin);
    }

    @Test
    public void shouldUploadCaseWorkerUsersXlsFileSuccessfully() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload Xls.xls",
            TYPE_XLSX, "200 OK", cwdAdmin);
    }

    @Test
    public void shouldUploadServiceRoleMappingsXlsxFileSuccessfully() throws IOException {

        String exceptedResponse = "{\"message\":\"Request Completed Successfully\","
            + "\"message_details\":\"4 record(s) uploaded\"}";
        Map<String, Object> response = uploadCaseWorkerFile("ServiceRoleMapping_BBA9.xlsx",
            TYPE_XLSX, "200 OK", cwdAdmin);

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
            TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenXlsFileIsPasswordProtected() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload With Password.xls",
            CaseWorkerConstants.TYPE_XLS, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenXlsxFileIsPasswordProtected() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload With Password.xlsx",
            TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenFileHasNoData() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload Xlsx With Only Header.xlsx",
            TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenContentTypeIsInvalid() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload Xlsx With Only Header.xlsx",
            "application/octet-stream", "400", cwdAdmin);
    }

    @Test
    public void shouldReturn403WhenRoleIsInvalid() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload Xlsx With Only Header.xlsx",
            TYPE_XLSX, "403", "invalid");
    }

    @Test
    public void shouldReturn403WhenLdFeatureIsDisabled() throws IOException {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("CaseWorkerRefController.caseWorkerFileUpload",
            "test-flag-1");
        when(featureToggleServiceImpl.isFlagEnabled(anyString(), anyString())).thenReturn(false);
        when(featureToggleServiceImpl.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        uploadCaseWorkerFile("Staff Data Upload With Password.xlsx",
            TYPE_XLSX, "403", cwdAdmin);
    }


    @Test
    public void shouldCreateCaseWorkerAuditSuccess() throws IOException {
        validateAuditCaseWorkerCreate();
    }

    @Test
    public void shouldCreateCaseWorkerAuditPartialSuccess() throws IOException {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        response = uploadCaseWorkerFile("Staff Data Upload With Jsr.xlsx",
            CaseWorkerConstants.TYPE_XLS, "200 OK", cwdAdmin);

        CaseWorkerFileCreationResponse resultResponse =
            objectMapper.readValue(getJsonResponse(response), CaseWorkerFileCreationResponse.class);
        CaseWorkerFileCreationResponse expectedResponse = createCaseWorkerExpectedErrorDetails();

        assertThat(expectedResponse.getDetailedMessage()).isEqualTo(resultResponse.getDetailedMessage());
        assertThat(expectedResponse.getErrorDetails()).containsAll(resultResponse.getErrorDetails());
        assertThat(expectedResponse.getMessage()).isEqualTo(resultResponse.getMessage());

        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(PARTIAL_SUCCESS.getStatus());
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(exceptionCaseWorkers).hasSizeGreaterThanOrEqualTo(1);
    }

    private CaseWorkerFileCreationResponse createCaseWorkerExpectedErrorDetails() {
        LinkedList<JsrFileErrors> errors = new LinkedList<>();
        errors.add(JsrFileErrors.builder().rowId("2").filedInError("locations").errorDescription(
            CaseWorkerConstants.NO_LOCATION_PRESENT).build());
        errors.add(JsrFileErrors.builder().rowId("3").filedInError("roles").errorDescription(
            CaseWorkerConstants.NO_ROLE_PRESENT).build());
        errors.add(JsrFileErrors.builder().rowId("4").filedInError("workAreas").errorDescription(
            CaseWorkerConstants.NO_WORK_AREA_PRESENT).build());
        errors.add(JsrFileErrors.builder().rowId("5").filedInError("userType").errorDescription(
            CaseWorkerConstants.NO_USER_TYPE_PRESENT).build());
        errors.add(JsrFileErrors.builder().rowId("6").filedInError("firstName").errorDescription(
            CaseWorkerConstants.FIRST_NAME_MISSING).build());
        errors.add(JsrFileErrors.builder().rowId("7").filedInError("lastName").errorDescription(
            CaseWorkerConstants.LAST_NAME_MISSING).build());
        errors.add(JsrFileErrors.builder().rowId("8").filedInError("officialEmail").errorDescription(
            CaseWorkerConstants.INVALID_EMAIL).build());
        errors.add(JsrFileErrors.builder().rowId("9").filedInError("regionName").errorDescription(
            CaseWorkerConstants.MISSING_REGION).build());
        errors.add(JsrFileErrors.builder().rowId("9").filedInError("regionId").errorDescription(
            CaseWorkerConstants.MISSING_REGION).build());
        errors.add(JsrFileErrors.builder().rowId("11").filedInError(ROLE_FIELD).errorDescription(
            DUPLICATE_PRIMARY_AND_SECONDARY_ROLES).build());
        errors.add(JsrFileErrors.builder().rowId("10").filedInError(LOCATION_FIELD).errorDescription(
            DUPLICATE_PRIMARY_AND_SECONDARY_LOCATIONS).build());
        errors.add(JsrFileErrors.builder().rowId("12").filedInError(AREA_OF_WORK_FIELD).errorDescription(
            DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK).build());
        return CaseWorkerFileCreationResponse.builder()
            .errorDetails(errors)
            .detailedMessage("11 record(s) failed validation and 1 record(s) uploaded")
            .message("Request completed with partial success."
                + " Some records failed during validation and were ignored.")
            .build();
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
            + "\"error_details\":[{\"row_id\":\"1\","
            + "\"error_description\":\"Failed to create in UP with response status 404\"}]}";

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
    public void shouldUploadServiceRoleMappingsXlsxFileWithJsr() throws IOException {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        String exceptedResponse = "{\"message\":\"Request completed with partial success. "
            + "Some records failed during validation and were ignored.\","
            + "\"message_details\":\"2 record(s) failed validation and 2 record(s) uploaded\","
            + "\"error_details\":[{\"row_id\":\"1\",\"field_in_error\":\"serviceId\",\"error_description\":"
            + "\"must not be empty\"},{\"row_id\":\"2\",\"field_in_error\":\"idamRoles\","
            + "\"error_description\":\"must not be empty\"},"
            + "{\"row_id\":\"2\",\"field_in_error\":\"roleId\",\"error_description\":\"must not be null\"}]}";

        response = uploadCaseWorkerFile("ServiceRoleMapping_BBA9WithJSR.xlsx",
            TYPE_XLSX, "200 OK", cwdAdmin);

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

    @Test
    public void shouldHandlePartialSuccessWhenFileHasBadFormulaRecord() throws IOException {
        Map<String, Object> response = uploadCaseWorkerFile("Staff Data Test incorrect function.xlsx",
                TYPE_XLSX, "200 OK", cwdAdmin);
        assertThat(response.get("message")).isEqualTo(REQUEST_FAILED_FILE_UPLOAD_JSR);
        assertThat(response.get("message_details")).isEqualTo(String.format(RECORDS_FAILED, 4));
        assertThat((List)response.get("error_details")).hasSize(4);
    }
}
