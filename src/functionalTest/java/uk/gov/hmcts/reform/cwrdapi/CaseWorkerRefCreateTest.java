package uk.gov.hmcts.reform.cwrdapi;

import com.google.common.collect.ImmutableList;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
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
        Map<String, String> userDetail = idamOpenIdClient.createUser("caseworker-iac-bulkscan");
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
}
