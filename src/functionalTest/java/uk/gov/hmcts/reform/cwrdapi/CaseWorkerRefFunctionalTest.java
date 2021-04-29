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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerFileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.ResourceUtils.getFile;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.IDAM_ROLE_MAPPINGS_SUCCESS;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.RECORDS_UPLOADED;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.REQUEST_COMPLETED_SUCCESSFULLY;
import static uk.gov.hmcts.reform.cwrdapi.util.CaseWorkerConstants.TYPE_XLSX;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@RunWith(CustomSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
@TestPropertySource(properties = {"spring.config.location=classpath:application-functional.yml"})
public class CaseWorkerRefFunctionalTest extends AuthorizationFunctionalTest {

    public static final String FETCH_BY_CASEWORKER_ID = "CaseWorkerRefUsersController.fetchCaseworkersById";
    public static List<String> caseWorkerIds = new ArrayList<>();

    @Test
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
    //this test verifies new User profile is created when user is already present in SIDAM
    public void createCwWhenUserNotExistsInCwrAndUpAndExistsInSidam_Ac2() {
        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        UserProfileResponse upResponse = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        Assertions.assertThat(upResponse.getRoles())
                .containsExactlyInAnyOrderElementsOf(ImmutableList.of(CWD_USER, CASEWORKER_IAC_BULKSCAN));
    }

    @Test
    // this test verifies User profile are fetched from CWR
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
    public void shouldUploadXlsxFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload.xlsx",
                        200, REQUEST_COMPLETED_SUCCESSFULLY, ROLE_CWD_ADMIN);

        CaseWorkerFileCreationResponse caseWorkerFileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerFileCreationResponse.getMessage().contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerFileCreationResponse.getDetailedMessage().contains(format(RECORDS_UPLOADED, 4)));
    }

    @Test
    public void shouldUploadXlsFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload.xls",
                        200, REQUEST_COMPLETED_SUCCESSFULLY, ROLE_CWD_ADMIN);

        CaseWorkerFileCreationResponse caseWorkerFileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerFileCreationResponse.getMessage()
                .contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerFileCreationResponse.getDetailedMessage()
                .contains(format(RECORDS_UPLOADED, 3)));
    }

    @Test
    public void shouldUploadServiceRoleMappingXlsxFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/ServiceRoleMapping_BBA9.xlsx",
                        200, IDAM_ROLE_MAPPINGS_SUCCESS, ROLE_CWD_ADMIN);


        CaseWorkerFileCreationResponse caseWorkerFileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerFileCreationResponse.getMessage()
                .contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerFileCreationResponse.getDetailedMessage()
                .contains(format(RECORDS_UPLOADED, 4)));
    }

    @Test
    public void shouldUploadServiceRoleMappingXlsFileSuccessfully() throws IOException {
        ExtractableResponse<Response> uploadCaseWorkerFileResponse =
                uploadCaseWorkerFile("src/functionalTest/resources/ServiceRoleMapping_BBA9.xls",
                        200, IDAM_ROLE_MAPPINGS_SUCCESS, ROLE_CWD_ADMIN);

        CaseWorkerFileCreationResponse caseWorkerProfileCreationResponse = uploadCaseWorkerFileResponse
                .as(CaseWorkerFileCreationResponse.class);
        assertTrue(caseWorkerProfileCreationResponse.getMessage()
                .contains(REQUEST_COMPLETED_SUCCESSFULLY));
        assertTrue(caseWorkerProfileCreationResponse.getDetailedMessage()
                .contains(format(RECORDS_UPLOADED, 4)));
    }

    @Test
    public void shouldReturn401WhenAuthenticationInvalid() {
        Response response = caseWorkerApiClient.withUnauthenticatedRequest()
                .post("/refdata/case-worker/upload-file/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(401);
    }

    @Test
    public void shouldReturn403WhenRoleIsInvalid() throws IOException {
        uploadCaseWorkerFile("src/functionalTest/resources/Staff Data Upload.xlsx",
                403,
                TYPE_XLSX, "Invalid");
    }

    private ExtractableResponse<Response> uploadCaseWorkerFile(String filePath,
                                                               int statusCode,
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
}