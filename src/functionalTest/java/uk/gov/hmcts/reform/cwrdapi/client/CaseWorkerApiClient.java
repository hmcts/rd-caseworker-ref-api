package uk.gov.hmcts.reform.cwrdapi.client;

import com.google.common.collect.ImmutableList;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserTypesResponse;
import uk.gov.hmcts.reform.cwrdapi.idam.IdamOpenIdClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Objects.nonNull;
import static net.logstash.logback.encoder.org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.ROLE_STAFF_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.generateRandomEmail;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.setEmailsTobeDeleted;


@Slf4j
@Component
public class CaseWorkerApiClient {

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

    public RequestSpecification withUnauthenticatedRequest() {
        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .baseUri(caseWorkerApiUrl)
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("Accepts", APPLICATION_JSON_VALUE);
    }

    public RequestSpecification getMultipleAuthHeadersInternal() {
        return getMultipleAuthHeaders(idamOpenIdClient.getcwdAdminOpenIdToken("cwd-admin"));
    }

    public RequestSpecification getMultipleAuthHeadersInternal(String role) {
        return getMultipleAuthHeaders(idamOpenIdClient.getOpenIdTokenByRole(role));
    }

    public RequestSpecification getMultiPartWithAuthHeaders(String role) {
        String userToken = idamOpenIdClient.getOpenIdTokenByRole(role);
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

    public RequestSpecification getMultipleAuthHeadersWithoutContentType(String role) {
        String userToken = idamOpenIdClient.getOpenIdTokenByRole(role);
        return SerenityRest.with()
                .relaxedHTTPSValidation()
                .baseUri(caseWorkerApiUrl)
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
                .role("Legal Caseworker").isPrimaryFlag(true).build());


        CaseWorkerWorkAreaRequest workerWorkAreaRequest1 = CaseWorkerWorkAreaRequest
                .caseWorkerWorkAreaRequest()
                .serviceCode("BBA9").areaOfWork("Immigration and Asylum Appeals").build();

        CaseWorkerWorkAreaRequest workerWorkAreaRequest2 = CaseWorkerWorkAreaRequest
                .caseWorkerWorkAreaRequest()
                .serviceCode("ABA1").areaOfWork("Divorce").build();

        List<CaseWorkerWorkAreaRequest> areaRequests =
                ImmutableList.of(workerWorkAreaRequest1, workerWorkAreaRequest2);

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
                        .caseAllocator(true)
                        .taskSupervisor(false)
                        .idamRoles(idamRoles)
                        .baseLocations(locationRequestList)
                        .roles(roleRequests)
                        .workerWorkAreaRequests(areaRequests).build());
    }

    public List<CaseWorkersProfileCreationRequest> updateCaseWorkerProfileRequest(String... email) {
        List<CaseWorkerLocationRequest> locationRequestList = List.of(
                CaseWorkerLocationRequest
                        .caseWorkersLocationRequest()
                        .location("updated location")
                        .locationId(3)
                        .isPrimaryFlag(true).build(),
                CaseWorkerLocationRequest
                        .caseWorkersLocationRequest()
                        .location("updated added new location")
                        .locationId(1).isPrimaryFlag(false).build());

        List<CaseWorkerRoleRequest> roleRequests = List.of(
                CaseWorkerRoleRequest
                        .caseWorkerRoleRequest()
                        .role("Senior Legal Caseworker")
                        .isPrimaryFlag(true).build(),
                CaseWorkerRoleRequest
                        .caseWorkerRoleRequest()
                        .role("Legal Caseworker")
                        .isPrimaryFlag(false).build());


        List<CaseWorkerWorkAreaRequest> areaRequests = List.of(
                CaseWorkerWorkAreaRequest
                        .caseWorkerWorkAreaRequest()
                        .serviceCode("BAA1")
                        .areaOfWork("Non-Money Claims").build(),
                CaseWorkerWorkAreaRequest
                        .caseWorkerWorkAreaRequest()
                        .serviceCode("BAA9")
                        .areaOfWork("Possession Claims").build());

        Set<String> idamRoles = new HashSet<>();

        String emailToUsed = isNotEmpty(email) && nonNull(email[0]) ? email[0] : generateRandomEmail();
        setEmailsTobeDeleted(emailToUsed);
        return ImmutableList.of(
                CaseWorkersProfileCreationRequest
                        .caseWorkersProfileCreationRequest()
                        .firstName("updatedFirstName")
                        .lastName("updatedLastName")
                        .emailId(emailToUsed)
                        .regionId(2)
                        .region("County")
                        .userType("Future Operations")
                        .suspended(false)
                        .idamRoles(idamRoles)
                        .baseLocations(locationRequestList)
                        .roles(roleRequests)
                        .workerWorkAreaRequests(areaRequests).build());
    }

    public Response createUserProfiles(List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests) {
        Response response = getMultipleAuthHeadersInternal()
                .body(caseWorkersProfileCreationRequests)
                .post("/refdata/case-worker/users")
                .andReturn();
        log.info(":: Create user profile response status code :: " + response.statusCode());

        response.then()
                .assertThat()
                .statusCode(201);

        return response;
    }

    public Response deleteCaseworkerByIdOrEmailPattern(String path, HttpStatus statusCode) {
        Response response = getMultipleAuthHeadersInternal()
                .delete(path)
                .andReturn();

        log.info(":: delete user profile response status code :: " + response.statusCode());

        response.then()
                .assertThat()
                .statusCode(statusCode.value());

        return response;
    }

    public Object fetchUserType(HttpStatus expectedStatus) {
        Response response = getMultipleAuthHeadersInternal(ROLE_STAFF_ADMIN)
                .get("/refdata/case-worker/user-type")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());
        return response.getBody().as(StaffRefDataUserTypesResponse.class);
    }

}