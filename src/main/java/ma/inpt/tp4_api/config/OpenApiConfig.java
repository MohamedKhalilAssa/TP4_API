package ma.inpt.tp4_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token authentication")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    @Bean
    public org.springdoc.core.models.GroupedOpenApi v1Api() {
        return org.springdoc.core.models.GroupedOpenApi.builder()
                .group("v1")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("API V1")
                        .version("1.0")
                        .description("Version 1 endpoints with JWT authentication")))
                .pathsToMatch("/api/v1/**")
                .build();
    }

    @Bean
    public org.springdoc.core.models.GroupedOpenApi v2Api() {
        return org.springdoc.core.models.GroupedOpenApi.builder()
                .group("v2")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info()
                        .title("API V2")
                        .version("2.0")
                        .description("Version 2 endpoints")))
                .pathsToMatch("/api/v2/**")
                .build();
    }
}