package uk.gov.hmcts.reform.cwrdapi.util;


import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.JwtDecoder;


public class JwtDecoderMockBuilder {
    private final JwtDecoder jwtDecoder;

    public JwtDecoderMockBuilder(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public void resetJwtDecoder() {
        Mockito.reset(jwtDecoder);
    }


}
