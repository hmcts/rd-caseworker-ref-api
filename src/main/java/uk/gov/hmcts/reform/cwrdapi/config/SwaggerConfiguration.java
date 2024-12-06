package uk.gov.hmcts.reform.cwrdapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    private static final String DESCRIPTION = "API will help to provide Case worker user profile data to clients.";
    private static final String AUTHORIZATION = "Authorization";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes(
                    AUTHORIZATION,
                    new io.swagger.v3.oas.models.security.SecurityScheme()
                        .name(AUTHORIZATION)
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Valid IDAM user token, (Bearer keyword is "
                            + "added automatically)")
                )
                .addSecuritySchemes(SERVICE_AUTHORIZATION,
                    new io.swagger.v3.oas.models.security.SecurityScheme()
                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                        .name(SERVICE_AUTHORIZATION)
                        .type(SecurityScheme.Type.APIKEY)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Valid Service-to-Service JWT token for a "
                            + "whitelisted micro-service")
                )
            )
            .info(new Info().title("RD Case Worker Ref Api service")
                .description(DESCRIPTION))
            .externalDocs(new ExternalDocumentation()
                .description("README")
                .url("https://github.com/hmcts/rd-caseworker-ref-api/blob/master/README.md"))
            .addSecurityItem(new SecurityRequirement().addList(AUTHORIZATION))
            .addSecurityItem(new SecurityRequirement().addList(SERVICE_AUTHORIZATION));
    }


    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("rd-caseworker-ref-api")
                .pathsToMatch("/**")
                .build();
    }



}
