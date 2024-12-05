package uk.gov.hmcts.reform.cwrdapi.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

@Configuration
@io.swagger.v3.oas.annotations.security.SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT", scheme = "bearer")
@io.swagger.v3.oas.annotations.security.SecurityScheme(name = "ServiceAuthorization", type = SecuritySchemeType.APIKEY,
    in = SecuritySchemeIn.HEADER, bearerFormat = "JWT", description = "ServiceAuthorization")
public class SwaggerConfiguration {


    /* private static final String DESCRIPTION = "API will help to provide Case worker user profile data to clients.";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes(
                    "Authorization",
                    new io.swagger.v3.oas.models.security.SecurityScheme()
                        .name("Authorization")
                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Valid IDAM user token, (Bearer keyword is "
                            + "added automatically)")
                )
                .addSecuritySchemes("ServiceAuthorization",
                    new io.swagger.v3.oas.models.security.SecurityScheme()
                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                        .name("ServiceAuthorization")
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
            .addSecurityItem(new SecurityRequirement().addList("Authorization"))
            .addSecurityItem(new SecurityRequirement().addList("ServiceAuthorization"));
    }*/

    @Bean
    public GroupedOpenApi publicApi(OperationCustomizer customGlobalHeaders) {
        return GroupedOpenApi.builder()
            .group("rd-caseworker-ref-api")
            .pathsToMatch("/**")
            .build();
    }

    @Bean
    public OperationCustomizer customGlobalHeaders() {
        return (Operation customOperation, HandlerMethod handlerMethod) -> {
            /*Parameter serviceAuthorizationHeader = new Parameter()
                .in(ParameterIn.HEADER.toString())
                .schema(new StringSchema())
                .name("ServiceAuthorization")
                .description("Keyword `Bearer` followed "
                    + "by a service-to-service token for a whitelisted micro-service")
                .required(true);
            Parameter authorizationHeader = new Parameter()
                .in(ParameterIn.HEADER.toString())
                .schema(new StringSchema())
                .name("Authorization")
                .description("Authorization token")
                .required(true);
            customOperation.addParametersItem(authorizationHeader);
            customOperation.addParametersItem(serviceAuthorizationHeader);*/
            return customOperation;
        };
    }
}

