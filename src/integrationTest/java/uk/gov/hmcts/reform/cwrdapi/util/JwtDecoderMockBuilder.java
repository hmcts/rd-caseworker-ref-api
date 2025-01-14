package uk.gov.hmcts.reform.cwrdapi.util;


import org.springframework.security.oauth2.jwt.JwtDecoder;

public class JwtDecoderMockBuilder extends AuthorizationEnabledIntegrationTest {

    public void resetJwtDecoder() {
    }

    public synchronized JwtDecoder getJwtDecoder() {
        return null;
    }
}
