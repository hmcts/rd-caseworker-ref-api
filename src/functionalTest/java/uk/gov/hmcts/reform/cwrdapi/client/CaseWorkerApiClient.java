package uk.gov.hmcts.reform.cwrdapi.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.idam.IdamOpenIdClient;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.nonNull;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.generateRandomEmail;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.setEmailsTobeDeleted;


@Slf4j
@Component
public class CaseWorkerApiClient {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String SERVICE_HEADER = "ServiceAuthorization";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final String caseWorkerApiUrl;
    private final String s2sToken;


    protected IdamOpenIdClient idamOpenIdClient;

    public CaseWorkerApiClient(
            String caseWorkerApiUrl,
            String s2sToken, IdamOpenIdClient idamOpenIdClient) {
        this.caseWorkerApiUrl = caseWorkerApiUrl;
        this.s2sToken = s2sToken;
        this.idamOpenIdClient = idamOpenIdClient;
    }

    public IdamOpenIdClient getidamOpenIdClient() {
        return idamOpenIdClient;
    }

    public String getWelcomePage() {
        return withUnauthenticatedRequest()
                .get("/")
                .then()
                .statusCode(OK.value())
                .and()
                .extract()
                .response()
                .body()
                .asString();
    }

    public String getHealthPage() {
        return withUnauthenticatedRequest()
                .get("/health")
                .then()
                .statusCode(OK.value())
                .and()
                .extract()
                .response()
                .body()
                .asString();
    }


    public RequestSpecification withUnauthenticatedRequest() {
        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .baseUri(caseWorkerApiUrl)
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("Accepts", APPLICATION_JSON_VALUE);
    }

    public RequestSpecification getMultipleAuthHeadersInternal(String role) {
        return getMultipleAuthHeaders(idamOpenIdClient.getInternalOpenIdToken(role));
    }

    public RequestSpecification getMultiPartWithAuthHeaders(String role) {
        return withAuthenticatedMultipartRequestHeader(idamOpenIdClient.getInternalOpenIdToken(role));
    }

    private RequestSpecification withAuthenticatedMultipartRequestHeader(String userToken) {
        return SerenityRest.with()
                .relaxedHTTPSValidation()
                .baseUri(caseWorkerApiUrl)
                .header(SERVICE_HEADER, "Bearer " + s2sToken)
                .header("Content-Type", MediaType.MULTIPART_FORM_DATA_VALUE)
                .header(AUTHORIZATION_HEADER, "Bearer " + userToken);
    }

    public RequestSpecification getMultipleAuthHeaders(String userToken) {
        return SerenityRest.with()
                .relaxedHTTPSValidation()
                .baseUri(caseWorkerApiUrl)
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("Accepts", APPLICATION_JSON_VALUE)
                .header(SERVICE_HEADER, "Bearer " + s2sToken)
                .header(AUTHORIZATION_HEADER, "Bearer " + userToken);
    }


    public List<CaseWorkersProfileCreationRequest> createCaseWorkerProfiles(String... email) {
        List<CaseWorkerLocationRequest> locationRequestList = ImmutableList.of(CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .location("test location")
                .locationId(2).isPrimaryFlag(true).build());

        List<CaseWorkerRoleRequest> roleRequests = ImmutableList.of(CaseWorkerRoleRequest
                .caseWorkerRoleRequest()
                .role("tribunal-caseworker").isPrimaryFlag(true).build());

        List<CaseWorkerWorkAreaRequest> areaRequests = ImmutableList.of(CaseWorkerWorkAreaRequest
                .caseWorkerWorkAreaRequest()
                .serviceCode("BAA1").areaOfWork("Non-Money Claims").build());

        Set<String> idamRoles = new HashSet<>();

        String emailToUsed = isNotEmpty(email) && nonNull(email[0]) ? email[0] : generateRandomEmail();
        setEmailsTobeDeleted(emailToUsed.toLowerCase());
        return ImmutableList.of(
                CaseWorkersProfileCreationRequest
                        .caseWorkersProfileCreationRequest()
                        .firstName("cwr-test")
                        .lastName("cwr-test")
                        .emailId(emailToUsed.toLowerCase())
                        .regionId(1)
                        .region("National")
                        .userType("CTSC")
                        .suspended(false)
                        .idamRoles(idamRoles)
                        .baseLocations(locationRequestList)
                        .roles(roleRequests)
                        .workerWorkAreaRequests(areaRequests).build());
    }

    @SuppressWarnings("unused")
    private JsonNode parseJson(String jsonString) throws IOException {
        return mapper.readTree(jsonString);
    }

}