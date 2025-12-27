package com.iyte_yazilim.proje_pazari.presentation.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class JwtSecretValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String DEFAULT_SECRET = "your-256-bit-secret-key-change-this-in-production-please-make-it-long-enough-for-hs256";

    @PostConstruct
    public void validateJwtSecret() {
        if (jwtSecret.equals(DEFAULT_SECRET)) {
            String errorMessage = "CRITICAL SECURITY ERROR: JWT secret is using the default value. " +
                    "This is a severe security vulnerability. " +
                    "Please set the 'jwt.secret' property to a secure random value via environment variable JWT_SECRET. " +
                    "Application startup blocked.";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        if (jwtSecret.length() < 32) {
            String errorMessage = "SECURITY ERROR: JWT secret is too short (minimum 32 characters required for HS256). " +
                    "Current length: " + jwtSecret.length() + ". " +
                    "Please set a longer value via environment variable JWT_SECRET. " +
                    "Application startup blocked.";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        log.info("JWT secret validation passed");
    }
}
