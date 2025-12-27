package com.iyte_yazilim.proje_pazari.presentation.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
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
                                        "API documentation for Proje Pazari - A project marketplace platform")
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
}
