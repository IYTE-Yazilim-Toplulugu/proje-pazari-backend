package com.iyte_yazilim.proje_pazari.presentation.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtSecretValidator {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final String PLACEHOLDER_INDICATOR = "change-this-in-production";

    @PostConstruct
    public void validateJwtSecret() {
        if (jwtSecret.contains(PLACEHOLDER_INDICATOR)) {
            String errorMessage =
                    "SECURITY ERROR: JWT secret contains the known placeholder substring. "
                            + "Set the JWT_SECRET environment variable to a secure random value "
                            + "(minimum 32 characters). "
                            + "Generate one with: openssl rand -base64 64";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        if (jwtSecret.length() < 32) {
            String errorMessage =
                    "SECURITY ERROR: JWT secret is too short (minimum 32 characters required for HS256). "
                            + "Current length: "
                            + jwtSecret.length()
                            + ". "
                            + "Set a longer value via environment variable JWT_SECRET.";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        log.info("JWT secret validation passed.");
    }
}
