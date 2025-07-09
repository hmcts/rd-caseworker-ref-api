package uk.gov.hmcts.reform.cwrdapi.util;


import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
public class JwtDecoderMockBuilder extends AuthorizationEnabledIntegrationTest {
    @Autowired
    JwtDecoder jwtDecoder;

    public  void resetJwtDecoder() {
        Mockito.reset(jwtDecoder);
    }

}
