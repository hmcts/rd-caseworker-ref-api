package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.MultiPartSpecification;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.client.domain.StaffProfileWithServiceName;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureConditionEvaluation;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RECORDS_UPLOADED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLSX;
import static uk.gov.hmcts.reform.cwrdapi.util.FeatureToggleConditionExtension.getToggledOffMessage;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@SerenityTest
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CaseWorkerRefFunctionalTest extends AuthorizationFunctionalTest {

    public static final String CREATE_CASEWORKER_PROFILE = "CaseWorkerRefUsersController.createCaseWorkerProfiles";
    public static final String FETCH_BY_CASEWORKER_ID = "CaseWorkerRefUsersController.fetchCaseworkersById";
    public static final String CASEWORKER_FILE_UPLOAD = "CaseWorkerRefController.caseWorkerFileUpload";
    public static final String DELETE_CASEWORKER_BY_ID_OR_EMAILPATTERN =
            "CaseWorkerRefUsersController.deleteCaseWorkerProfileByIdOrEmailPattern";
    public static final String STAFF_BY_SERVICE_NAME_URL = "/refdata/internal/staff/usersByServiceName";
    public static List<String> caseWorkerIds = new ArrayList<>();
    public static final String FETCH_STAFF_BY_CCD_SERVICE_NAMES =
            "StaffReferenceInternalController.fetchStaffByCcdServiceNames";

    @Test
    @ToggleEnable(mapKey = CREATE_CASEWORKER_PROFILE, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    //this test verifies new User profile is created
    public void createCwProfileWhenUserNotExistsInCrdAndSidamAndUp_Ac1() {
        List<CaseWorkerRoleRequest> roleRequests = new ArrayList<CaseWorkerRoleRequest>();
        roleRequests.add(new CaseWorkerRoleRequest("National Business Centre Team Leader",true));
        roleRequests.add(new CaseWorkerRoleRequest("Regional Centre Team Leader",false));
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles();
        caseWorkersProfileCreationRequests.get(0).setRoles(roleRequests);
        Response response = caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse =
                response.getBody().as(CaseWorkerProfileCreationResponse.class);
        List<String> caseWorkerIds = caseWorkerProfileCreationResponse.getCaseWorkerIds();
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(caseWorkerIds).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> fetchedList =
                Arrays.asList(fetchResponse.getBody().as(
                        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile[].class));
        assertEquals(1, fetchedList.size());
        assertEquals("Regional Centre Team Leader", fetchedList.get(0).getRoles().get(1).getRoleName());
        List<String> workAreas = fetchedList.stream().flatMap(fw -> fw.getWorkAreas().stream().map(
                WorkArea::getAreaOfWork)).collect(
                Collectors.toList());
        assertTrue(workAreas.contains(caseWorkersProfileCreationRequests.get(0)
                .getWorkerWorkAreaRequests().get(0).getAreaOfWork()));
        caseWorkersProfileCreationRequests.get(0)
                .getWorkerWorkAreaRequests().forEach(workerWorkAreaRequest ->
                        assertTrue(workAreas.contains(workerWorkAreaRequest.getAreaOfWork())));
        assertEquals(fetchedList.get(0).getFirstName(), caseWorkersProfileCreationRequests.get(0).getFirstName());
        assertEquals(caseWorkersProfileCreationRequests.size(), caseWorkerIds.size());
    }

    @Test
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = CREATE_CASEWORKER_PROFILE, withFeature = false)
    //this test verifies new User profile is created is prohibited when api is toggled off
    public void createCwWhenUserNotExistsInCwrAndSidamAndUp_Ac1_LD_disabled() {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_ADMIN)
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    //this test verifies new User profile is created when user is already present in SIDAM
    public void createCwWhenUserNotExistsInCwrAndUpAndExistsInSidam_Ac2() {
        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        UserProfileResponse upResponse = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        Assertions.assertThat(upResponse.getRoles())
                .containsExactlyInAnyOrderElementsOf(ImmutableList
                        .of(CWD_USER, CASEWORKER_IAC_BULKSCAN, CASEWORKER_SENIOR_IAC));
    }

    @Test
    public void updateNamesMismatchinUpCwandSidam() {
        var profileCreateRequests = createNewActiveCaseWorkerProfile();
        var upResponse = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        var caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles(upResponse.getEmail());
        caseWorkersProfileCreationRequests.get(0).setFirstName("cwr-test-one");
        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);
        UserProfileResponse upResponseUpdated =
                getUserProfileFromUp(caseWorkersProfileCreationRequests.get(0).getEmailId());
        assertEquals("cwr-test-one",upResponseUpdated.getFirstName());
    }

    @Test
    public void updateCwWhenNamesMismatchExistsInCwrAndUpAndSidam_Ac3() {
        var profileCreateRequests = createNewActiveCaseWorkerProfile();
        var upResponse = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        var caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles(upResponse.getEmail());
        caseWorkersProfileCreationRequests.get(0).setFirstName("cwr-test-one");
        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);
        var idamResponse = getIdamResponse(upResponse.getIdamId());
        assertEquals(idamResponse.get("forename"),caseWorkersProfileCreationRequests.get(0).getFirstName());
    }

    @Test
    // this test verifies User profile are fetched from CWR
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldGetCaseWorkerDetails() {
        if (isEmpty(caseWorkerIds)) {
            //Create 2 Caseworker Users
            List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>();

            caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                    .createCaseWorkerProfiles());
            caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                    .createCaseWorkerProfiles());

            Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_ADMIN)
                    .body(caseWorkersProfileCreationRequests)
                    .post("/refdata/case-worker/users")
                    .andReturn();
            response.then()
                    .assertThat()
                    .statusCode(201);

            CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse =
                    response.getBody().as(CaseWorkerProfileCreationResponse.class);
            caseWorkerIds = caseWorkerProfileCreationResponse.getCaseWorkerIds();
            assertEquals(caseWorkersProfileCreationRequests.size(), caseWorkerIds.size());
        }
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(caseWorkerIds).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> fetchedList =
                Arrays.asList(fetchResponse.getBody().as(
                        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile[].class));
        assertEquals(caseWorkerIds.size(), fetchedList.size());
        fetchedList.forEach(caseWorkerProfile ->
                assertTrue(caseWorkerIds.contains(caseWorkerProfile.getId())));
    }

    @Test
    // this test verifies User profile are not fetched from CWR when toggled off
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = false)
    public void should_retrieve_403_when_Api_toggled_off() {

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>(
                caseWorkerApiClient.createCaseWorkerProfiles());
        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/fetchUsersById")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    // this test verifies User profile are fetched from CWR when id matched what given in request rest should be ignored
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldGetOnlyFewCaseWorkerDetails() {
        if (isEmpty(caseWorkerIds)) {
            createCaseWorkerIds();
        }
        List<String> tempCwIds = new ArrayList<>(caseWorkerIds);
        tempCwIds.add("randomId");
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(tempCwIds).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> fetchedList =
                Arrays.asList(fetchResponse.getBody().as(
                        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile[].class));
        assertEquals(1, fetchedList.size());
        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile caseWorkerProfile =
                fetchedList.get(0);

        assertTrue(caseWorkerIds.contains(caseWorkerProfile.getId()));
        assertTrue(StringUtils.isNotEmpty(caseWorkerProfile.getFirstName()));
        assertTrue(StringUtils.isNotEmpty(caseWorkerProfile.getLastName()));
        assertTrue(StringUtils.isNotEmpty(caseWorkerProfile.getOfficialEmail()));

        assertTrue(CollectionUtils.isNotEmpty(caseWorkerProfile.getLocations()));
        assertTrue(CollectionUtils.isNotEmpty(caseWorkerProfile.getRoles()));
        assertTrue(CollectionUtils.isNotEmpty(caseWorkerProfile.getWorkAreas()));

    }

    @Test
    // this test verifies User profile are not fetched from CWR when user is invalid
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldThrowForbiddenExceptionForNonCompliantRole() {
        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("prd-admin")
                .body(UserRequest.builder().userIds(Collections.singletonList("someUUID")).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(403);

    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldUploadXlsxFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload.xlsx",
                        200, REQUEST_COMPLETED_SUCCESSFULLY, TYPE_XLSX, ROLE_CWD_ADMIN);

        CaseWorkerFileCreationResponse caseWorkerFileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerFileCreationResponse.getMessage().contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerFileCreationResponse.getDetailedMessage().contains(format(RECORDS_UPLOADED, 6)));
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldUploadXlsFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload.xls",
                        200, REQUEST_COMPLETED_SUCCESSFULLY, TYPE_XLS,
                        ROLE_CWD_ADMIN);

        CaseWorkerFileCreationResponse caseWorkerFileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerFileCreationResponse.getMessage()
                .contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerFileCreationResponse.getDetailedMessage()
                .contains(format(RECORDS_UPLOADED, 3)));
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    @Order(1)
    public void shouldUploadServiceRoleMappingXlsxFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/ServiceRoleMapping_BBA9.xlsx",
                        200, IDAM_ROLE_MAPPINGS_SUCCESS, TYPE_XLS,
                        ROLE_CWD_ADMIN);


        CaseWorkerFileCreationResponse caseWorkerFileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerFileCreationResponse.getMessage()
                .contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerFileCreationResponse.getDetailedMessage()
                .contains(format(RECORDS_UPLOADED, 4)));
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    @Order(2)
    public void shouldUploadServiceRoleMappingXlsFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/ServiceRoleMapping_BBA9.xls",
                        200, IDAM_ROLE_MAPPINGS_SUCCESS, TYPE_XLS,
                        ROLE_CWD_ADMIN);

        CaseWorkerFileCreationResponse caseWorkerProfileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerProfileCreationResponse.getMessage()
                .contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerProfileCreationResponse.getDetailedMessage()
                .contains(format(RECORDS_UPLOADED, 4)));
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    @Order(3)
    public void shouldUploadServiceRoleMappingAba1XlsFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/ServiceRoleMapping_ABA1.xls",
                        200, IDAM_ROLE_MAPPINGS_SUCCESS, TYPE_XLS,
                        ROLE_CWD_ADMIN);

        CaseWorkerFileCreationResponse caseWorkerProfileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerProfileCreationResponse.getMessage()
                .contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerProfileCreationResponse.getDetailedMessage()
                .contains(format(RECORDS_UPLOADED, 4)));
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldReturn401WhenAuthenticationInvalid() {
        Response response = caseWorkerApiClient.withUnauthenticatedRequest()
                .post("/refdata/case-worker/upload-file/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(401);
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldReturn403WhenRoleIsInvalid() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload.xlsx",
                403, null,
                TYPE_XLSX, "Invalid");
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldReturn403WhenUploadFileApiToggledOff() throws IOException {

        String exceptionMessage = FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD;

        uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload.xlsx",
                403, exceptionMessage,
                TYPE_XLSX, ROLE_CWD_ADMIN);
    }

    @Test
    @ToggleEnable(mapKey = DELETE_CASEWORKER_BY_ID_OR_EMAILPATTERN, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    //this test verifies that a User Profile is deleted by ID
    public void deleteCaseworkerById() {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests =
                caseWorkerApiClient.createCaseWorkerProfiles();

        // create user
        Response createResponse = caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse =
                createResponse.getBody().as(CaseWorkerProfileCreationResponse.class);

        List<String> caseWorkerIds = caseWorkerProfileCreationResponse.getCaseWorkerIds();
        assertEquals(caseWorkersProfileCreationRequests.size(), caseWorkerIds.size());

        //delete user
        caseWorkerApiClient.deleteCaseworkerByIdOrEmailPattern(
                "/refdata/case-worker/users?userId=" + caseWorkerIds.get(0), NO_CONTENT);

        //search for deleted user
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(caseWorkerIds).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();

        //assert that delete user is not found
        assertThat(fetchResponse.getStatusCode()).isEqualTo(404);
    }

    @Test
    @ToggleEnable(mapKey = DELETE_CASEWORKER_BY_ID_OR_EMAILPATTERN, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    //this test verifies that a User Profile is deleted by Email Pattern
    public void deleteCaseworkerByEmailPattern() {
        String emailPattern = "deleteTest1234";
        String email = format(EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests =
                caseWorkerApiClient.createCaseWorkerProfiles(email);

        // create user with email pattern
        Response createResponse = caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse =
                createResponse.getBody().as(CaseWorkerProfileCreationResponse.class);

        List<String> caseWorkerIds = caseWorkerProfileCreationResponse.getCaseWorkerIds();
        assertEquals(caseWorkersProfileCreationRequests.size(), caseWorkerIds.size());

        //delete user by email pattern
        caseWorkerApiClient.deleteCaseworkerByIdOrEmailPattern(
                "/refdata/case-worker/users?emailPattern=" + emailPattern, NO_CONTENT);

        //search for deleted user
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(UserRequest.builder().userIds(caseWorkerIds).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();

        //assert that delete user is not found
        assertThat(fetchResponse.getStatusCode()).isEqualTo(404);
    }

    @Test
    @ToggleEnable(mapKey = DELETE_CASEWORKER_BY_ID_OR_EMAILPATTERN, withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void deleteCaseworkerReturns403WhenToggledOff() {
        caseWorkerApiClient.deleteCaseworkerByIdOrEmailPattern(
                "/refdata/case-worker/users?emailPattern=ForbiddenException", FORBIDDEN);
    }

    @Test
    @ToggleEnable(mapKey = FETCH_STAFF_BY_CCD_SERVICE_NAMES, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldFetchStaffProfileByCcdServiceNamesWithDefaultParams() {
        if (isEmpty(caseWorkerIds)) {
            createCaseWorkerIds();
        }
        Set<String> expectedServiceNames = Set.of("divorce");
        String ccdServiceNames = String.join(",", expectedServiceNames);
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersWithoutContentType(ROLE_CWD_SYSTEM_USER)
                .get(STAFF_BY_SERVICE_NAME_URL
                        + "?ccd_service_names=" + ccdServiceNames)
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);
        List<StaffProfileWithServiceName> paginatedStaffProfile =
                Arrays.asList(fetchResponse.getBody().as(StaffProfileWithServiceName[].class));

        assertFalse(paginatedStaffProfile.isEmpty());
        Set<String> actualServiceNames = new HashSet<>();
        paginatedStaffProfile
                .forEach(p -> actualServiceNames.add(p.getCcdServiceName().toLowerCase()));

        assertTrue(actualServiceNames.containsAll(expectedServiceNames));
    }

    @Test
    @ToggleEnable(mapKey = FETCH_STAFF_BY_CCD_SERVICE_NAMES, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldFetchStaffProfileByCcdServiceNamesWithPaginatedParams() {
        if (isEmpty(caseWorkerIds)) {
            createCaseWorkerIds();
        }
        Set<String> expectedServiceNames = Set.of("divorce");
        String ccdServiceNames = String.join(",", expectedServiceNames);
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersWithoutContentType(ROLE_CWD_SYSTEM_USER)
                .get(STAFF_BY_SERVICE_NAME_URL
                        + "?ccd_service_names=" + ccdServiceNames
                        + "&page_number=0&page_size=2&sort_column=caseWorkerId&sort_direction=ASC")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);
        List<StaffProfileWithServiceName> paginatedStaffProfile =
                Arrays.asList(fetchResponse.getBody().as(StaffProfileWithServiceName[].class));

        assertFalse(paginatedStaffProfile.isEmpty());
        Set<String> actualServiceNames = new HashSet<>();
        paginatedStaffProfile
                .forEach(p -> actualServiceNames.add(p.getCcdServiceName().toLowerCase()));

        assertTrue(actualServiceNames.containsAll(expectedServiceNames));
    }

    @Test
    @ToggleEnable(mapKey = FETCH_STAFF_BY_CCD_SERVICE_NAMES, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldFetchStaffProfileByCcdServiceNamesInDesc() {
        if (isEmpty(caseWorkerIds)) {
            List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>();

            caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                    .createCaseWorkerProfiles());
            caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                    .createCaseWorkerProfiles());
            caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);
        }
        String ccdServiceNames = "all";
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersWithoutContentType(ROLE_CWD_SYSTEM_USER)
                .get(STAFF_BY_SERVICE_NAME_URL
                        + "?ccd_service_names=" + ccdServiceNames
                        + "&page_number=0&page_size=40&sort_column=caseWorkerId&sort_direction=DESC")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);
        List<StaffProfileWithServiceName> paginatedStaffProfile =
                Arrays.asList(fetchResponse.getBody().as(StaffProfileWithServiceName[].class));

        List<String> caseWorkerIds = paginatedStaffProfile.stream()
                .map(ps -> ps.getStaffProfile().getId())
                .distinct().collect(Collectors.toList());
        assertTrue(Ordering.natural().reverse().isOrdered(caseWorkerIds));

    }


    @Test
    @ToggleEnable(mapKey = FETCH_STAFF_BY_CCD_SERVICE_NAMES, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldThrowRecordNotFoundErrorOnInvalidCcdServiceName() {
        if (isEmpty(caseWorkerIds)) {
            List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>();

            CaseWorkerWorkAreaRequest workerWorkAreaRequest1 = CaseWorkerWorkAreaRequest
                    .caseWorkerWorkAreaRequest()
                    .serviceCode("ABA4").areaOfWork("Adoption").build();

            CaseWorkerWorkAreaRequest workerWorkAreaRequest2 = CaseWorkerWorkAreaRequest
                    .caseWorkerWorkAreaRequest()
                    .serviceCode("BHA3").areaOfWork("Specified Money Claims").build();

            List<CaseWorkerWorkAreaRequest> areaRequests =
                    ImmutableList.of(workerWorkAreaRequest1, workerWorkAreaRequest2);

            caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                    .createCaseWorkerProfiles());
            caseWorkersProfileCreationRequests.get(0).setWorkerWorkAreaRequests(areaRequests);
            caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                    .createCaseWorkerProfiles());
            caseWorkersProfileCreationRequests.get(1).setWorkerWorkAreaRequests(areaRequests);
            caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);
        }
        String ccdServiceNames = "Invalid";
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersWithoutContentType(ROLE_CWD_SYSTEM_USER)
                .get(STAFF_BY_SERVICE_NAME_URL
                        + "?ccd_service_names=" + ccdServiceNames
                        + "&page_number=1&page_size=100&sort_column=caseWorkerId&sort_direction=DESC")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(404);


    }

    @Test
    @ToggleEnable(mapKey = FETCH_STAFF_BY_CCD_SERVICE_NAMES, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldThrowForbiddenExceptionForNonCompliantRoleWhileFetchingStaffByCcdServiceNames() {
        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .get(STAFF_BY_SERVICE_NAME_URL
                        + "?ccd_service_names=cmc")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(403);

    }

    @Test
    @ToggleEnable(mapKey = FETCH_STAFF_BY_CCD_SERVICE_NAMES, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldReturn404WhenNoCcdServiceNameFound() {
        if (isEmpty(caseWorkerIds)) {
            createCaseWorkerIds();
        }
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersWithoutContentType(ROLE_CWD_SYSTEM_USER)
                .get(STAFF_BY_SERVICE_NAME_URL
                        + "?ccd_service_names=invalid")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(404);
    }

    @Test
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = FETCH_STAFF_BY_CCD_SERVICE_NAMES, withFeature = false)
    public void shouldReturn403WhenFetchStaffProfileByCcdServiceNamesApiToggledOff() {

        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersWithoutContentType(ROLE_CWD_SYSTEM_USER)
                .get(STAFF_BY_SERVICE_NAME_URL
                        + "?ccd_service_names=cmc")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(403);

        assertThat(fetchResponse.getBody().asString()).contains(getToggledOffMessage());

    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldUploadXlsxFileWithCaseAllocatorAndTaskSupervisorRolesSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload with non idam roles.xlsx",
                        200, REQUEST_COMPLETED_SUCCESSFULLY, TYPE_XLSX, ROLE_CWD_ADMIN);

        CaseWorkerFileCreationResponse caseWorkerFileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerFileCreationResponse.getMessage().contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerFileCreationResponse.getDetailedMessage().contains(format(RECORDS_UPLOADED, 4)));
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
    @ExtendWith(FeatureToggleConditionExtension.class)
    public void shouldUploadXlsFileWithCaseAllocatorAndTaskSupervisorRolesSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/Staff_Data_Upload_with_non_idam_roles.xls",
                        200, REQUEST_COMPLETED_SUCCESSFULLY, TYPE_XLS,
                        ROLE_CWD_ADMIN);

        CaseWorkerFileCreationResponse caseWorkerFileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerFileCreationResponse.getMessage()
                .contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerFileCreationResponse.getDetailedMessage()
                .contains(format(RECORDS_UPLOADED, 3)));
    }

    private ExtractableResponse<Response> uploadCaseWorkerFile(String filePath,
                                                               int statusCode,
                                                               String messageBody,
                                                               String header,
                                                               String role) throws IOException {
        MultiPartSpecification multiPartSpec = getMultipartFile(filePath, header);

        Response response = caseWorkerApiClient.getMultiPartWithAuthHeaders(role)
                .multiPart(multiPartSpec)
                .post("/refdata/case-worker/upload-file")
                .andReturn();
        response.then().log().all()
                .assertThat()
                .statusCode(statusCode);

        return response.then()
                .extract();
    }


    private MultiPartSpecification getMultipartFile(String filePath,
                                                    String headerValue) throws IOException {
        File file = getFile(filePath);
        FileInputStream input = new FileInputStream(file);
        MultiPartSpecBuilder multiPartSpecBuilder = new MultiPartSpecBuilder(IOUtils.toByteArray(input))
                .fileName(file.getName())
                .header("Content-Type",
                        headerValue);
        return multiPartSpecBuilder.build();
    }

    private void createCaseWorkerIds() {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests =
                caseWorkerApiClient.createCaseWorkerProfiles();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_ADMIN)
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(201);

        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse =
                response.getBody().as(CaseWorkerProfileCreationResponse.class);
        caseWorkerIds = caseWorkerProfileCreationResponse.getCaseWorkerIds();
    }
}