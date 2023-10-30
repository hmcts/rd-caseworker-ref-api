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
import org.json.JSONArray;
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

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.Assert.assertNotNull;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@PactTestFor(providerName = "referenceData_location")
//@PactFolder("pacts")
public class LocationReferenceDataConsumerTest {

    private static final String LRD_URL = "/refdata/location/orgServices";
    private static final String GET_LRD_URL = "/refdata/location";
    private static final String GET_LRD_COURT_VENUE_URL = "/refdata/location/court-venues";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }


    //@Pact(provider = "referenceData_location", consumer = "lrd_ref_api_for_court_venue")
    public RequestResponsePact executeReturnCourtVenues(PactDslWithProvider builder) {

        return builder
                .given("Court Venues exist for the input request provided")
                .uponReceiving("valid request to retrieve court venue")
                .path(GET_LRD_COURT_VENUE_URL)
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createLrdCourtVenueResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "executeReturnCourtVenues")
    void getExecuteReturnCourtVenues(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .get(mockServer.getUrl() + GET_LRD_COURT_VENUE_URL)
                        .then()
                        .log().all().extract().asString();
        JSONArray jsonArray = new JSONArray(actualResponseBody);
        assertNotNull(jsonArray);
    }

    //@Pact(provider = "referenceData_location", consumer = "lrd_ref_api_for_court_venue_by_service_code")
    public RequestResponsePact executeReturnCourtVenuesByServiceCode(PactDslWithProvider builder) {

        return builder
                .given("Court Venues exist for the service code provided")
                .uponReceiving("valid request to retrieve court venue for given service code")
                .path(GET_LRD_COURT_VENUE_URL + "/services")
                .query("service_code=1")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createLrdCourtVenueForServiceCodeResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "executeReturnCourtVenuesByServiceCode")
    void getExecuteReturnCourtVenuesByServiceCode(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .get(mockServer.getUrl() + GET_LRD_COURT_VENUE_URL + "/services" + "?service_code=1")
                        .then()
                        .log().all().extract().asString();
        JSONObject jsonArray = new JSONObject(actualResponseBody);
        assertNotNull(jsonArray);
    }


    //@Pact(provider = "referenceData_location", consumer = "lrd_ref_api_for_court_venue_by_search_string")
    public RequestResponsePact executeReturnCourtVenuesBySearchString(PactDslWithProvider builder) {

        return builder
                .given("Court Venues exist for the search string provided")
                .uponReceiving("valid request to retrieve court venue by search string")
                .path(GET_LRD_COURT_VENUE_URL + "/venue-search")
                .query("search-string=456")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createLrdCourtVenueResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "executeReturnCourtVenuesBySearchString")
    void getExecuteReturnCourtVenuesBySearchString(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .get(mockServer.getUrl() + GET_LRD_COURT_VENUE_URL + "/venue-search" + "?search-string=456")
                        .then()
                        .log().all().extract().asString();
        JSONArray jsonArray = new JSONArray(actualResponseBody);
        assertNotNull(jsonArray);
    }

    //@Pact(provider = "referenceData_location", consumer = "lrd_ref_api_building_locations_service")
    public RequestResponsePact executeRetrieveBuildingLocationsDetails(PactDslWithProvider builder) {

        return builder
                .given("Building Location details exist for the request provided")
                .uponReceiving("valid request for building location details")
                .path(GET_LRD_URL + "/building-locations")
                .query("building_location_name=Taylor House Tribunal Hearing Centre")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createLrdBuildingLocationsResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "executeRetrieveBuildingLocationsDetails")
    void getExecuteRetrieveBuildingLocationsDetailsTest(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .get(mockServer.getUrl() + GET_LRD_URL + "/building-locations"
                                + "?building_location_name=Taylor House Tribunal Hearing Centre")
                        .then()
                        .log().all().extract().asString();
        JSONObject jsonArray = new JSONObject(actualResponseBody);
        assertNotNull(jsonArray);
    }

    //@Pact(provider = "referenceData_location", consumer = "lrd_ref_api_building_locations_search_service")
    public RequestResponsePact executeRetrieveBuildingLocationsSearchDetails(PactDslWithProvider builder) {

        return builder
                .given("Building Location details exist for the searchString provided")
                .uponReceiving("valid request for building location details")
                .path(GET_LRD_URL + "/building-locations/search")
                .query("search=Taylor")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createLrdBuildingSearchLocationsResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "executeRetrieveBuildingLocationsSearchDetails")
    void getExecuteRetrieveBuildingLocationsSearchDetailsTest(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .get(mockServer.getUrl() + GET_LRD_URL + "/building-locations/search" + "?search=Taylor")
                        .then()
                        .log().all().extract().asString();
        JSONArray jsonArray = new JSONArray(actualResponseBody);
        assertNotNull(jsonArray);
    }



    //@Pact(provider = "referenceData_location", consumer = "lrd_ref_api_service")
    public RequestResponsePact executeRetrieveRegionDetails(PactDslWithProvider builder) {

        return builder
                .given("Region Details exist")
                .uponReceiving("valid request for retrieve region details")
                .path(GET_LRD_URL + "/regions")
                .query("region=National")
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(getResponseHeaders())
                .body(createLrdRegionResponse())
                .toPact();
    }

    //@Test
    //@PactTestFor(pactMethod = "executeRetrieveRegionDetails")
    void getExecuteRetrieveRegionDetailsTest(MockServer mockServer) throws JSONException {
        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(getHttpHeaders())
                        .contentType(ContentType.JSON)
                        .get(mockServer.getUrl() + GET_LRD_URL + "/regions" + "?region=National")
                        .then()
                        .log().all().extract().asString();
        JSONArray jsonArray = new JSONArray(actualResponseBody);
        assertNotNull(jsonArray);
    }


    //GET
    //@Pact(provider = "referenceData_location", consumer = "crd_case_worker_ref_service")
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

    //@Test
    //@PactTestFor(pactMethod = "executeGetOrgService")
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

    private DslPart createLrdRegionResponse() {

        return newJsonArray(o -> o.object(ob -> ob
                .stringType("region_id", "1")
                .stringType("description", "National")
                .stringType("welsh_description", "")
        )
                .object(ob -> ob
                .stringType("region_id", "2")
                .stringType("description", "London")
                .stringType("welsh_description", ""))).build();
    }


    private DslPart createLrdBuildingLocationsResponse() {

        return newJsonBody(o -> o
                .stringType("address", "Address 123")
                .stringType("area", "Area 1")
                .stringType("building_location_id", "123")
                .stringType("cluster_id", "456")
                .stringType("epimms_id", "4567")
                .stringType("building_location_status", "OPEN")
                .stringType("building_location_name", "Taylor House Tribunal Hearing Centre")
                .minArrayLike("court_venues", 1, r -> r
                        .stringType("court_venue_id")
                        .stringType("epimms_id")
                        .stringType("site_name")
                        .stringType("region_id")
                )

        ).build();
    }



    private DslPart createLrdCourtVenueResponse() {

        return newJsonArray(o -> o.object(ob -> ob
                .stringType("cluster_name", "ClusterXYZ")
                .stringType("court_address", "courtAddress")
                .stringType("court_location_code", "courtLocationCode")
                .stringType("court_name", "courtName")
                .stringType("court_status", "Closed")
                .stringType("court_type", "Immigration and Asylum")
                .stringType("court_type_id", "17")
                .stringType("epimms_id", "12345")
                )
                .object(ob -> ob
                .stringType("cluster_name", "ClusterXYZ")
                .stringType("court_address", "courtAddress")
                .stringType("court_location_code", "courtLocationCode")
                .stringType("court_name", "courtName")
                .stringType("court_status", "Open")
                .stringType("court_type", "Immigration and Asylum")
                .stringType("court_type_id", "17")
                .stringType("epimms_id", "123456"))
        ).build();
    }


    private DslPart createLrdCourtVenueForServiceCodeResponse() {

        return newJsonBody(o -> o
                .stringType("court_type_id", "17")
                .stringType("service_code", "1")
                .stringType("court_type", "Immigration and Asylum")


        ).build();
    }

    private DslPart createLrdCourtVenueForSearchString() {

        return newJsonBody(o -> o
                .stringType("welsh_court_type")
                .stringType("service_code", "1")
                .stringType("court_type", "Immigration and Asylum")
                .stringType("court_type_id", "17")


        ).build();
    }

    private DslPart createLrdBuildingSearchLocationsResponse() {

        return newJsonArray(o ->
                o.object(ob -> ob
                        .stringType("region", "Region XYZ")
                        .stringType("cluster_name", "ClusterXYZ")
                        .stringType("cluster_id", "456")
                        .stringType("epimms_id", "4567")
                        .stringType("building_location_status", "OPEN")
                        .stringType("building_location_name", "Taylor House Tribunal Hearing Centre")
                        .stringType("address", "Address 123")
                        .stringType("area", "Area 1")
                        .stringType("postcode", "XY2 YY3")
                        .stringType("region_id", "123")
                                )
                .object(obj -> obj
                                .stringType("region", "Region XYZ")
                                .stringType("cluster_name", "ClusterXYZ")
                                .stringType("cluster_id", "456")
                                .stringType("epimms_id", "45678")
                                .stringType("building_location_status", "CLOSED")
                                .stringType("building_location_name", "Taylor House Tribunal Hearing Centre 2")
                                .stringType("address", "Address 123456")
                                .stringType("area", "Area 2")
                                .stringType("postcode", "XY21 YY3")
                                .stringType("region_id", "123")
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
