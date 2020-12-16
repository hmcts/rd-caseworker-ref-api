package uk.gov.hmcts.reform.cwrdapi;

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
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
@TestPropertySource(properties = {"spring.config.location=classpath:application-functional.yml"})
public class CaseWorkerRefCreateTest extends AuthorizationFunctionalTest {

    @Autowired
    protected FuncTestRequestHandler testRequestHandler;

    @Value("${userProfUrl}")
    protected String userProfUrl;

    @Test
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

        List<CaseWorkerProfile> fetchedList = Arrays.asList(fetchResponse.getBody().as(CaseWorkerProfile[].class));
        assertEquals(2, fetchedList.size());
        fetchedList.forEach(caseWorkerProfile ->
                assertTrue(caseWorkerIds.contains(caseWorkerProfile.getCaseWorkerId())));
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

        List<CaseWorkerProfile> fetchedList = Arrays.asList(fetchResponse.getBody().as(CaseWorkerProfile[].class));
        assertEquals(1, fetchedList.size());
        CaseWorkerProfile caseWorkerProfile = fetchedList.get(0);

        assertTrue(caseWorkerIds.contains(caseWorkerProfile.getCaseWorkerId()));
        assertTrue(StringUtils.isNotEmpty(caseWorkerProfile.getFirstName()));
        assertTrue(StringUtils.isNotEmpty(caseWorkerProfile.getLastName()));
        assertTrue(StringUtils.isNotEmpty(caseWorkerProfile.getEmailId()));

        assertTrue(CollectionUtils.isNotEmpty(caseWorkerProfile.getCaseWorkerLocations()));
        assertTrue(CollectionUtils.isNotEmpty(caseWorkerProfile.getCaseWorkerRoles()));
        assertTrue(CollectionUtils.isNotEmpty(caseWorkerProfile.getCaseWorkerWorkAreas()));

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
