package uk.gov.hmcts.reform.cwrdapi;

import com.google.gson.Gson;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.gov.hmcts.reform.cwrdapi.controllers.advice.ErrorResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.JsrFileErrors;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerAudit;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerIdamRoleAssociation;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.domain.ExceptionCaseWorker;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants;
import uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerReferenceDataClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.lang.String.format;
import static java.time.ZoneId.of;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Map.entry;
import static org.apache.logging.log4j.util.Strings.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.controllers.constants.ErrorConstants.FILE_UPLOAD_IN_PROGRESS;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.AuditStatus.SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.AREA_OF_WORK_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_EMAIL_PROFILES;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_PRIMARY_AND_SECONDARY_LOCATIONS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_PRIMARY_AND_SECONDARY_ROLES;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FIRST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.FIRST_NAME_MISSING;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.INVALID_EMAIL;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LAST_NAME_INVALID;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LAST_NAME_MISSING;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.LOCATION_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.MISSING_REGION;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_PRIMARY_LOCATION_PRESENT;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_ROLE_PRESENT;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_USER_TYPE_PRESENT;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.NO_WORK_AREA_PRESENT;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RECORDS_FAILED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RECORDS_UPLOADED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_FAILED_FILE_UPLOAD_JSR;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.ROLE_FIELD;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLSX;

public class CaseWorkerCreateUserWithFileUploadTest extends FileUploadTest {

    @Autowired
    JdbcTemplate template;

    String exceptedResponse = "{\"message\":\"Request Completed Successfully\","
        + "\"message_details\":\"4 record(s) uploaded\"}";

