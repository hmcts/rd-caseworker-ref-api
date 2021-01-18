package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import uk.gov.hmcts.reform.cwrdapi.client.CaseWorkerApiClient;
import uk.gov.hmcts.reform.cwrdapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.cwrdapi.client.S2sClient;
import uk.gov.hmcts.reform.cwrdapi.client.response.UserProfileResponse;
import uk.gov.hmcts.reform.cwrdapi.config.Oauth2;
import uk.gov.hmcts.reform.cwrdapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.cwrdapi.controllers.request.CaseWorkersProfileCreationRequest;
import uk.gov.hmcts.reform.cwrdapi.idam.IdamOpenIdClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.springframework.http.HttpStatus.OK;

@ContextConfiguration(classes = {TestConfigProperties.class, Oauth2.class, FuncTestRequestHandler.class})
@ComponentScan("uk.gov.hmcts.reform.cwrdapi")
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
@TestExecutionListeners(listeners = {
        AuthorizationFunctionalTest.class,
        DependencyInjectionTestExecutionListener.class})
public class AuthorizationFunctionalTest extends AbstractTestExecutionListener {

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
    public static final String EMAIL_TEMPLATE = "CWR-func-test-user-%s@cwrfunctestuser.com";
    public static final String CWD_USER = "cwd-user";
    public static final String CASEWORKER_IAC_BULKSCAN = "caseworker-iac-bulkscan";
    public static final String CASEWORKER_IAC = "caseworker-iac";
    public static final String USER_STATUS_SUSPENDED = "SUSPENDED";

    @Autowired
    public FuncTestRequestHandler funcTestRequestHandler;

    public static List<String> emailsTobeDeleted = new ArrayList<>();

    @Value("${userProfUrl}")
    protected String baseUrlUserProfile;

    @Override
    public void beforeTestClass(TestContext testContext) {
        testContext.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(this);

        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.defaultParser = Parser.JSON;

        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sName);
        log.info("Configured S2S URL: " + s2sUrl);

        idamOpenIdClient = new IdamOpenIdClient(configProperties);

        //Single S2S & Sidam call
        s2sToken = isNull(s2sToken) ? new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S() : s2sToken;

        caseWorkerApiClient = new CaseWorkerApiClient(
                caseWorkerApiUrl,
                s2sToken, idamOpenIdClient);
    }

    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10)).toLowerCase();
    }


    @AfterClass
    public static void destroy() {
        emailsTobeDeleted.forEach(email -> {
            idamOpenIdClient.deleteSidamUser(email);
        });
    }

    public static String getS2sToken() {
        return s2sToken;
    }

    public static void setEmailsTobeDeleted(String emailTobeDeleted) {
        emailsTobeDeleted.add(emailTobeDeleted);
    }

    public List<CaseWorkersProfileCreationRequest> createNewActiveCaseWorkerProfile() {
        Map<String, String> userDetail = idamOpenIdClient.createUser(CASEWORKER_IAC_BULKSCAN);
        String userEmail = userDetail.get(EMAIL);

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles(userEmail);

        caseWorkerApiClient.createUserProfiles(caseWorkersProfileCreationRequests);

        return caseWorkersProfileCreationRequests;
    }

    public List<uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile> getUserProfilesFromCw(
            List<String> caseWorkerIds, int expectedResponse) {
        Response fetchResponse = caseWorkerApiClient.getMultipleAuthHeadersInternal()
                .body(caseWorkerIds).log().body(true)
                .post("/refdata/case-worker/users/fetchUsersById/")
                .then()
                .log().body(true)
                .and()
                .extract().response();

        log.info("CW get user response: ", fetchResponse.getStatusCode());

        fetchResponse.then()
                .assertThat()
                .statusCode(expectedResponse);
        return asList(fetchResponse.getBody().as(
                uk.gov.hmcts.reform.cwrdapi.client.domain.CaseWorkerProfile[].class));
    }

    public UserProfileResponse getUserProfileFromUp(String email) {
        return funcTestRequestHandler.sendGet(OK,
                "/v1/userprofile/roles?email=" + email, UserProfileResponse.class, baseUrlUserProfile);
    }

}