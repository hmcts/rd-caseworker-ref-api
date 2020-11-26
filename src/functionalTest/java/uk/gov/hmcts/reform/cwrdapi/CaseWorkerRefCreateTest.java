package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.List;
import java.util.Map;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class CaseWorkerRefCreateTest extends AuthorizationFunctionalTest {

    @Value("${userProfUrl}")
    protected String baseUrlUserProfile;

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
    public void whenUserNotExistsInCwrAndUpAndExistsInSidam_Ac2() {
        Map<String, String> userDeatil = idamOpenIdClient.createUser("caseworker_iac_dwp");
        String userEmail = userDeatil.get(EMAIL);

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
            .createCaseWorkerProfiles(userEmail);

        Response response = caseWorkerApiClient.getMultipleAuthHeadersInternal()
            .body(caseWorkersProfileCreationRequests)
            .post("/refdata/case-worker/users/")
            .andReturn();
        response.then()
            .assertThat()
            .statusCode(201);

        //        UserProfileResponse upResponse = funcTestRequestHandler.sendGet(HttpStatus.OK,
        //            "/v1/userprofile?email="
        //                + userEmail.toLowerCase(),
        //            UserProfileResponse.class, baseUrlUserProfile);
        //        List<String> exceptedRoles = ImmutableList.of("caseworker-iac-bulkscan", "caseworker_iac_dwp");
        //        assertEquals(upResponse.getRoles(), exceptedRoles);
    }
}
