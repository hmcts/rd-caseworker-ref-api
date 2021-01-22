package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SecurityUtilsTest {

    @Mock
    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);

    @InjectMocks
    private final SecurityUtils securityUtils = new SecurityUtils(
            authTokenGenerator
    );

    private final String serviceAuthorization = "Bearer eyJhbGciOiJIUzUxMiJ9"
            + ".eyJzdWIiOiJjY2RfZ3ciLCJleHAiOjE1OTQ2ODQ5MTF9"
            + ".LH3aiNniHNMlTwuSdzgRic9sD_4inQv5oUqJ0kkRKVasS4RfhIz2tRdttf-sSMkUga1p1teOt2iCq4BQBDS7KA";

    @Test
    void getServiceAuthorizationHeader() {
        String result = securityUtils.getServiceAuthorizationHeader();
        assertNotNull(result);
        assertTrue(result.contains(serviceAuthorization));
    }
}
