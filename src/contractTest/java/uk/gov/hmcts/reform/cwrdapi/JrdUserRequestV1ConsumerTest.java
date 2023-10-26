package uk.gov.hmcts.reform.cwrdapi;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.core.model.RequestResponsePact;
import com.fasterxml.jackson.core.JsonProcessingException;
import groovy.util.logging.Slf4j;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@PactTestFor(providerName = "referenceData_judicial")
//@PactFolder("pacts")
public class JrdUserRequestV1ConsumerTest {

    private static final String JRD_GET_PROFILES_URL = "/refdata/judicial/users";

    private static final String SIDAM_ID = "44362987-4b00-f2e7-4ff8-761b87f16bf9";


    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }


    //To run the Pact tests please uncommnet all Pact Test annotations
    //@Pact(provider = "referenceData_judicial", consumer = "JRD_API_V1_Search_ConsumerTest")
    public RequestResponsePact searchJrdProfiles(PactDslWithProvider builder) throws JsonProcessingException {

        return builder
                .given("User profile details exist for the search request provided")
                .uponReceiving("the api returns judicial user profiles "
                        + "based on the provided list of user ids")
                .path(JRD_GET_PROFILES_URL + "/search")
                .body(createJrdProfileSearchRequest())
                .method(HttpMethod.POST.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(searchJrdProfilesResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "searchJrdProfiles")
    void searchJrdProfiles(MockServer mockServer)
            throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .body(createJrdProfileSearchRequest().toString())
                        .contentType(ContentType.JSON)
                        .post(mockServer.getUrl() + JRD_GET_PROFILES_URL + "/search")
                        .then()
                        .log().all().extract().asString();

        JSONArray response = new JSONArray(actualResponseBody);
        Assertions.assertThat(response).isNotNull();

    }

    //@Pact(provider = "referenceData_judicial", consumer = "JRD_Fetch_API_ConsumerTest")
    public RequestResponsePact getJudicialUserProfiles(PactDslWithProvider builder) throws JsonProcessingException {

        return builder
                .given("User profile details exist")
                .uponReceiving("the api returns judicial user profiles ")
                .path(JRD_GET_PROFILES_URL + "/fetch")
                .body(createJrdProfileUserRequest())
                .method(HttpMethod.POST.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createJrdFetchProfilesResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "getJudicialUserProfiles")
    void executeGetJudicialUserProfiles(MockServer mockServer)
            throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .body(createJrdProfileUserRequest().toString())
                        .contentType(ContentType.JSON)
                        .post(mockServer.getUrl() + JRD_GET_PROFILES_URL + "/fetch")
                        .then()
                        .log().all().extract().asString();

        JSONArray response = new JSONArray(actualResponseBody);
        Assertions.assertThat(response).isNotNull();

    }

    //@Pact(provider = "referenceData_judicial", consumer = "JRD_API_ConsumerTest")
    public RequestResponsePact getJrdProfilesListOfIds(PactDslWithProvider builder) throws JsonProcessingException {

        return builder
                .given("return judicial user profiles along with their active appointments and authorisations")
                .uponReceiving("the api returns judicial user profiles "
                        + "based on the provided list of user ids")
                .path(JRD_GET_PROFILES_URL)
                .body(createJrdProfileUpdateRequest())
                .method(HttpMethod.POST.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createJrdProfilesResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "getJrdProfilesListOfIds")
    void executeGetJrdProfilesListOfIds(MockServer mockServer)
            throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .body(createJrdProfileUpdateRequest().toString())
                        .contentType(ContentType.JSON)
                        .post(mockServer.getUrl() + JRD_GET_PROFILES_URL)
                        .then()
                        .log().all().extract().asString();

        JSONArray response = new JSONArray(actualResponseBody);
        Assertions.assertThat(response).isNotNull();

    }

    //@Pact(provider = "referenceData_judicial", consumer = "JRD_API_consumerTest")
    public RequestResponsePact getJrdProfilesServiceName(PactDslWithProvider builder) throws JsonProcessingException {

        return builder
                .given("return judicial user profiles along with their active appointments and authorisations")
                .uponReceiving("the api returns judicial user profiles "
                        + "based on the provided service name")
                .path(JRD_GET_PROFILES_URL)
                .body(createJrdProfileForServiceNameRequest())
                .method(HttpMethod.POST.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createJrdProfilesResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "getJrdProfilesServiceName")
    void executeGetJrdProfilesServiceName(MockServer mockServer)
            throws JSONException {
        var actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .body(createJrdProfileForServiceNameRequest().toString())
                        .contentType(ContentType.JSON)
                        .post(mockServer.getUrl() + JRD_GET_PROFILES_URL)
                        .then()
                        .log().all().extract().asString();

        JSONArray response = new JSONArray(actualResponseBody);
        Assertions.assertThat(response).isNotNull();

    }

    private DslPart createJrdProfilesResponse() {
        return newJsonArray(o -> o.object(ob -> ob
                .stringType("sidam_id", SIDAM_ID)
                .stringType("object_id", "fcb4f03c-4b3f-4c3c-bf3a-662b4557b470")
                .stringType("email_id", "e@mail.com")
                .minArrayLike("appointments", 1, r -> r
                        .stringType("location_id", "1")
                )
                .minArrayLike("authorisations", 1, r -> r
                        .stringType("jurisdiction", "IA")
                )
        )).build();
    }

    private DslPart createJrdFetchProfilesResponse() {
        return newJsonArray(o -> o.object(ob -> ob
                .stringType("idamId", SIDAM_ID)
                .minArrayLike("appointments", 1, r -> r
                        .stringType("appointmentId", "1")
                )
                .minArrayLike("authorisations", 1, r -> r

                        .stringType("authorisationId", "1234")
                )
        )).build();
    }


    private DslPart createJrdProfileUpdateRequest() {
        return newJsonBody(o -> o
                .stringType("ccdServiceName", null)
                .stringType("object_ids", null)
                .minArrayLike("sidam_ids", 1,
                        PactDslJsonRootValue.stringType("44362987-4b00-f2e7-4ff8-761b87f16bf9"),1)
        ).build();
    }


    private DslPart createJrdProfileUserRequest() {
        return newJsonBody(o -> o
                .minArrayLike("userIds", 1,
                        PactDslJsonRootValue.stringType("44362987-4b00-f2e7-4ff8-761b87f16bf9"),1)
        ).build();
    }

    private DslPart createJrdProfileForServiceNameRequest() {
        return newJsonBody(o -> o
                .stringType("ccdServiceName", "CMC")
                .stringType("object_ids", null)
                .minArrayLike("sidam_ids", 1,
                        PactDslJsonRootValue.stringType("44362987-4b00-f2e7-4ff8-761b87f16bf9"),1)
        ).build();
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");
        return responseHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

    private DslPart createJrdProfileSearchRequest() {
        return newJsonBody(o -> o
                .stringType("searchString", "testFullName")
                .stringType("serviceCode", "CMC")
                .stringType("location", "location")
        ).build();
    }

    private DslPart searchJrdProfilesResponse() {
        return newJsonArray(o -> o.object(ob -> ob
                .stringType("idamId", SIDAM_ID)
                .stringType("fullName", "testFullName")
                .stringType("knownAs", "testKnownAs")
                .stringType("surname", "surname")
                .stringType("emailId", "test@test.com")
                .stringType("title", "Family Judge")
                .stringType("personalCode", "1234")))
                .build();
    }
}
