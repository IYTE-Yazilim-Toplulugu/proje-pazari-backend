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

    private static final String PLACEHOLDER_INDICATOR = "change-this-in-production";

    @PostConstruct
    public void validateJwtSecret() {
        if (jwtSecret.contains(PLACEHOLDER_INDICATOR)
                || jwtSecret.matches(".*(.)\\1{2,}.*")
                || jwtSecret.matches(".*123.*|.*abc.*|.*qwerty.*|.*asdf.*|.*zxcv.*")) {
            String errorMessage =
                    "CRITICAL SECURITY ERROR: JWT secret contains a known insecure placeholder or suspicious pattern."
                            + "This is a severe security vulnerability. "
                            + "Please set the JWT_SECRET environment variable to a secure random value "
                            + "(minimum 32 characters, must not contain placeholder substrings). "
                            + "Application startup blocked."
                            + "(use: openssl rand -base64 32)";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        if (jwtSecret.length() < 32) {
            String errorMessage =
                    "SECURITY ERROR: JWT secret is too short (minimum 32 characters required for HS256). "
                            + "Current length: "
                            + jwtSecret.length()
                            + ". "
                            + "Please set a longer value via environment variable JWT_SECRET. "
                            + "Application startup blocked.";
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        log.info("JWT secret validation passed");
    }
}