    @Test
    public void shouldUploadCaseWorkerUsersXlsxFileSuccessfully() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload.xlsx",
            TYPE_XLSX, "200 OK", cwdAdmin);

        var caseWorkerProfile = caseWorkerProfileRepository.findAll();
        TimeZone systemZone = TimeZone.getDefault();
        int totalZoneSecondsFromUtc =
                caseWorkerProfile.get(0).getCreatedDate().atZone(of(systemZone.getID())).getOffset().getTotalSeconds();
        //to check UTC time is persisted in db
        assertThat(caseWorkerProfile.get(0).getCreatedDate())
            .isCloseToUtcNow(within(totalZoneSecondsFromUtc + 10, SECONDS));

        long caseAllocatorCount = caseWorkerProfile.stream()
                .filter(CaseWorkerProfile::getCaseAllocator)
                .count();
        assertEquals(0, caseAllocatorCount);

        long taskSupervisor = caseWorkerProfile.stream()
                .filter(CaseWorkerProfile::getTaskSupervisor)
                .count();

        assertEquals(0, taskSupervisor);

    }

    @Test
    public void shouldUploadCaseWorkerUsersXlsFileSuccessfully() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload Xls.xls",
            TYPE_XLSX, "200 OK", cwdAdmin);

        var caseWorkerProfile = caseWorkerProfileRepository.findAll();
        long caseAllocatorCount = caseWorkerProfile.stream()
                .filter(CaseWorkerProfile::getCaseAllocator)
                .count();
        assertEquals(0, caseAllocatorCount);

        long taskSupervisor = caseWorkerProfile.stream()
                .filter(CaseWorkerProfile::getTaskSupervisor)
                .count();

        assertEquals(0, taskSupervisor);
    }

    @Test
    public void shouldUploadServiceRoleMappingsXlsxFileSuccessfully() throws IOException {

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
    public void shouldUploadServiceRoleMappingsXlsxFileSuccessfully_with_extra_spaces() throws IOException {
        uploadCaseWorkerFile("ServiceRoleMapping_BBA9_extra_spaces.xlsx", TYPE_XLSX, "200 OK", cwdAdmin);

        List<CaseWorkerIdamRoleAssociation> associations = roleAssocRepository.findAll();
        CaseWorkerIdamRoleAssociation association = associations.get(0);
        assertThat(association.getIdamRole()).isEqualTo("caseworker-iac");
        assertThat(association.getServiceCode()).isEqualTo("BBA9");
    }

    @Test
    public void shouldReturn200PartialSuccessWhenNameIsLongerThan128AndNameHasInvalidCharacter() throws IOException {
        Map<String, Object> response = uploadCaseWorkerFile(
            "Staff Data Upload With Name Longer Than 128 and Name With Invalid Character.xlsx",
            CaseWorkerConstants.TYPE_XLS, "200 OK", cwdAdmin);

        assertThat(response).contains(entry("message", REQUEST_FAILED_FILE_UPLOAD_JSR))
            .contains(entry("message_details",
                format("2 record(s) failed validation and 1 record(s) uploaded", 2))).hasSize(6);
        assertThat(response.get("error_details").toString()).contains(FIRST_NAME_INVALID);
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(PARTIAL_SUCCESS.getStatus()).hasSize(15);
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(exceptionCaseWorkers).isNotEmpty();
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
    public void shouldReturn400WhenFileHasAllEmptyRows() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload With All Empty Rows.xlsx",
            TYPE_XLSX, "400", cwdAdmin);
    }

    @Test
    public void shouldReturn400WhenContentTypeIsInvalid() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload Xlsx With Only Header.xlsx",
            "application/octet-stream", "400", cwdAdmin);
    }

    @Test
    public void shouldReturn403WhenRoleIsInvalid() throws IOException {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        uploadCaseWorkerFile("Staff Data Upload Xlsx With Only Header.xlsx",
            TYPE_XLSX, "403", "invalid");
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @Test
    public void shouldReturn403WhenLdFeatureIsDisabled() throws IOException {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("CaseWorkerRefController.caseWorkerFileUpload",
            "test-flag-1");
        when(featureToggleServiceImpl.isFlagEnabled(anyString(), anyString())).thenReturn(false);
        when(featureToggleServiceImpl.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        uploadCaseWorkerFile("Staff Data Upload With Password.xlsx",
            TYPE_XLSX, "403", cwdAdmin);
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }


    @Test
    public void shouldCreateCaseWorkerAuditSuccess() throws IOException {
        validateAuditCaseWorkerCreate();
    }

    @Test
    public void shouldCreateCaseWorkerAuditSuccessWitUpConflict() throws Exception {
        String roles = "[\"Senior Tribunal Caseworker\"]";
        userProfileGetUserWireMock("ACTIVE", roles);
        modifyUserRoles();
        validateAuditCaseWorkerConflict();
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
        errors.add(JsrFileErrors.builder().rowId("3").filedInError(LOCATION_FIELD).errorDescription(
            NO_PRIMARY_LOCATION_PRESENT).build());
        errors.add(JsrFileErrors.builder().rowId("4").filedInError("roles").errorDescription(
            NO_ROLE_PRESENT).build());
        errors.add(JsrFileErrors.builder().rowId("5").filedInError("workAreas").errorDescription(
            NO_WORK_AREA_PRESENT).build());
        errors.add(JsrFileErrors.builder().rowId("6").filedInError("userType").errorDescription(
            NO_USER_TYPE_PRESENT).build());
        errors.add(JsrFileErrors.builder().rowId("7").filedInError("firstName").errorDescription(
            FIRST_NAME_MISSING).build());
        errors.add(JsrFileErrors.builder().rowId("8").filedInError("lastName").errorDescription(
            LAST_NAME_MISSING).build());
        errors.add(JsrFileErrors.builder().rowId("9").filedInError("officialEmail").errorDescription(
            INVALID_EMAIL).build());
        errors.add(JsrFileErrors.builder().rowId("10").filedInError("regionName").errorDescription(
            MISSING_REGION).build());
        errors.add(JsrFileErrors.builder().rowId("12").filedInError(ROLE_FIELD).errorDescription(
            DUPLICATE_PRIMARY_AND_SECONDARY_ROLES).build());
        errors.add(JsrFileErrors.builder().rowId("11").filedInError(LOCATION_FIELD).errorDescription(
            DUPLICATE_PRIMARY_AND_SECONDARY_LOCATIONS).build());
        errors.add(JsrFileErrors.builder().rowId("13").filedInError(AREA_OF_WORK_FIELD).errorDescription(
            DUPLICATE_SERVICE_CODE_IN_AREA_OF_WORK).build());
        errors.add(JsrFileErrors.builder().rowId("14").filedInError(LOCATION_FIELD).errorDescription(
            NO_PRIMARY_LOCATION_PRESENT).build());
        errors.add(JsrFileErrors.builder().rowId("15").filedInError("firstName").errorDescription(
            FIRST_NAME_INVALID).build());
        errors.add(JsrFileErrors.builder().rowId("16").filedInError("lastName").errorDescription(
            LAST_NAME_INVALID).build());
        errors.add(JsrFileErrors.builder().rowId("17").filedInError("officialEmail").errorDescription(
            INVALID_EMAIL).build());

        return CaseWorkerFileCreationResponse.builder()
            .errorDetails(errors)
            .detailedMessage("15 record(s) failed validation and 1 record(s) uploaded")
            .message("Request completed with partial success."
                + " Some records failed during validation and were ignored.")
            .build();
    }

    @Test
    public void shouldCreateCaseWorkerAuditFailureForBadIdamRoles() throws IOException {
        //create invalid stub of UP for Exception validation
        String errorMessageFromIdam = "The role to be assigned does not exist.";
        ErrorResponse errorResponse = ErrorResponse.builder()
            .errorCode(404)
            .errorDescription(errorMessageFromIdam).build();

        userProfileService.resetAll();
        userProfileService.stubFor(post(urlEqualTo("/v1/userprofile"))
            .willReturn(aResponse().withStatus(404)
                .withBody(new Gson().toJson(errorResponse).getBytes())));

        uploadCaseWorkerFile("Staff Data Upload.xlsx",
            TYPE_XLSX, "200 OK", cwdAdmin);
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(PARTIAL_SUCCESS.getStatus());
        assertThat(exceptionCaseWorkers.size()).isEqualTo(1);
        assertNotNull(exceptionCaseWorkers.get(0).getErrorDescription());
        assertEquals(exceptionCaseWorkers.get(0).getErrorDescription(), errorMessageFromIdam);
    }

    @Test
    public void shouldUploadServiceRoleMappingsXlsxFileWithJsr() throws IOException {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        String exceptedResponse = "{\"message\":\"Request completed with partial success. "
            + "Some records failed during validation and were ignored.\","
            + "\"message_details\":\"2 record(s) failed validation and 2 record(s) uploaded\","
            + "\"error_details\":[{\"row_id\":\"2\",\"field_in_error\":\"serviceId\",\"error_description\":"
            + "\"must not be empty\"},{\"row_id\":\"3\",\"field_in_error\":\"idamRoles\","
            + "\"error_description\":\"must not be empty\"},"
            + "{\"row_id\":\"3\",\"field_in_error\":\"roleId\",\"error_description\":\"must not be null\"}]}";

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
    public void shouldFailCaseWorkerUsersXlsxFileUploadIfPreviousUploadInProgress() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "200 OK", cwdAdmin);

        template.execute("update case_worker_audit set status = 'InProgress' "
            + "where job_id = (select max(job_id) from case_worker_audit)");

        response = uploadCaseWorkerFile("Staff Data Upload.xlsx",
            CaseWorkerConstants.TYPE_XLSX, "403", cwdAdmin);

        ErrorResponse resultResponse = (ErrorResponse) getJsonResponse(response, "response_body",
            ErrorResponse.class);

        assertEquals(FILE_UPLOAD_IN_PROGRESS.getErrorMessage(), resultResponse.getErrorDescription());
    }

    @Test
    public void shouldHandlePartialSuccessWhenFileHasBadFormulaRecord() throws IOException {
        Map<String, Object> response = uploadCaseWorkerFile("Staff Data Test incorrect function.xlsx",
            TYPE_XLSX, "200 OK", cwdAdmin);

        assertThat(response).contains(entry("message", REQUEST_FAILED_FILE_UPLOAD_JSR))
            .contains(entry("message_details", format(RECORDS_FAILED, 4))).hasSize(6);
        assertThat((List) response.get("error_details")).hasSize(4);
    }

    @Test
    public void shouldUploadStaffDataXlsxFileSuccessfully_whenEmptyRowsInBetween() throws IOException {
        Map<String, Object> response = uploadCaseWorkerFile("Staff Data Upload With Some Empty Rows.xlsx",
            TYPE_XLSX, "200 OK", cwdAdmin);

        assertThat(response).contains(entry("message", REQUEST_COMPLETED_SUCCESSFULLY))
            .contains(entry("message_details", format(RECORDS_UPLOADED, 2))).hasSize(6);
        assertThat(response.get("error_details")).isNull();
    }

    @Test
    public void shouldUploadStaffDataXlsxFileSuccessfully_whenNoEmptyRowsInBetween() throws IOException {
        Map<String, Object> response = uploadCaseWorkerFile("Staff Data Upload With All Valid Rows.xlsx",
            TYPE_XLSX, "200 OK", cwdAdmin);

        assertThat(response).contains(entry("message", REQUEST_COMPLETED_SUCCESSFULLY))
            .contains(entry("message_details", format(RECORDS_UPLOADED, 2))).hasSize(6);
        assertThat(response.get("error_details")).isNull();
    }

    @Test
    public void shouldCreateCaseWorkerAudit_when_email_in_capital_letters() throws IOException {
        Map<String, Object> response = uploadCaseWorkerFile("Staff Data Upload "
            + "With Case Insensitive Email.xlsx", TYPE_XLSX, "200 OK", cwdAdmin);

        assertThat(response).contains(entry("message", REQUEST_COMPLETED_SUCCESSFULLY))
            .contains(entry("message_details", format(RECORDS_UPLOADED, 1))).hasSize(6);
        assertThat((List) response.get("error_details")).isNull();
        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(SUCCESS.getStatus());
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(exceptionCaseWorkers).isEmpty();
    }

    @Test
    public void shouldHandleDuplicateEmailProfiles() throws IOException {
        Map<String, Object> response =
            uploadCaseWorkerFile("Staff Data Upload With Duplicate Email Profiles.xlsx",
                TYPE_XLSX, "200 OK", cwdAdmin);

        assertThat(response).contains(entry("message", REQUEST_FAILED_FILE_UPLOAD_JSR))
            .contains(entry("message_details", format(RECORDS_FAILED, 2).concat(" and ")
                .concat(format(RECORDS_UPLOADED, 1))))
            .hasSize(6);

        assertThat((List) response.get("error_details")).hasSize(2);
        assertTrue(((List<?>) response.get("error_details")).get(0).toString()
            .contains(format(DUPLICATE_EMAIL_PROFILES, 3)));
        assertTrue(((List<?>) response.get("error_details")).get(1).toString()
            .contains(format(DUPLICATE_EMAIL_PROFILES, 4)));

        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAudits.size()).isEqualTo(1);
        assertThat(caseWorkerAudits.get(0).getStatus()).isEqualTo(PARTIAL_SUCCESS.getStatus());
        List<ExceptionCaseWorker> exceptionCaseWorkers = caseWorkerExceptionRepository.findAll();
        assertThat(exceptionCaseWorkers.size()).isEqualTo(2);
        assertEquals(format(DUPLICATE_EMAIL_PROFILES, 3), exceptionCaseWorkers.get(0).getErrorDescription());
        assertEquals(format(DUPLICATE_EMAIL_PROFILES, 4), exceptionCaseWorkers.get(1).getErrorDescription());
    }

    @Test
    public void shouldFailToCreateAuditForInvalidRole() throws IOException {
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
        uploadCaseWorkerFile("Staff Data Upload.xlsx",
                        TYPE_XLSX, "403", "invalid");

        List<CaseWorkerAudit> caseWorkerAudits = caseWorkerAuditRepository.findAll();
        assertThat(caseWorkerAudits.size()).isZero();
        CaseWorkerReferenceDataClient.setBearerToken(EMPTY);
    }

    @Test
    public void shouldUploadCaseWorkerUsersXlsxFileWithNonIdamRolesSuccessfully() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload with non idam roles.xlsx",
                TYPE_XLSX, "200 OK", cwdAdmin);

        var caseWorkerProfile = caseWorkerProfileRepository.findAll();
        long caseAllocatorCount = caseWorkerProfile.stream()
                .filter(CaseWorkerProfile::getCaseAllocator)
                .count();
        assertEquals(2L, caseAllocatorCount);

        long taskSupervisor = caseWorkerProfile.stream()
                .filter(CaseWorkerProfile::getTaskSupervisor)
                .count();

        assertEquals(2L, taskSupervisor);

    }

    @Test
    public void shouldUploadCaseWorkerUsersXlsFileWithNonIdamRolesSuccessfully() throws IOException {
        uploadCaseWorkerFile("Staff Data Upload with non idam roles.xls",
                TYPE_XLSX, "200 OK", cwdAdmin);

        var caseWorkerProfile = caseWorkerProfileRepository.findAll();

        long caseAllocatorCount = caseWorkerProfile.stream()
                .filter(CaseWorkerProfile::getCaseAllocator)
                .count();
        assertEquals(1L, caseAllocatorCount);

        long taskSupervisor = caseWorkerProfile.stream()
                .filter(CaseWorkerProfile::getTaskSupervisor)
                .count();

        assertEquals(1L, taskSupervisor);
    }

}
