package uk.gov.hmcts.reform.cwrdapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.JOSEException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.ContextCleanupListener;
import uk.gov.hmcts.reform.cwrdapi.config.WireMockExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

@Configuration
@TestPropertySource(properties = {"OPEN_ID_API_BASE_URI:http://0.0.0.0:6000/o"})
public class IntegrationTestOidcSecurityConfig extends ContextCleanupListener {

    public static WireMockExtension mockHttpServerForOidc = new WireMockExtension(6000);

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() throws JsonProcessingException, JOSEException {
        setUpMockServiceForOidc();
        return new InMemoryClientRegistrationRepository(clientRegistration());
    }

    private ClientRegistration clientRegistration() {
        return ClientRegistration.withRegistrationId("oidc")
                .redirectUriTemplate("{baseUrl}/{action}/oauth2/code/{registrationId}")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope("read:user")
                .authorizationUri("http://0.0.0.0:6000/o/authorize")
                .tokenUri("http://0.0.0.0:6000/o/access_token")
                .userInfoUri("http://0.0.0.0:6000/o/userinfo")
                .userNameAttributeName("id")
                .clientName("Client Name")
                .clientId("client-id")
                .clientSecret("client-secret")
                .build();
    }


    public void setUpMockServiceForOidc() throws JsonProcessingException, JOSEException {

        mockHttpServerForOidc.stubFor(get(urlPathMatching("/o/.well-known/openid-configuration"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Connection", "close")
                        .withBody(
                                "  {"
                                + "  \"issuer\": \"http://0.0.0.0:6000/o\","
                                + "  \"jwks_uri\": \"http://0.0.0.0:7000/jwks\" "
                                + "}")));

        if (!mockHttpServerForOidc.isRunning()) {
            mockHttpServerForOidc.start();
        }

    }
}




