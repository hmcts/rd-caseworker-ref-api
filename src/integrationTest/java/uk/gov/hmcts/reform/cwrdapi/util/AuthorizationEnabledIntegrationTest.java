package uk.gov.hmcts.reform.cwrdapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.sdk.server.LDClient;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.cwrdapi.client.domain.AttributeResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.cwrdapi.client.domain.UserProfileRolesResponse;
import uk.gov.hmcts.reform.cwrdapi.config.RestTemplateConfiguration;
import uk.gov.hmcts.reform.cwrdapi.config.TestConfig;
import uk.gov.hmcts.reform.cwrdapi.repository.CaseWorkerIdamRoleAssociationRepository;
import uk.gov.hmcts.reform.cwrdapi.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.cwrdapi.servicebus.TopicPublisher;
import uk.gov.hmcts.reform.cwrdapi.wiremock.WireMockExtension;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Configuration
@TestPropertySource(properties = {
    "USER_PROFILE_URL:http://127.0.0.1:8091",
    "spring.config.location=classpath:application-test.yml",
})
@ContextConfiguration(classes = {TestConfig.class, RestTemplateConfiguration.class})
public abstract class AuthorizationEnabledIntegrationTest extends SpringBootIntegrationTest {

    @MockitoBean
    protected FeatureToggleServiceImpl featureToggleServiceImpl;

    @MockitoBean
    protected TopicPublisher topicPublisher;

    @MockitoBean
    LDClient ldClient;

    @Autowired
    protected CaseWorkerReferenceDataClient caseworkerReferenceDataClient;

    @RegisterExtension
    public static WireMockExtension userProfileService = new WireMockExtension(8091);

    @Value("${crd.security.roles.cwd-admin}")
    public String cwdAdmin;

    @Value("${oidc.issuer}")
    private String issuer;

    @Value("${oidc.expiration}")
    private long expiration;

    @MockitoBean
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    protected CaseWorkerIdamRoleAssociationRepository roleAssocRepository;

    @Autowired
    Flyway flyway;

    @BeforeEach
    public void setUpClient() {
        when(featureToggleServiceImpl.isFlagEnabled(anyString(), anyString())).thenReturn(true);
        doNothing().when(topicPublisher).sendMessage(any());
        flyway.clean();
        flyway.migrate();
    }

    public void userProfileGetUserWireMock(String idamStatus, String roles) throws JsonProcessingException {
        userProfileService.stubFor(get(urlPathMatching("/v1/userprofile.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "close")
                        .withStatus(200)
                        .withBody(getObjectMapper().writeValueAsString(
                                getUserIdentifierData(idamStatus, roles))))
        );
    }

    public void userProfileGetUserByIdWireMock(String idamId, Integer status) throws JsonProcessingException {
        userProfileService.stubFor(get(urlPathMatching("/v1/userprofile.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "close")
                        .withStatus(status)
                        .withBody(getObjectMapper().writeValueAsString(
                                getUserIdentifierData("pending", "%s"))))
        );
    }

    private UserIdentifier getUserIdentifierData(String idamStatus, String roles) {
        return UserIdentifier.builder()
                .userIdentifier(UUID.randomUUID().toString())
                .firstName("prashanth")
                .lastName("rao")
                .email("super.user@hmcts.net")
                .idamStatus(idamStatus)
                .roles(List.of(roles))
                .build();
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

    public static void userProfilePostUserWireMockForStaffProfile(HttpStatus status) {
        userProfileService.stubFor(post(urlPathEqualTo("/v1/userprofile"))
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
}
