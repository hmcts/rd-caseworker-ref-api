package uk.gov.hmcts.reform.cwrdapi.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import uk.gov.hmcts.reform.cwrdapi.util.UserIdentifier;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
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
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(getUserDetailsJson())
                                        .withTransformers("user-token-response")
                        )
        );
    }

    private static String getUserDetailsJson() {
        try {
            return getObjectMapper().writeValueAsString(
                    UserIdentifier.builder()
                            .id("%s")
                            .uid("%s")
                            .forename("Super")
                            .surname("User")
                            .email("super.user@hmcts.net")
                            .accountStatus("active")
                            .roles(List.of("%s"))
                            .build()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create IDAM userinfo response", e);
        }
    }
}
