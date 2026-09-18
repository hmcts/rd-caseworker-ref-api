package uk.gov.hmcts.reform.cwrdapi.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import uk.gov.hmcts.reform.cwrdapi.util.UserIdentifier;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static uk.gov.hmcts.reform.cwrdapi.util.SpringBootIntegrationTest.getObjectMapper;

public final class IdamWireMockStubs {

    private IdamWireMockStubs() {
    }

    public static void registerDefaults(WireMockServer server) {

        server.stubFor(
                get(urlPathEqualTo("/o/userinfo"))
                        .atPriority(10)
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody(
                                                getUserDetailsJson(
                                                        "active",
                                                        List.of("%s")
                                                )
                                        )
                        )
        );
    }

    public static StubMapping stubIdamWithInvalidRole(WireMockServer server) {
        try {

            return server.stubFor(get(urlPathMatching("/o/userinfo.*"))
                    .atPriority(1)
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withHeader("Connection", "close")
                            .withBody(getUserDetailsJson("active", List.of("invalid-role")))
                            .withTransformers("user-token-response")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static StubMapping stubIdamWithGivenRoleAndStatus(WireMockServer server, String status, List<String> roles) {
        try {

            return server.stubFor(get(urlPathMatching("/o/userinfo.*"))
                    .atPriority(1)
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withHeader("Connection", "close")
                            .withBody(getUserDetailsJson(status, roles))
                            .withTransformers("user-token-response")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getUserDetailsJson(String status, List<String> roles) {
        try {
            return getObjectMapper().writeValueAsString(
                    UserIdentifier.builder()
                            .id("%s")
                            .uid("%s")
                            .forename("Super")
                            .surname("User")
                            .email("super.user@hmcts.net")
                            .accountStatus(status)
                            .roles(roles)
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to create IDAM userinfo response", e
            );
        }
    }
}
