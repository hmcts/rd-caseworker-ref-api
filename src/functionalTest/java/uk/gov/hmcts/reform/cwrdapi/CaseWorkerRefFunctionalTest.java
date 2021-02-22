package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.MultiPartSpecification;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Location;
import uk.gov.hmcts.reform.cwrdapi.client.domain.Role;
import uk.gov.hmcts.reform.cwrdapi.client.domain.WorkArea;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureConditionEvaluation;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RECORDS_UPLOADED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLSX;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@RunWith(CustomSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
@TestPropertySource(properties = {"spring.config.location=classpath:application-functional.yml"})
@SuppressWarnings("unchecked")
public class CaseWorkerRefFunctionalTest extends AuthorizationFunctionalTest {

    public static final String CREATE_CASEWORKER_PROFILE = "CaseWorkerRefUsersController.createCaseWorkerProfiles";
    public static final String FETCH_BY_CASEWORKER_ID = "CaseWorkerRefUsersController.fetchCaseworkersById";
    public static List<String> caseWorkerIds = new ArrayList<>();
    public static final String CASEWORKER_FILE_UPLOAD = "CaseWorkerRefController.caseWorkerFileUpload";

    @Test
    @ToggleEnable(mapKey = CREATE_CASEWORKER_PROFILE, withFeature = true)
    //this test verifies new User profile is created
    public void createCwProfileWhenUserNotExistsInCrdAndSidamAndUp_Ac1() {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles();

        Response response = caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse =
                response.getBody().as(CaseWorkerProfileCreationResponse.class);
        List<String> caseWorkerIds = caseWorkerProfileCreationResponse.getCaseWorkerIds();
        assertEquals(caseWorkersProfileCreationRequests.size(), caseWorkerIds.size());
    }

    @Test
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
        assertThat(response.getBody().asString()).contains(CustomSerenityRunner.getFeatureFlagName().concat(" ")
                .concat(FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD));
    }

    @Test
    //this test verifies new User profile is created when user is already present in SIDAM
    public void createCwWhenUserNotExistsInCwrAndUpAndExistsInSidam_Ac2() {
        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        UserProfileResponse upResponse = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        Assertions.assertThat(upResponse.getRoles())
                .containsExactlyInAnyOrderElementsOf(ImmutableList.of(CWD_USER, CASEWORKER_IAC_BULKSCAN));
    }

    @Test
    @Ignore
    //this test verifies User profile is updated when user is already present in CW, UP and SIDAM
    public void createCwWhenUserExistsInCwrAndUpAndExistsInSidam_Ac3() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        String email = profileCreateRequests.get(0).getEmailId();
        CaseWorkersProfileCreationRequest updatedReq = caseWorkerApiClient.updateCaseWorkerProfileRequest(email).get(0);
        Set<String> idamRole = updatedReq.getIdamRoles();
        idamRole.add(CASEWORKER_IAC);
        idamRole.add(CASEWORKER_IAC_BULKSCAN);
        updatedReq.setIdamRoles(idamRole);

        caseWorkerApiClient.createUserProfiles(Collections.singletonList(updatedReq));

        UserProfileResponse upResponse = getUserProfileFromUp(email);
        assertEquals(ImmutableList.of(CASEWORKER_IAC,CWD_USER,CASEWORKER_IAC_BULKSCAN), upResponse.getRoles());

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> cwProfiles = getUserProfilesFromCw(
                UserRequest.builder().userIds(asList(upResponse.getIdamId())).build(), 200);
        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile cwProfile = cwProfiles.get(0);
        assertThat(cwProfile.getId()).isEqualTo(upResponse.getIdamId());
        assertThat(cwProfile.getFirstName()).isEqualTo(updatedReq.getFirstName());
        assertThat(cwProfile.getLastName()).isEqualTo(updatedReq.getLastName());
        assertThat(cwProfile.getOfficialEmail()).isEqualTo(updatedReq.getEmailId());
        assertThat(cwProfile.getRegionId()).isEqualTo(updatedReq.getRegionId());
        assertThat(cwProfile.getRegionName()).isEqualTo(updatedReq.getRegion());
        assertThat(cwProfile.getUserId()).isEqualTo(2);
        assertThat(cwProfile.getUserType()).isEqualTo(updatedReq.getUserType());
        assertThat(cwProfile.getSuspended()).isEqualTo(String.valueOf(updatedReq.isSuspended()));
        assertThat(cwProfile.getLocations()).hasSize(2);
        for (CaseWorkerLocationRequest locationRequest : updatedReq.getBaseLocations()) {
            List<Location> responseLocation = cwProfile.getLocations().stream().filter(fit ->
                    locationRequest.getLocationId().equals(fit.getBaseLocationId())).collect(Collectors.toList());
            assertThat(responseLocation).isNotEmpty().hasSize(1);
            assertThat(responseLocation.get(0).getLocationName()).isEqualTo(locationRequest.getLocation());
            assertThat(responseLocation.get(0).isPrimary()).isEqualTo(locationRequest.isPrimaryFlag());
        }
        assertThat(cwProfile.getWorkAreas()).hasSize(2);
        for (CaseWorkerWorkAreaRequest workAreaRequest : updatedReq.getWorkerWorkAreaRequests()) {
            List<WorkArea> responseWorkAreas = cwProfile.getWorkAreas().stream().filter(fit ->
                    workAreaRequest.getServiceCode().equals(fit.getServiceCode())).collect(Collectors.toList());
            assertThat(responseWorkAreas).isNotEmpty().hasSize(1);
            assertThat(responseWorkAreas.get(0).getAreaOfWork()).isEqualTo(workAreaRequest.getAreaOfWork());
        }
        assertThat(cwProfile.getRoles()).hasSize(2);
        for (CaseWorkerRoleRequest roleRequest : updatedReq.getRoles()) {
            List<Role> responseRoles = cwProfile.getRoles().stream().filter(fit ->
                    roleRequest.getRole().equals(fit.getRoleName())).collect(Collectors.toList());
            assertThat(responseRoles).isNotEmpty().hasSize(1);
            assertThat(responseRoles.get(0).getRoleId()).isNotNull();
            assertThat(responseRoles.get(0).isPrimary()).isEqualTo(roleRequest.isPrimaryFlag());
        }
    }

    @Test
    @Ignore
    // this test verifies User profile is updated when user is already present in CW, UP , SIDAM and delete
    // flag is sent is request then user should be suspended in UP and SIDAM
    public void createCwWhenUserExistsInCwrAndUpAndExistsInSidamAndDeleteFlagTrue_Ac4() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        profileCreateRequests.get(0).setSuspended(true);
        caseWorkerApiClient.createUserProfiles(profileCreateRequests);

        UserProfileResponse upResponseForExistingUser = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(USER_STATUS_SUSPENDED, upResponseForExistingUser.getIdamStatus());

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> cwProfiles = getUserProfilesFromCw(
                UserRequest.builder().userIds(asList(upResponseForExistingUser.getIdamId())).build(), 200);

        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile cwProfile = cwProfiles.get(0);
        assertThat(cwProfile.getSuspended()).isEqualTo("true");
    }

    @Test
    @Ignore
    // this test verifies User profile is updated when user is already present in CW, UP , SIDAM and roles are same as
    // SIDAM, then just update user in CWR
    public void createCwWhenUserExistsInCwrAndUpAndExistsInSidamAndRolesAreSame_Ac5() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();

        caseWorkerApiClient.createUserProfiles(profileCreateRequests);

        UserProfileResponse upResponseForExistingUser = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(ImmutableList.of(CWD_USER, CASEWORKER_IAC_BULKSCAN), upResponseForExistingUser.getRoles());
    }

    @Test
    // this test verifies User profile are fetched from CWR
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = true)
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
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = false)
    public void should_retrieve_403_when_Api_toggled_off() {

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>(
                caseWorkerApiClient.createCaseWorkerProfiles());
        String exceptionMessage = CustomSerenityRunner.getFeatureFlagName().concat(" ")
                .concat(FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD);
        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/fetchUsersById")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(exceptionMessage);
    }

    @Test
    // this test verifies User profile are fetched from CWR when id matched what given in request rest should be ignored
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = true)
    public void shouldGetOnlyFewCaseWorkerDetails() {
        if (isEmpty(caseWorkerIds)) {
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
    public void shouldUploadXlsxFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload.xlsx",
                        200, REQUEST_COMPLETED_SUCCESSFULLY, TYPE_XLSX, ROLE_CWD_ADMIN);

        CaseWorkerFileCreationResponse caseWorkerFileCreationResponse = uploadCaseWorkerFileResponse
            .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerFileCreationResponse.getMessage().contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerFileCreationResponse.getDetailedMessage().contains(format(RECORDS_UPLOADED, 4)));
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
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
            .contains(format(RECORDS_UPLOADED,4)));
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = true)
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
    public void shouldReturn403WhenRoleIsInvalid() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload.xlsx",
                403, null,
                TYPE_XLSX, "Invalid");
    }

    @Test
    @ToggleEnable(mapKey = CASEWORKER_FILE_UPLOAD, withFeature = false)
    public void shouldReturn403WhenUploadFileApiToggledOff() throws IOException {

        String exceptionMessage = CustomSerenityRunner.getFeatureFlagName().concat(" ")
                .concat(FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD);

        uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload.xlsx",
                403, exceptionMessage,
                TYPE_XLSX, ROLE_CWD_ADMIN);
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
        MultiPartSpecBuilder multiPartSpecBuilder =  new MultiPartSpecBuilder(IOUtils.toByteArray(input))
                .fileName(file.getName())
                .header("Content-Type",
                        headerValue);
        return multiPartSpecBuilder.build();
    }
}