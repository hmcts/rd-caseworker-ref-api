package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.CaseWorkerProfileCreationResponse;
import uk.gov.hmcts.reform.cwrdapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.cwrdapi.util.FeatureConditionEvaluation;
import uk.gov.hmcts.reform.cwrdapi.util.ToggleEnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@RunWith(CustomSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
@TestPropertySource(properties = {"spring.config.location=classpath:application-functional.yml"})
public class CaseWorkerRefCreateTest extends AuthorizationFunctionalTest {

    @Autowired
    public FuncTestRequestHandler testRequestHandler;

    public static final String CREATE_CASEWORKER_PROFILE = "CaseWorkerRefController.createCaseWorkerProfiles";
    public static final String FETCH_BY_CASEWORKER_ID = "CaseWorkerRefController.fetchCaseworkersById";

    @Value("${userProfUrl}")
    protected String userProfUrl;

    @Test
    @ToggleEnable(mapKey = CREATE_CASEWORKER_PROFILE, withFeature = true)
    public void whenUserNotExistsInCwrAndSidamAndUp_Ac1() {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(201);

        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse =
                response.getBody().as(CaseWorkerProfileCreationResponse.class);
        List<String> caseWorkerIds = new ArrayList<>(caseWorkerProfileCreationResponse.getCaseWorkerIds());
        assertEquals(caseWorkersProfileCreationRequests.size(), caseWorkerIds.size());
    }

    @Test
    @ToggleEnable(mapKey = CREATE_CASEWORKER_PROFILE, withFeature = false)
    public void whenUserNotExistsInCwrAndSidamAndUp_Ac1_LD_disabled() {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(CustomSerenityRunner.getFeatureFlagName().concat(" ")
                .concat(FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD));
    }

    @Test
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = true)
    public void shouldGetCaseWorkerDetails() throws Exception {
        //Create 2 Caseworker Users
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>();

        caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                .createCaseWorkerProfiles());
        caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                .createCaseWorkerProfiles());

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(201);

        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse =
                response.getBody().as(CaseWorkerProfileCreationResponse.class);
        List<String> caseWorkerIds = new ArrayList<>(caseWorkerProfileCreationResponse.getCaseWorkerIds());
        assertEquals(2, caseWorkerIds.size());
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .body(UserRequest.builder().userIds(caseWorkerIds).build())
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
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = false)
    public void should_retrieve_403_when_Api_toggled_off() {

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>(
                caseWorkerApiClient.createCaseWorkerProfiles());
        String exceptionMessage = CustomSerenityRunner.getFeatureFlagName().concat(" ")
                .concat(FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD);
        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/fetchUsersById")
                .andReturn();
        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(exceptionMessage);
    }

    @Test
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = true)
    public void shouldGetOnlyFewCaseWorkerDetails() throws Exception {

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = new ArrayList<>(caseWorkerApiClient
                .createCaseWorkerProfiles());

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(201);

        CaseWorkerProfileCreationResponse caseWorkerProfileCreationResponse =
                response.getBody().as(CaseWorkerProfileCreationResponse.class);
        List<String> caseWorkerIds = new ArrayList<>(caseWorkerProfileCreationResponse.getCaseWorkerIds());
        caseWorkerIds.add("randomId");

        assertEquals(2, caseWorkerIds.size());
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal("cwd-admin")
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
    @ToggleEnable(mapKey = FETCH_BY_CASEWORKER_ID, withFeature = true)
    public void shouldThrowForbiddenExceptionForNonCompliantRole() {
        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("dummyRole")
                .body(UserRequest.builder().userIds(Collections.singletonList("someUUID")).build())
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(403);

    }
}
