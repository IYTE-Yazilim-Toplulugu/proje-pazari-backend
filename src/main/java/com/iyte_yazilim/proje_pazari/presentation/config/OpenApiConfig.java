package com.iyte_yazilim.proje_pazari.presentation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Proje Pazari API")
                                .version("1.0.0")
                                .description(
                                        "API documentation for Proje Pazari - A project marketplace platform\n\n"
                                                + "**Multi-Language Support:**\n"
                                                + "- Supported languages: Turkish (tr), English (en)\n"
                                                + "- Default language: Turkish (tr)\n"
                                                + "- Use `Accept-Language` header to specify preferred language\n"
                                                + "- Example: `Accept-Language: en` for English responses")
                                .contact(
                                        new Contact()
                                                .name("IYTE Yazilim Toplulugu")
                                                .email("contact@proje-pazari.com")
                                                .url("https://github.com/IYTE-Yazilim-Toplulugu"))
                                .license(
                                        new License()
                                                .name("MIT License")
                                                .url("https://opensource.org/licenses/MIT")))
                .servers(
                        List.of(
                                new Server()
                                        .url("http://localhost:8080")
                                        .description("Development Server"),
                                new Server()
                                        .url("https://api.proje-pazari.com")
                                        .description("Production Server")))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "Bearer Authentication",
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description(
                                                        "Enter JWT token obtained from /api/v1/auth/login")));
    }

    /**
     * Adds Accept-Language header to all API operations in Swagger UI
     */
    @Bean
    public OperationCustomizer customizeAcceptLanguageHeader() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(
                    new HeaderParameter()
                            .name("Accept-Language")
                            .description(
                                    "Preferred language for API responses (tr, en). Default: tr")
                            .required(false)
                            .schema(
                                    new StringSchema()
                                            ._enum(List.of("tr", "en"))
                                            ._default("tr"))
                            .example("en"));
            return operation;
        };
    }
}