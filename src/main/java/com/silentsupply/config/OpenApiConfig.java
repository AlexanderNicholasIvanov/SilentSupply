package com.silentsupply.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 * Customizes the API documentation with metadata, JWT auth scheme, and endpoint grouping.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures the OpenAPI specification with JWT bearer auth and API metadata.
     *
     * @return the customized OpenAPI definition
     */
    @Bean
    public OpenAPI silentSupplyOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SilentSupply API")
                        .description("B2B supply chain marketplace with automated negotiation. "
                                + "Register as a supplier or buyer, manage products, place orders, "
                                + "submit RFQs, and let the negotiation engine handle the rest.")
                        .version("0.1.0")
                        .contact(new Contact()
                                .name("SilentSupply Team")
                                .email("team@silentsupply.com")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token obtained from /api/auth/login")));
    }

    /**
     * Groups authentication endpoints (register, login).
     *
     * @return the grouped API definition for auth
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("1-auth")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * Groups product catalog endpoints.
     *
     * @return the grouped API definition for products
     */
    @Bean
    public GroupedOpenApi productsApi() {
        return GroupedOpenApi.builder()
                .group("2-products")
                .pathsToMatch("/api/products/**")
                .build();
    }

    /**
     * Groups catalog order endpoints.
     *
     * @return the grouped API definition for orders
     */
    @Bean
    public GroupedOpenApi ordersApi() {
        return GroupedOpenApi.builder()
                .group("3-orders")
                .pathsToMatch("/api/orders/**")
                .build();
    }

    /**
     * Groups RFQ and proposal endpoints.
     *
     * @return the grouped API definition for negotiations
     */
    @Bean
    public GroupedOpenApi negotiationApi() {
        return GroupedOpenApi.builder()
                .group("4-negotiation")
                .pathsToMatch("/api/rfqs/**", "/api/suppliers/*/negotiation-rules/**")
                .build();
    }

    /**
     * Groups company management endpoints.
     *
     * @return the grouped API definition for companies
     */
    @Bean
    public GroupedOpenApi companiesApi() {
        return GroupedOpenApi.builder()
                .group("5-companies")
                .pathsToMatch("/api/companies/**")
                .build();
    }
}
