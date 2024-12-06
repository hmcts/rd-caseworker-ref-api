package uk.gov.hmcts.reform.cwrdapi;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.core.model.RequestResponsePact;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import io.restassured.http.ContentType;
import jakarta.validation.constraints.NotNull;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@PactTestFor(providerName = "referenceData_location")
//@PactFolder("pacts")
public class CommonDataApiConsumerTest {

    private static final String CRD_URL = "/refdata/commondata/lov/categories";
    private static final String CRD_CASE_FLAG_URL = "/refdata/commondata/caseflags/service-id";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    //@Pact(provider = "referenceData_commondata", consumer = "crd_api_for_list_of_category")
    public RequestResponsePact executeListOfCategoryValues(PactDslWithProvider builder) {

        return builder
                .given("ListOfCategories Details Exist")
                .uponReceiving("valid request to retrieve list of category values")
                .path(CRD_URL + "/HearingChannel")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createCrdListOfValuesResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "executeListOfCategoryValues")
    void getExecuteListOfCategoryValues(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .get(mockServer.getUrl() + CRD_URL + "/HearingChannel")
                        .then()
                        .log().all().extract().asString();
        JSONObject jsonArray = new JSONObject(actualResponseBody);
        assertNotNull(jsonArray);
    }



    //@Pact(provider = "referenceData_commondata", consumer = "crd_api_for_list_of_case_flag")
    public RequestResponsePact executeCaseFlagDetailsForServiceID(PactDslWithProvider builder) {

        return builder
                .given("Case Flag Details Exist")
                .uponReceiving("valid request to retrieve list of case flag for given servie Id")
                .path(CRD_CASE_FLAG_URL + "=HearingChannel")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createCrdListOfCaseFlagResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "executeCaseFlagDetailsForServiceID")
    void getExecuteCaseFlagDetailsForServiceID(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .get(mockServer.getUrl() + CRD_CASE_FLAG_URL + "=HearingChannel")
                        .then()
                        .log().all().extract().asString();
        JSONObject jsonArray = new JSONObject(actualResponseBody);
        assertNotNull(jsonArray);
    }

    private DslPart createCrdListOfValuesResponse() {

        return newJsonBody(o -> o
                .minArrayLike("list_of_values",1,obj -> obj
                        .stringType("category_key","HearingChannel")
                        .stringType("key","video")
                        .stringType("value_en","Video")
                        .stringType("value_cy",null)
                        .stringType("hint_text_en",null)
                        .stringType("hint_text_cy",null)
                        .stringType("parent_category",null)
                        .stringType("active_flag","Y")
                )
        ).build();
    }

    private DslPart createCrdListOfCaseFlagResponse() {

        return newJsonBody(o -> o
                .minArrayLike("flags",1,obj -> obj
                        .minArrayLike("FlagDetails",1,ob -> ob
                        .stringType("flagCode","RA0001")
                        )
                )
        ).build();
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
