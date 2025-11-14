package com.crudzaso.CrudCloud.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI/Swagger documentation.
 *
 * Configures automatic API documentation generation and UI.
 * Accessible at http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenAPIConfig {

    /**
     * Configures the OpenAPI specification with API information and security definitions.
     *
     * @return OpenAPI bean with API documentation configuration
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CrudCloud API")
                        .version("1.0.0")
                        .description("Comprehensive API for managing cloud databases, subscriptions, and payments")
                        .contact(new Contact()
                                .name("CrudCloud Support")
                                .email("support@crudcloud.com")
                                .url("https://crudcloud.com")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}
