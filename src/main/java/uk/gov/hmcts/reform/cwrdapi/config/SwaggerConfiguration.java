package uk.gov.hmcts.reform.cwrdapi.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityScheme(name = "ServiceAuthorization", type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER, bearerFormat = "JWT", description = "ServiceAuthorization")
public class SwaggerConfiguration {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("rd-caseworker-ref-api")
                .pathsToMatch("/**")
                .build();
    }

}
