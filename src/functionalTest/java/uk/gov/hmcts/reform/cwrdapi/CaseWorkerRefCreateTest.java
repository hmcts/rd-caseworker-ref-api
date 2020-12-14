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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.config.RepositoryConfig;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.domain.CaseWorkerProfile;
import uk.gov.hmcts.reform.cwrdapi.service.CaseWorkerService;
import uk.gov.hmcts.reform.cwrdapi.service.impl.CaseWorkerServiceImpl;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
//@ContextConfiguration(classes = {CaseWorkerRefApiApplication.class})
public class CaseWorkerRefCreateTest extends AuthorizationFunctionalTest {

    @Test
    public void whenUserNotExistsInCwrAndSidamAndUp_Ac1() {
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
            .createCaseWorkerProfiles();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal()
            .body(caseWorkersProfileCreationRequests)
            .post("/refdata/case-worker/users/")
            .andReturn();
        response.then()
            .assertThat()
            .statusCode(201);
    }

    @Test
    public void whenUserNotExistsInCwrAndUpAndExistsInSidam_Ac2() throws Exception {
        Map<String, String> userDetail = idamOpenIdClient.createUser("crd-admin");
        String userEmail = userDetail.get(EMAIL);

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
            .createCaseWorkerProfiles(userEmail.toLowerCase());

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal()
            .body(caseWorkersProfileCreationRequests)
            .post("/refdata/case-worker/users/")
            .andReturn();
        response.then()
            .assertThat()
            .statusCode(201);

        UserProfileResponse upResponse = funcTestRequestHandler.sendGet(HttpStatus.OK,
            "/v1/userprofile/roles?email="
                + userEmail.toLowerCase() +"",
            UserProfileResponse.class, baseUrlUserProfile);
        List<String> exceptedRoles = ImmutableList.of("cwd-user","caseworker-iac-bulkscan");
        assertEquals(exceptedRoles, upResponse.getRoles());
    }

    @Test
    public void shouldGetCaseWorkerDetails() {
        //Create 2 Caseworker Users
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles();

        caseWorkersProfileCreationRequests.addAll(caseWorkerApiClient
                .createCaseWorkerProfiles());
        List<String> caseWorkerIds = new ArrayList<>();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal()
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(201);

        List<CaseWorkerProfile> caseWorkerProfileList = (List<CaseWorkerProfile>) response.getBody();
        caseWorkerProfileList.forEach(caseWorkerProfile -> caseWorkerIds.add(caseWorkerProfile.getCaseWorkerId()));

        assertEquals(2, caseWorkerIds.size());
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal()
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        List<CaseWorkerProfile> fetchedList = (List<CaseWorkerProfile>) response.getBody();
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

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal()
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(201);

        List<CaseWorkerProfile> caseWorkerProfileList = (List<CaseWorkerProfile>) response.getBody();
        assertEquals(2, caseWorkerProfileList.size());
        caseWorkerIds.add(caseWorkerProfileList.get(0).getCaseWorkerId());
        caseWorkerIds.add("418a6015-ca64-4658-b792-b0b096644edb");

        assertEquals(2, caseWorkerIds.size());
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal()
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        fetchResponse.then()
                .assertThat()
                .statusCode(200);

        List<CaseWorkerProfile> fetchedList = (List<CaseWorkerProfile>) response.getBody();
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
        //Create 2 Caseworker Users
        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles();

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal("dummyRole")
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users/fetchUsersById/")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(403);

    }
}
