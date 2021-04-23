package uk.gov.hmcts.reform.cwrdapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.launchdarkly.sdk.server.LDClient;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.flywaydb.core.Flyway;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.cwrdapi.client.domain.AttributeResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.config.RestTemplateConfiguration;
import uk.gov.hmcts.reform.cwrdapi.config.TestConfig;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;

import java.util.LinkedList;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.JwtTokenUtil.decodeJwtToken;
import static uk.gov.hmcts.reform.cwrdapi.util.JwtTokenUtil.getUserIdAndRoleFromToken;
import static uk.gov.hmcts.reform.cwrdapi.util.KeyGenUtil.getDynamicJwksResponse;

@Configuration
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990", "IDAM_URL:http://127.0.0.1:5000",
    "USER_PROFILE_URL:http://127.0.0.1:8091", "spring.config.location=classpath:application-test.yml"})
@ContextConfiguration(classes = {TestConfig.class, RestTemplateConfiguration.class})
public abstract class AuthorizationEnabledIntegrationTest extends SpringBootIntegrationTest {

    @MockBean
    protected FeatureToggleServiceImpl featureToggleServiceImpl;

    @MockBean
    protected TopicPublisher topicPublisher;

    @MockBean
    LDClient ldClient;

    @Autowired
    protected CaseWorkerReferenceDataClient caseworkerReferenceDataClient;

    @ClassRule
    public static WireMockRule s2sService = new WireMockRule(wireMockConfig().port(8990));

    @ClassRule
    public static WireMockRule userProfileService = new WireMockRule(wireMockConfig().port(8091));

    @ClassRule
    public static WireMockRule sidamService = new WireMockRule(wireMockConfig().port(5000)
        .extensions(new CaseWorkerTransformer()));

    @ClassRule
    public static WireMockRule mockHttpServerForOidc = new WireMockRule(wireMockConfig().port(7000));

    @Value("${crd.security.roles.cwd-admin}")
    public String cwdAdmin;

    @Value("${oidc.issuer}")
    private String issuer;

    @Value("${oidc.expiration}")
    private long expiration;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    protected CaseWorkerIdamRoleAssociationRepository roleAssocRepository;

    @Autowired
    Flyway flyway;

    @Before
    public void setUpClient() {
        when(featureToggleServiceImpl.isFlagEnabled(anyString(), anyString())).thenReturn(true);
        doNothing().when(topicPublisher).sendMessage(any());
        flyway.clean();
        flyway.migrate();
    }

    @Before
    public void setupIdamStubs() throws Exception {

        s2sService.stubFor(get(urlEqualTo("/details"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                    .withHeader("Connection", "close")
                .withBody("rd_caseworker_ref_api")));

        sidamService.stubFor(get(urlPathMatching("/o/userinfo"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                    .withHeader("Connection", "close")
                .withBody("{"
                    + "  \"id\": \"%s\","
                    + "  \"uid\": \"%s\","
                    + "  \"forename\": \"Super\","
                    + "  \"surname\": \"User\","
                    + "  \"email\": \"super.user@hmcts.net\","
                    + "  \"accountStatus\": \"active\","
                    + "  \"roles\": ["
                    + "  \"%s\""
                    + "  ]"
                    + "}")
                .withTransformers("user-token-response")));

        mockHttpServerForOidc.stubFor(get(urlPathMatching("/jwks"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                    .withHeader("Connection", "close")
                .withBody(getDynamicJwksResponse())));
    }

    public void userProfileGetUserWireMock(String idamStatus, String roles) {
        userProfileService.stubFor(get(urlPathMatching("/v1/userprofile.*"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                    .withHeader("Connection", "close")
                .withStatus(200)
                .withBody("{"
                    + "  \"userIdentifier\":\"" + UUID.randomUUID().toString() + "\","
                    + "  \"firstName\": \"prashanth\","
                    + "  \"lastName\": \"rao\","
                    + "  \"email\": \"super.user@hmcts.net\","
                    + "  \"idamStatus\": \"" + idamStatus + "\","
                    + "  \"roles\": " + roles
                    + "}")));
    }

    public void modifyUserRoles() throws Exception {

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        RoleAdditionResponse roleAdditionRes = new RoleAdditionResponse();
        roleAdditionRes.setIdamStatusCode("201");
        roleAdditionRes.setIdamMessage("Success");
        userProfileRolesResponse.setRoleAdditionResponse(roleAdditionRes);

        userProfileService.stubFor(put(urlPathMatching("/v1/userprofile.*"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                    .withHeader("Connection", "close")
                .withStatus(201)
                .withBody(objectMapper.writeValueAsString(userProfileRolesResponse))));
    }

    public void modifyUserStatus(int idamStatus) throws Exception {

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();

        AttributeResponse attributeResponse = new AttributeResponse();
        attributeResponse.setIdamStatusCode(idamStatus);
        userProfileRolesResponse.setAttributeResponse(attributeResponse);

        userProfileService.stubFor(put(urlPathMatching("/v1/userprofile.*"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                    .withHeader("Connection", "close")
                .withStatus(201)
                .withBody(objectMapper.writeValueAsString(userProfileRolesResponse))));
    }

    //removed UUID mock here and put in Test config,hence use this only for insert integration testing
    //for update use insert response UUID in test or other mock methods
    @Before
    public void userProfilePostUserWireMock() {
        userProfileService.stubFor(post(urlPathMatching("/v1/userprofile"))
            .inScenario("")
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                    .withHeader("Connection", "close")
                .withStatus(201)
                .withBody("{"
                    + "  \"idamRegistrationResponse\":\"201\""
                    + "}")));
    }

    @After
    public void cleanupTestData() {
    }


    //removed UUID mock here and put in Test config,hence use this only for insert integration testing
    //for update use insert response UUID in test or other mock methods
    public void userProfileCreateUserWireMock(HttpStatus status) {

        userProfileService.stubFor(post(urlPathMatching("/v1/userprofile"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                    .withHeader("Connection", "close")
                .withStatus(status.value())
                .withBody("{"
                    + "  \"idamRegistrationResponse\":\"" + status.value() + "\""
                    + "}")));
    }

    public static class CaseWorkerTransformer extends ResponseTransformer {
        @Override
        public Response transform(Request request, Response response, FileSource files, Parameters parameters) {

            String formatResponse = response.getBodyAsString();

            String token = request.getHeader("Authorization");
            String tokenBody = decodeJwtToken(token.split(" ")[1]);
            LinkedList tokenInfo = getUserIdAndRoleFromToken(tokenBody);
            formatResponse = format(formatResponse, tokenInfo.get(1), tokenInfo.get(1), tokenInfo.get(0));

            return Response.Builder.like(response)
                .but().body(formatResponse)
                .build();
        }

        @Override
        public String getName() {
            return "user-token-response";
        }

        public boolean applyGlobally() {
            return false;
        }
    }
}

