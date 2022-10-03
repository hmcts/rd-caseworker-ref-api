package uk.gov.hmcts.reform.cwrdapi;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.AfterAll;
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
import uk.gov.hmcts.reform.cwrdapi.controllers.request.UserRequest;
import uk.gov.hmcts.reform.cwrdapi.idam.IdamOpenIdClient;
import uk.gov.hmcts.reform.lib.client.response.S2sClient;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
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
    public static String[] EMAIL_TEMPLATE;
    public static final String CWD_USER = "cwd-user";
    public static final String CASEWORKER_IAC_BULKSCAN = "caseworker-iac-bulkscan";
    public static final String CASEWORKER_IAC = "caseworker-iac";
    public static final String CASEWORKER_SENIOR_IAC = "caseworker-senior-iac";
    public static final String USER_STATUS_SUSPENDED = "SUSPENDED";
    public static final String ROLE_CWD_ADMIN = "cwd-admin";
    public static final String ROLE_CWD_SYSTEM_USER = "cwd-system-user";

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

    public static String generateRandomEmail(int domainIndex) {
        return String.format(EMAIL_TEMPLATE[domainIndex], randomAlphanumeric(10)).toLowerCase();
    }


    @Value("${email.domainList}")
    public void setNameStatic(String emailDomainList) {
        List<String> emailArray = new ArrayList();
        if (ObjectUtils.isNotEmpty(emailDomainList)) {
            String[] domainArray = convertDomainStringIntoArray(emailDomainList);
            for (int index = 0; index < domainArray.length; index++) {
                emailArray.add("CWR-func-test-user-%s@" + domainArray[index]);
            }
        }
        EMAIL_TEMPLATE = emailArray.toArray(new String[0]);
        log.debug("Email Template =>" + EMAIL_TEMPLATE);
    }

    private static String[] convertDomainStringIntoArray(String emailDomainList) {
        return emailDomainList.split(",");
    }


    @AfterAll
    public static void destroy() {
        emailsTobeDeleted.forEach(email -> idamOpenIdClient.deleteSidamUser(email));
        log.info("delete idam user called");
    }

    public static String getS2sToken() {
        return s2sToken;
    }

    public static void setEmailsTobeDeleted(String emailTobeDeleted) {
        emailsTobeDeleted.add(emailTobeDeleted);
    }

    public List<CaseWorkersProfileCreationRequest> createNewActiveCaseWorkerProfile(int domainIndex) {
        Map<String, String> userDetail = idamOpenIdClient.createUser(CASEWORKER_IAC_BULKSCAN);
        String userEmail = userDetail.get(EMAIL);

        List<CaseWorkersProfileCreationRequest> caseWorkersProfileCreationRequests = caseWorkerApiClient
                .createCaseWorkerProfiles(domainIndex, userEmail);

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
        return idamOpenIdClient.getUser(idamId);
    }
}
