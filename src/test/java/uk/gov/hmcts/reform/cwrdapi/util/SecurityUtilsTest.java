package uk.gov.hmcts.reform.cwrdapi.util;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SecurityUtilsTest {

    @Mock
    private final AuthTokenGenerator authTokenGenerator = mock(AuthTokenGenerator.class);

    private final SecurityUtils securityUtils = new SecurityUtils(
            authTokenGenerator
    );

    @Test
    public void getServiceAuthorizationHeader() {
        String serviceAuthorization = "Bearer eyJhbGciOiA";
        when(authTokenGenerator.generate()).thenReturn(serviceAuthorization);
        String result = securityUtils.getServiceAuthorizationHeader();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains(serviceAuthorization));
    }
}
