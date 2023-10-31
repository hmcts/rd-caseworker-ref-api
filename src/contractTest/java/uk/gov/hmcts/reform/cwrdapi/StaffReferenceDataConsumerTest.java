package uk.gov.hmcts.reform.cwrdapi;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.core.model.RequestResponsePact;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.client.fluent.Executor;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.Assert.assertNotNull;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@PactTestFor(providerName = "referenceData_caseworkerRefUsers")
//@PactFolder("pacts")
public class StaffReferenceDataConsumerTest {

    private static final String CW_URL = "/refdata/case-worker";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }


    // GET call : This API is used to retrieve the service specific skills
    //@Pact(provider = "referenceData_caseworkerRefUsers", consumer = "referenceData_caseworker_consumer")
    public RequestResponsePact retrieveSkills(PactDslWithProvider builder) {

        return builder
            .given("A list of staff ref data Service skills with serviceCodes")
            .uponReceiving("Service Skills Data")
            .path(CW_URL+"/skill")
            .query("service_codes=CCA")
            .method(HttpMethod.GET.toString())
            .headers(getResponseHeaders())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(getResponseHeaders())
            .body(retrieveOrgServiceResponse())
            .toPact();
    }

    // GET call : This API is used to retrieve the service specific skills
    //@Test
    //@PactTestFor(pactMethod = "retrieveSkills")
    void getRetrieveSkillsTest(MockServer mockServer) throws JSONException {
        String actualResponseBody =
            SerenityRest
                .given()
                .headers(getHttpHeaders())
                .contentType(ContentType.JSON)
                .get(mockServer.getUrl() + CW_URL+"/skill"+"?service_codes=CCA")
                .then()
                .log().all().extract().asString();
        JSONObject jsonArray = new JSONObject(actualResponseBody);
        assertNotNull(jsonArray);
    }


    // GET call : This API gets the user types from staff reference data
    //@Pact(provider = "referenceData_caseworkerRefUsers", consumer = "referenceData_caseworker_consumer")
    public RequestResponsePact retrieveUserTypes(PactDslWithProvider builder) {

        return builder
            .given("A list of all staff reference data user-type")
            .uponReceiving("user types from staff reference data")
            .path(CW_URL+"/user-type")
            .method(HttpMethod.GET.toString())
            .headers(getResponseHeaders())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(getResponseHeaders())
            .body(retrieveUserTypeResponse())
            .toPact();
    }

    // GET call : This API gets the user types from staff reference data
    //@Test
    //@PactTestFor(pactMethod = "retrieveUserTypes")
    void getRetrieveUserTypesTest(MockServer mockServer) throws JSONException {
        String actualResponseBody =
            SerenityRest
                .given()
                .headers(getHttpHeaders())
                .contentType(ContentType.JSON)
                .get(mockServer.getUrl() + CW_URL+"/user-type")
                .then()
                .log().all().extract().asString();
        JSONObject jsonArray = new JSONObject(actualResponseBody);
        assertNotNull(jsonArray);
    }

    // GET call : This API is used to retrieve the Job Title's
    //@Pact(provider = "referenceData_caseworkerRefUsers", consumer = "referenceData_caseworker_consumer")
    public RequestResponsePact retrieveJobTitles(PactDslWithProvider builder) {

        return builder
            .given("A list of all staff reference data role-type")
            .uponReceiving("role types from staff reference data")
            .path(CW_URL+"/job-title")
            .method(HttpMethod.GET.toString())
            .headers(getResponseHeaders())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(getResponseHeaders())
            .body(retrieveJobTitlesResponse())
            .toPact();
    }

    // GET call : This API is used to retrieve the Job Title's
    //@Test
    //@PactTestFor(pactMethod = "retrieveJobTitles")
    void getRetrieveJobTitlesTest(MockServer mockServer) throws JSONException {
        String actualResponseBody =
            SerenityRest
                .given()
                .headers(getHttpHeaders())
                .contentType(ContentType.JSON)
                .get(mockServer.getUrl() + CW_URL+"/job-title")
                .then()
                .log().all().extract().asString();
        JSONObject jsonArray = new JSONObject(actualResponseBody);
        assertNotNull(jsonArray);
    }


    // GET call : This API search a staff user by Id
    //@Pact(provider = "referenceData_caseworkerRefUsers", consumer = "referenceData_caseworker_consumer")
    public RequestResponsePact retrieveUserById(PactDslWithProvider builder) {

        return builder
            .given("A staff profile by caseworker id")
            .uponReceiving("caseworkerid  from staff reference data")
            .path(CW_URL+"/profile/search-by-id")
            .method(HttpMethod.GET.toString())
            .query("id=123")
            .headers(getResponseHeaders())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .headers(getResponseHeaders())
            .body(retrieveUserByIdResponse())
            .toPact();
    }

    // GET call : This API search a staff user by Id
    //@Test
    //@PactTestFor(pactMethod = "retrieveUserById")
    void getRetrieveUserByIdTest(MockServer mockServer) throws JSONException {
        String actualResponseBody =
            SerenityRest
                .given()
                .headers(getHttpHeaders())
                .contentType(ContentType.JSON)
                .get(mockServer.getUrl() + CW_URL+"/profile/search-by-id?id=123")
                .then()
                .log().all().extract().asString();
        JSONObject jsonArray = new JSONObject(actualResponseBody);
        assertNotNull(jsonArray);
    }



    private DslPart retrieveUserByIdResponse() {
        return newJsonBody(o -> o
                    .stringType("first_name","first name")
                    .stringType("last_name","last name")
                    .stringType("email_id","first@emil.com")
        ).build();
    }

    private DslPart retrieveJobTitlesResponse() {
        return newJsonBody(o -> o
            .minArrayLike("job_title", 1,
                obj -> obj.numberType("role_id", 1)
                    .stringType("role_description","Role Description 1"))
        ).build();
    }


    private DslPart retrieveUserTypeResponse() {
        return newJsonBody(o -> o
            .minArrayLike("user_type", 1,
                obj -> obj.id().numberType("id", 1)
                            .stringType("code","User Type 1"))
        ).build();
    }

    private DslPart retrieveOrgServiceResponse() {
        return newJsonBody(o -> o
            .minArrayLike("service_skill", 1,
                obj -> obj.stringType("id", "BBA3")
                    .minArrayLike("skills",1,
                        ob -> ob.id().numberType("id",1)
                            .stringType("code","A1")
                            .stringType("description","desc1")
                            .stringType("user_type","user_type1"))
            )
        ).build();
    }


    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("ServiceAuthorization", "Bearer " + "1234");
        headers.add("Authorization", "Bearer " + "2345");
        return headers;
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
            "application/json");
        return responseHeaders;
    }

}
