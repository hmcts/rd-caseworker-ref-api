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
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static org.junit.Assert.assertNotNull;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "referenceData_location")
@PactFolder("pacts")
public class LocationReferenceDataConsumerTest {

    private static final String LRD_URL = "/refdata/location/orgServices";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    //GET
    @Pact(provider = "referenceData_location", consumer = "crd_case_worker_ref_service")
    public RequestResponsePact executeGetOrgService(PactDslWithProvider builder) {

        return builder
                .given("Organisational Service details exist")
                .uponReceiving("valid request for profile data based on service name")
                .path(LRD_URL)
                .query("ccdServiceNames=cmc")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createLrdOrgServiceResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetOrgService")
    void getOrgServiceTest(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .get(mockServer.getUrl() + LRD_URL + "?ccdServiceNames=cmc")
                        .then()
                        .log().all().extract().asString();
        JSONArray jsonArray = new JSONArray(actualResponseBody);
        assertNotNull(jsonArray);
    }

    private DslPart createLrdOrgServiceResponse() {
        return newJsonArray(o -> o.object(ob -> ob
                .stringType("service_code",
                        "AAA6")
                .stringType("ccd_service_name", "CMC")
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
