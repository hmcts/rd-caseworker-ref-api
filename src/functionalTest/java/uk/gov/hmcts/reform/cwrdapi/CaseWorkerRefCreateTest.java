package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.cwrdapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
@TestPropertySource(properties = {"spring.config.location=classpath:application-functional.yml"})
public class CaseWorkerRefCreateTest extends AuthorizationFunctionalTest {

    @Autowired
    public FuncTestRequestHandler testRequestHandler;

    @Value("${userProfUrl}")
    protected String userProfUrl;

    @Test
    public void whenUserNotExistsInCwrAndSidamAndUp_Ac1() {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles();

        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);
    }

    @Test
    public void whenUserNotExistsInCwrAndUpAndExistsInSidam_Ac2() {
        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        UserProfileResponse upResponse = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(ImmutableList.of(CWD_USER, CASEWORKER_IAC_BULKSCAN), upResponse.getRoles());
    }

    @Test
    public void whenUserExistsInCwrAndUpAndExistsInSidam_Ac3() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();

        CaseWorkersProfileCreationRequest request = profileCreateRequests.get(0);
        Set<String> idamRole = request.getIdamRoles();
        idamRole.add(CASEWORKER_IAC);
        idamRole.add(CASEWORKER_IAC_BULKSCAN);
        request.setIdamRoles(idamRole);

        caseWorkerApiClient.createUserProfiles(profileCreateRequests);

        UserProfileResponse upResponseForExistingUser = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(ImmutableList.of(CASEWORKER_IAC,CWD_USER,CASEWORKER_IAC_BULKSCAN),
                upResponseForExistingUser.getRoles());
    }

    @Test
    public void whenUserExistsInCwrAndUpAndExistsInSidamAndDeleteFlagTrue_Ac4() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();
        profileCreateRequests.get(0).setDeleteFlag(true);
        caseWorkerApiClient.createUserProfiles(profileCreateRequests);

        UserProfileResponse upResponseForExistingUser = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(upResponseForExistingUser.getIdamStatus(), USER_STATUS_SUSPENDED);
    }

    @Test
    public void whenUserExistsInCwrAndUpAndExistsInSidamAndRolesAreSame_Ac5() {

        List<CaseWorkersProfileCreationRequest> profileCreateRequests = createNewActiveCaseWorkerProfile();

        caseWorkerApiClient.createUserProfiles(profileCreateRequests);

        UserProfileResponse upResponseForExistingUser = getUserProfileFromUp(profileCreateRequests.get(0).getEmailId());
        assertEquals(ImmutableList.of(CWD_USER, CASEWORKER_IAC_BULKSCAN), upResponseForExistingUser.getRoles());
    }

    public List<CaseWorkersProfileCreationRequest> createNewActiveCaseWorkerProfile() {
        Map<String, String> userDetail = idamOpenIdClient.createUser(CASEWORKER_IAC_BULKSCAN);
        String userEmail = userDetail.get(EMAIL);

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles(userEmail);

        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        return caseWorkersProfileCreationRequests;
    }

    public UserProfileResponse getUserProfileFromUp(String email) {
        return funcTestRequestHandler.sendGet(OK,
                "/v1/userprofile/roles?email=" + email, UserProfileResponse.class, baseUrlUserProfile);
    }

    @Test
    public void shouldGetCaseWorkerDetails() throws Exception {
        //Create 2 Caseworker Users
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>();

        caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                .createCaseWorkerProfiles());
        caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                .createCaseWorkerProfiles());

        List<String> caseWorkerIds = new ArrayList<>();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(201);

        for (CaseWorkersProfileCreationRequest request : caseWorkersProfileCreationRequests) {
            UserProfileResponse resource =
                    testRequestHandler.sendGet(HttpStatus.OK,
                            "?email=" + request.getEmailId(),
                            UserProfileResponse.class, userProfUrl + "/v1/userprofile"
                    );

            caseWorkerIds.add(resource.getIdamId());

        }

        assertEquals(2, caseWorkerIds.size());
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .body(caseWorkerIds)
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> fetchedList =
                Arrays.asList(fetchResponse.getBody().as(
                        uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile[].class));
        assertEquals(2, fetchedList.size());
        fetchedList.forEach(caseWorkerProfile ->
                assertTrue(caseWorkerIds.contains(caseWorkerProfile.getId())));
    }

    @Test
    public void shouldGetOnlyFewCaseWorkerDetails() throws Exception {

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>(caseWorkerApiClient
                .createCaseWorkerProfiles());

        List<String> caseWorkerIds = new ArrayList<>();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(201);

        for (CaseWorkersProfileCreationRequest request : caseWorkersProfileCreationRequests) {
            UserProfileResponse resource =
                    testRequestHandler.sendGet(HttpStatus.OK,
                            "?email=" + request.getEmailId(),
                            UserProfileResponse.class, userProfUrl + "/v1/userprofile"
                    );

            caseWorkerIds.add(resource.getIdamId());

        }
        caseWorkerIds.add("randomId");

        assertEquals(2, caseWorkerIds.size());
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .body(caseWorkerIds)
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
    public void shouldThrowForbiddenExceptionForNonCompliantRole() {
        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("dummyRole")
                .body(Collections.singletonList("someUUID"))
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(403);

    }
}