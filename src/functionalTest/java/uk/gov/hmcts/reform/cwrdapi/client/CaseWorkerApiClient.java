package uk.gov.hmcts.reform.cwrdapi.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerLocationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerServicesRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkerWorkAreaRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileUpdationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.LanguagePreference;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.StaffProfileRoleRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserCategory;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserTypeRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefDataUserTypesResponse;
import uk.gov.hmcts.reform.cwrdapi.controllers.response.StaffRefJobTitleResponse;
import uk.gov.hmcts.reform.cwrdapi.idam.IdamOpenIdClient;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.PRD_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.ROLE_CWD_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.ROLE_STAFF_ADMIN;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.STAFF_EMAIL_TEMPLATE;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.generateRandomEmail;
import static uk.gov.hmcts.reform.cwrdapi.AuthorizationFunctionalTest.setEmailsTobeDeleted;
import static uk.gov.hmcts.reform.lib.idam.IdamOpenId.EMAIL;


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

    public RequestSpecification getMultipleAuthHeadersInternal(List<String> roles) {
        return getMultipleAuthHeaders(idamOpenIdClient.getOpenIdTokenByRoles(roles));
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

    public RequestSpecification getMultipleAuthHeadersWithoutContentTypeWithPagination(String role,
                    String pageNumber, String pageSize) {

        String userToken = idamOpenIdClient.getOpenIdTokenByRole(role);
        return SerenityRest.with()
                .relaxedHTTPSValidation()
                .baseUri(caseWorkerApiUrl)
                .header("Accepts", APPLICATION_JSON_VALUE)
                .header(SERVICE_HEADER, "Bearer " + s2sToken)
                .header(AUTHORIZATION_HEADER, "Bearer " + userToken)
                .header("page-number", pageNumber)
                .header("page-size", pageSize);
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

    public List<CaseWorkersProfileCreationRequest> createCaseWorkerProfiles(String firstName,
                                                                            String lastName,String email) {
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

        String emailToUsed =  nonNull(email) ? email : generateRandomEmail();
        setEmailsTobeDeleted(emailToUsed.toLowerCase());
        return ImmutableList.of(
                CaseWorkersProfileCreationRequest
                        .caseWorkersProfileCreationRequest()
                        .firstName(firstName)
                        .lastName(lastName)
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

    public StaffProfileCreationRequest createStaffProfileCreationRequest() {

        String emailPattern = "deleteTest1234";
        String email = format(STAFF_EMAIL_TEMPLATE, randomAlphanumeric(10) + emailPattern).toLowerCase();

        Set<String> roles = ImmutableSet.of(" tribunal_case_worker ");
        List<StaffProfileRoleRequest> caseWorkerRoleRequests =
                ImmutableList.of(StaffProfileRoleRequest.staffProfileRoleRequest()
                         .roleId(1)
                        .role("Senior Legal Caseworker")
                        .isPrimaryFlag(true).build());

        List<CaseWorkerLocationRequest> caseWorkerLocationRequests = ImmutableList.of(CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true).locationId(1)
                .location("location").build());

        List<CaseWorkerServicesRequest> caseWorkerServicesRequests = ImmutableList.of(CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .service("areaOfWork").serviceCode("serviceCode")
                .build());

        return   StaffProfileCreationRequest
                 .staffProfileCreationRequest()
                 .firstName("StaffProfilefirstName")
                 .lastName("StaffProfilelastName")
                 .emailId(email)
                 .regionId(1).userType("CTSC")
                 .region("region")
                 .suspended(false)
                 .taskSupervisor(true)
                 .caseAllocator(false)
                 .staffAdmin(false)
                 .roles(caseWorkerRoleRequests)
                 .baseLocations(caseWorkerLocationRequests)
                 .services(caseWorkerServicesRequests)
                 .build();
    }

    public StaffProfileCreationRequest createStaffProfileCreationRequest(
            String emailId, String firstName, String lastName) {

        Set<String> roles = ImmutableSet.of(" tribunal_case_worker ");
        List<StaffProfileRoleRequest> caseWorkerRoleRequests =
                ImmutableList.of(StaffProfileRoleRequest.staffProfileRoleRequest().roleId(1).role(" role ")
                        .isPrimaryFlag(true).build());

        List<CaseWorkerLocationRequest> caseWorkerLocationRequests = ImmutableList.of(CaseWorkerLocationRequest
                .caseWorkersLocationRequest()
                .isPrimaryFlag(true).locationId(1)
                .location(" location ").build());

        List<CaseWorkerServicesRequest> caseWorkerServicesRequests = ImmutableList.of(CaseWorkerServicesRequest
                .caseWorkerServicesRequest()
                .service(" areaOfWork ").serviceCode(" serviceCode ")
                .build());

        return   StaffProfileCreationRequest
                .staffProfileCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .emailId(emailId)
                .regionId(1).userType("CTSC")
                .region("region")
                .suspended(false)
                .taskSupervisor(true)
                .caseAllocator(false)
                .staffAdmin(false)
                .roles(caseWorkerRoleRequests)
                .baseLocations(caseWorkerLocationRequests)
                .services(caseWorkerServicesRequests)
                .build();
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

        return response;
    }

    public Object fetchJobTitles(HttpStatus expectedStatus) {
        Response response = getMultipleAuthHeadersInternal(ROLE_STAFF_ADMIN)
                .get("/refdata/case-worker/job-title")
                .andReturn();
        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());
        return response.getBody().as(StaffRefJobTitleResponse.class);
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

    /*
     * Create user in IDAM first and then in Caseworker/UserProfile
     * Add to the IDAM deletion list
     */
    public Response createStaffUserProfile(StaffProfileCreationRequest request) {

        List<String> userRoles = List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN);
        Map<String, String> users =  idamOpenIdClient.createUser(userRoles,request.getEmailId(),
                                        request.getFirstName(),request.getFirstName());
        setEmailsTobeDeleted(users.get(EMAIL).toLowerCase());

        Response response = getMultipleAuthHeadersInternal(userRoles)
                .body(request)
                .post("/refdata/case-worker/profile")
                .andReturn();
        log.info(":: Create staff profile response status code :: " + response.statusCode());

        response.then()
                .assertThat()
                .statusCode(201);

        return response;
    }

    public Response updateCaseWorkerProfile(CaseWorkersProfileUpdationRequest request) {

        Response response = getMultipleAuthHeadersInternal(PRD_ADMIN)
            .body(request)
            .put("/refdata/case-worker/users/sync")
            .andReturn();
        log.info(":: Update staff profile response status code :: " + response.statusCode());

        response.then()
            .assertThat()
            .statusCode(200);

        return response;
    }

    public Response updateStaffUserProfile(StaffProfileCreationRequest staffProfileCreationRequest) {

        Response response = getMultipleAuthHeadersInternal(List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN))
                .body(staffProfileCreationRequest)
                .put("/refdata/case-worker/profile")
                .andReturn();
        log.info(":: Create staff profile response status code :: " + response.statusCode());

        response.then()
                .assertThat()
                .statusCode(200);

        return response;
    }

    public UserProfileCreationRequest createUserProfileRequest(StaffProfileCreationRequest request) {

        return new UserProfileCreationRequest(
                request.getEmailId(),
                request.getFirstName(),
                request.getLastName(),
                LanguagePreference.EN,
                UserCategory.CASEWORKER,
                UserTypeRequest.INTERNAL,
                Set.of("staff-admin"),
                false);
    }

    public Response createStaffUserProfileWithOutIdm(StaffProfileCreationRequest request) {

        List<String> userRoles = List.of(ROLE_CWD_ADMIN,ROLE_STAFF_ADMIN);

        Response response = getMultipleAuthHeadersInternal(userRoles)
                .body(request)
                .post("/refdata/case-worker/profile")
                .andReturn();
        log.info(":: Create staff profile response status code :: " + response.statusCode());

        response.then()
                .assertThat()
                .statusCode(201);

        return response;
    }

}