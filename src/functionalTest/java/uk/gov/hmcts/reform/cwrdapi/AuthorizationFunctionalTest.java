package uk.gov.hmcts.reform.cwrdapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.cwrdapi.client.CaseWorkerApiClient;
import uk.gov.hmcts.reform.cwrdapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.config.Oauth2;
import uk.gov.hmcts.reform.cwrdapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.idam.IdamOpenIdClient;
import uk.gov.hmcts.reform.lib.client.response.S2sClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@ContextConfiguration(classes = {TestConfigProperties.class, Oauth2.class, FuncTestRequestHandler.class})
@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
public class AuthorizationFunctionalTest {

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;

    @Value("${s2s-secret}")
    protected String s2sSecret;

    @Value("${targetInstance}")
    protected String caseWorkerApiUrl;

    protected static CaseWorkerApiClient caseWorkerApiClient;

    protected static IdamOpenIdClient idamOpenIdClient;

    @Autowired
    protected TestConfigProperties configProperties;


    protected static String s2sToken;

    public static final String EMAIL = "EMAIL";
    public static final String CREDS = "CREDS";
    public static final String EMAIL_TEMPLATE = "CWR-rd-func-test-user-only-%s@justice.gov.uk";
    public static final String CWD_USER = "cwd-user";
    public static final String CASEWORKER_IAC_BULKSCAN = "caseworker-iac-bulkscan";
    public static final String CASEWORKER_CIVIL = "caseworker-civil";
    public static final String CASEWORKER_CIVIL_STAFF = "caseworker-civil-staff";
    public static final String USER_STATUS_SUSPENDED = "SUSPENDED";
    public static final String ROLE_CWD_ADMIN = "cwd-admin";
    public static final String PRD_ADMIN = "prd-admin";

    public static final String ROLE_STAFF_ADMIN = "staff-admin";
    public static final String ROLE_CWD_SYSTEM_USER = "cwd-system-user";
    public static final String STAFF_EMAIL_TEMPLATE = "staff-rd-profile-func-test-user-only-%s@justice.gov.uk";
    public static final String STAFF_EMAIL_PATTERN = "staff-rd-profile-func-test-user-only";
    public static final String CWR_EMAIL_PATTERN = "cwr-rd-func-test-user-only";

    @Autowired
    public FuncTestRequestHandler funcTestRequestHandler;

    public static List<String> emailsTobeDeleted = new ArrayList<>();

    @Value("${userProfUrl}")
    protected String baseUrlUserProfile;

    @Autowired
    protected TestConfigProperties testConfigProperties;

    @PostConstruct
    public void beforeTestClass() {

        SerenityRest.useRelaxedHTTPSValidation();

        if (null == s2sToken) {
            log.info(":::: Generating S2S Token");
            s2sToken = new S2sClient(
                    testConfigProperties.getS2sUrl(),
                    testConfigProperties.getS2sName(),
                    testConfigProperties.getS2sSecret())
                    .signIntoS2S();
        }

        if (null == idamOpenIdClient) {
            idamOpenIdClient = new IdamOpenIdClient(testConfigProperties);
        }

        caseWorkerApiClient = new CaseWorkerApiClient(
                caseWorkerApiUrl,
                s2sToken, idamOpenIdClient);
    }

    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10)).toLowerCase();
    }

    public static String getS2sToken() {
        return s2sToken;
    }

    public static void setEmailsTobeDeleted(String emailTobeDeleted) {
        emailsTobeDeleted.add(emailTobeDeleted);
    }

    public static List<CaseWorkersProfileCreationRequest> createNewActiveCaseWorkerProfile() {
        Map<String, String> userDetail = idamOpenIdClient.createUser(CASEWORKER_IAC_BULKSCAN);
        String userEmail = userDetail.get(EMAIL);

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles(userEmail);

        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        return caseWorkersProfileCreationRequests;
    }

    public List getUserProfilesFromCw(
            UserRequest userRequest, int expectedResponse) {
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal(ROLE_CWD_SYSTEM_USER)
                .body(userRequest).log().body(true)
                .post("/refdata/case-worker/users/fetchUsersById/")
                .then()
                .log().body(true)
                .and()
                .extract().response();

        log.info("CW get user response: {}", fetchResponse.getStatusCode());

        fetchResponse.then()
                .assertThat()
                .statusCode(expectedResponse);
        return asList(fetchResponse.getBody().as(
                uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile[].class));
    }

    public UserProfileResponse getUserProfileFromUp(String email) {
        return funcTestRequestHandler.sendGet(OK,
                "/v1/userprofile/roles", UserProfileResponse.class, baseUrlUserProfile,
                Map.of("UserEmail", email));
    }

    public static Map getIdamResponse(String idamId) {
        idamOpenIdClient.getcwdAdminOpenIdToken("cwd-admin");
        return idamOpenIdClient.getUser(idamId);
    }

    public static void deleteCaseWorkerProfileByEmailPattern(String emailPattern) {
        //delete user by email pattern
        caseWorkerApiClient.deleteCaseworkerByIdOrEmailPattern(
                "/refdata/case-worker/users?emailPattern=" + emailPattern, NO_CONTENT);
    }

    public UserProfileResponse createUserProfileFromUp(UserProfileCreationRequest request)
            throws JsonProcessingException {
        return funcTestRequestHandler.sendPost(request,CREATED, "/v1/userprofile",baseUrlUserProfile,
                UserProfileResponse.class,idamOpenIdClient.getToken("cwd-admin"));
    }
}
