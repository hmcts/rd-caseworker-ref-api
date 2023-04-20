package uk.gov.hmcts.reform.cwrdapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.launchdarkly.sdk.server.LDClient;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.cwrdapi.client.domain.AttributeResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.config.RestTemplateConfiguration;
import uk.gov.hmcts.reform.cwrdapi.config.TestConfig;
import uk.gov.hmcts.reform.cwrdapi.config.WireMockExtension;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.lang.String.format;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.cwrdapi.util.JwtTokenUtil.decodeJwtToken;
import static uk.gov.hmcts.reform.cwrdapi.util.JwtTokenUtil.getUserIdAndRoleFromToken;
import static uk.gov.hmcts.reform.cwrdapi.util.KeyGenUtil.getDynamicJwksResponse;

@Configuration
@SerenityTest
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

    @RegisterExtension
    public static WireMockExtension s2sService = new WireMockExtension(8990);


    @RegisterExtension
    public static WireMockExtension userProfileService = new WireMockExtension(8091);

    @RegisterExtension
    public static WireMockExtension sidamService = new WireMockExtension(5000, new CaseWorkerTransformer());

    @RegisterExtension
    public static WireMockExtension mockHttpServerForOidc = new WireMockExtension(7000);

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

    @MockBean
    protected static JwtDecoder jwtDecoder;

    @BeforeEach
    public void setUpClient() {
        when(featureToggleServiceImpl.isFlagEnabled(anyString(), anyString())).thenReturn(true);
        doNothing().when(topicPublisher).sendMessage(any());
        flyway.clean();
        flyway.migrate();
    }

    @BeforeEach
    public void setupIdamStubs() throws Exception {

        s2sService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "close")
                        .withBody("rd_caseworker_ref_api")));


        UserInfo userDetails = UserInfo.builder()
                .id("%s")
                .uid("%s")
                .forename("Super")
                .surname("User")
                .email("super.user@hmcts.net")
                .accountStatus("active")
                .roles(List.of("%s"))
                .build();

        sidamService.stubFor(get(urlPathMatching("/o/userinfo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "close")
                        .withBody(WireMockUtil.getObjectMapper()
                                .writeValueAsString(userDetails))
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

    public void userProfileGetUserByIdWireMock(String idamId, Integer status) {
        userProfileService.stubFor(get(urlPathMatching("/v1/userprofile.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "close")
                        .withStatus(status)
                        .withBody("{"
                                + "  \"userIdentifier\":\"" + UUID.randomUUID().toString() + "\","
                                + "  \"firstName\": \"prashanth\","
                                + "  \"lastName\": \"rao\","
                                + "  \"email\": \"super.user@hmcts.net\","
                                + "  \"idamStatus\": \"pending\","
                                + "  \"roles\": ["
                                + "  \"%s\""
                                + "  ]"
                                + "}")));
    }


    public void userProfileDeleteUserWireMock() {
        userProfileService.stubFor(delete(urlPathMatching("/v1/userprofile/users.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(204)
                        .withBody("{"
                                + "  \"response\": \"UserProfile Successfully Deleted.\""
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
    @BeforeEach
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

    @AfterEach
    public void cleanupTestData() {
        JwtDecoderMockBuilder.resetJwtDecoder();
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

    public void userProfilePostUserWireMockForStaffProfile(HttpStatus status) {
        userProfileService.stubFor(post(urlPathMatching("/v1/userprofile"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(201)
                        .withBody("{"
                                + "  \"idamId\":\"" + UUID.randomUUID().toString() + "\","
                                + "  \"idamRegistrationResponse\":\"" + status.value() + "\""
                                + "}")));
    }

    public void userProfilePostUserWireMockForStaffProfile(boolean resend) {
        userProfileService.stubFor(post(urlPathMatching("/v1/userprofile"))
                .withRequestBody(equalToJson("{ \"resendInvite\": " + resend + "}", true,
                        true))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(201)
                        .withBody("{"
                                + "  \"idamId\":\"" + UUID.randomUUID() + "\","
                                + "  \"idamRegistrationResponse\":\"" + 201 + "\""
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

