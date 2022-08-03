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
import uk.gov.hmcts.reform.lib.config.TestConfig;

@Getter
@Setter
@Configuration
public class TestConfigProperties implements TestConfig {

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

    @Value("${targetInstance}")
    protected String targetInstance;

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;

    @Value("${s2s-secret}")
    protected String s2sSecret;

    @Value("${scope-name}")
    protected String scope;

    @Value("${fileversion.row}")
    private int fileVersionRow;

    @Value("${fileversion.coloumn}")
    private int fileVersionColumn;

    @Value("${fileversion.value}")
    private String fileVersionValue;

    @Bean
    public ObjectMapper defaultObjectMapper() {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.DEFAULT))
                .registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }



}