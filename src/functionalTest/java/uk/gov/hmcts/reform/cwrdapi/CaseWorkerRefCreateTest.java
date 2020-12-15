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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
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
    public void shouldGetCaseWorkerDetails() {
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

        List<CaseWorkerProfile> caseWorkerProfileList = Arrays.asList(response.getBody().as(CaseWorkerProfile[].class));
        for (CaseWorkerProfile profile : caseWorkerProfileList) {
            log.info("shouldGetCaseWorkerDetails : " + profile.getCaseWorkerId());
        }
        caseWorkerProfileList.forEach(caseWorkerProfile -> caseWorkerIds.add(caseWorkerProfile.getCaseWorkerId()));

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
    public void shouldGetOnlyFewCaseWorkerDetails() {
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

        List<CaseWorkerProfile> caseWorkerProfileList = Arrays.asList(response.getBody().as(CaseWorkerProfile[].class));
        assertEquals(2, caseWorkerProfileList.size());
        caseWorkerIds.add(caseWorkerProfileList.get(0).getCaseWorkerId());
        caseWorkerIds.add("418a6015-ca64-4658-b792-b0b096644edb");

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
