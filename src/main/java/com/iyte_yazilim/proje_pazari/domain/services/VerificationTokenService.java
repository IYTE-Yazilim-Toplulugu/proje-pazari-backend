package com.iyte_yazilim.proje_pazari.domain.services;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for email verification token operations.
 *
 * <p>Pure domain logic without framework dependencies.
 */
public class VerificationTokenService {

    private static final long EXPIRATION_HOURS = 24;

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public LocalDateTime calculateExpirationDate() {
        return LocalDateTime.now().plusHours(EXPIRATION_HOURS);
    }

    public boolean isTokenExpired(LocalDateTime expirationDate) {
        return LocalDateTime.now().isAfter(expirationDate);
    }
}
