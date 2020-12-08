package uk.gov.hmcts.reform.cwrdapi.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Getter
@Setter
@Configuration
public class TestConfigProperties {


    @Value("${oauth2.client.secret}")
    public String clientSecret;

    @Value("${test.user.password}")
    public String testUserPassword;

    @Value("${idam.api.url}")
    public String idamApiUrl;

    @Value("${idam.auth.redirectUrl}")
    public String oauthRedirectUrl;

    @Value("${idam.auth.clientId:xuiwebapp}")
    public String clientId;

    @Bean
    public ObjectMapper defaultObjectMapper() {
        return new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new ParameterNamesModule(JsonCreator.Mode.DEFAULT))
            .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }



}
