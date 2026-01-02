package com.iyte_yazilim.proje_pazari.domain.services;

import com.iyte_yazilim.proje_pazari.domain.entities.User;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
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

    public String generateTokenWithExpiry(User user) {
        String token = generateToken();
        user.setVerificationToken(token);
        user.setVerificationTokenExpiresAt(calculateExpirationDate());
        return token;
    }
}
