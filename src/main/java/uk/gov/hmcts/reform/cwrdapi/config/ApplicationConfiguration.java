package uk.gov.hmcts.reform.cwrdapi.config;

import feign.Feign;
import feign.jackson.JacksonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

@Configuration
@Slf4j
public class ApplicationConfiguration {

    private final String s2sSecret;
    private final String s2sMicroService;
    private final String s2sUrl;

    public ApplicationConfiguration(@Value("${idam.s2s-auth.totp_secret}") String s2sSecret,
                                    @Value("${idam.s2s-auth.microservice}") String s2sMicroService,
                                    @Value("${idam.s2s-auth.url}") String s2sUrl) {
        this.s2sSecret = s2sSecret;
        this.s2sMicroService = s2sMicroService;
        this.s2sUrl = s2sUrl;
    }

    @Bean
    public ServiceAuthorisationApi generateServiceAuthorisationApi(@Value("${idam.s2s-auth.url}") final String s2sUrl) {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .contract(new SpringMvcContract())
                .target(ServiceAuthorisationApi.class, s2sUrl);
    }

    @Bean
    public ServiceAuthTokenGenerator authTokenGenerator(
            @Value("${idam.s2s-auth.totp_secret}") final String secret,
            @Value("${idam.s2s-auth.microservice}") final String microService,
            final ServiceAuthorisationApi serviceAuthorisationApi) {
        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }

    public String getS2sSecret() {
        return s2sSecret;
    }

    public String getS2sMicroService() {
        return s2sMicroService;
    }

    public String getS2sUrl() {
        return s2sUrl;
    }
}
