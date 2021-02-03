package uk.gov.hmcts.reform.cwrdapi;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "rd_user_profile_api_service")
@PactFolder("pacts")
@SpringBootTest
public class UserProfileConsumerTest {

    private static final String UP_URL = "/v1/userprofile/";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    //GET
    @Pact(provider = "rd_user_profile_api_service", consumer = "crd_case_worker_ref_service")
    public RequestResponsePact executeGetUserProfileAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A user profile get request is submitted with valid email")
                .uponReceiving("valid request for profile data based on email")
                .path(UP_URL)
                .method(HttpMethod.GET.toString())
                .query("email=james.bond@justice.gov.uk")
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createUserProfileGetResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserProfileAndGet200")
    void getUserProfileAndGet200Test(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .queryParam("email", "james.bond@justice.gov.uk")
                        .get(mockServer.getUrl() + UP_URL)
                        .then()
                        .log().all().extract().asString();

        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        Assertions.assertNotNull(jsonResponse);
    }

    private DslPart createUserProfileGetResponse() {
        return newJsonBody(o -> o
                .stringType("email", "james.bond@justice.gov.uk")
                .stringType("firstName", "james")
                .stringType("lastName", "bond")
                .stringType("idamStatus", "Live")
                .stringType("userIdentifier", "007")
                ).build();
    }

    //Create
    private DslPart createUserProfileCreateResponse() {
        return null;
    }

    //Update
    @Pact(provider = "rd_user_profile_api_service", consumer = "crd_case_worker_ref_service")
    public RequestResponsePact executeUpdateUserProfileAndGet200(PactDslWithProvider builder) {

        return builder
                .given("A user profile update request is submitted")
                .uponReceiving("valid request to update profile data")
                .path(UP_URL + "007")
                .method(HttpMethod.PUT.toString())
                .body(createUserProfileUpdateRequest())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createUserProfileUpdateResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeUpdateUserProfileAndGet200")
    void updateUserProfileAndGet200Test(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .body(createUserProfileUpdateRequest().toString())
                        .put(mockServer.getUrl() + UP_URL + "007")
                        .then()
                        .log().all().extract().asString();

        JSONObject jsonResponse = new JSONObject(actualResponseBody);
        Assertions.assertNotNull(jsonResponse);
    }

    private DslPart createUserProfileUpdateRequest() {
        return newJsonBody(o -> o
                .stringType("email", "james.bond@justice.gov.uk")
                .stringType("firstName", "james")
                .stringType("lastName", "bond")
                .stringType("idamStatus", "status")
                .minArrayLike("rolesAdd", 1, obj -> obj.stringType("name", "tribunal-caseworker"))
                .minArrayLike("rolesDelete", 1, obj -> obj.stringType("name", "caseworker"))
        ).build();
    }

    private DslPart createUserProfileUpdateResponse() {
        return newJsonBody(o -> o
                .object("roleAdditionResponse", attribute -> attribute
                        .stringType("idamMessage", "11 OK")
                        .stringType("idamStatusCode", "200"))
                .minArrayLike("roleDeletionResponse", 1, 1,
                        roleDeletionResponse -> roleDeletionResponse
                                .stringType("idamMessage", "11 OK")
                                .stringType("idamStatusCode", "200")
                                .stringType("roleName", "caseworker")
                )).build();
    }


    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
                "application/json");
        return responseHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

}
