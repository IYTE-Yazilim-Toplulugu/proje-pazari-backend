package com.iyte_yazilim.proje_pazari.domain.entities;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Domain entity representing a single-use password reset token.
 *
 * <p>Tokens expire after a configurable TTL and are invalidated on use.
 */
@Getter
@Setter
public class PasswordResetToken {

    private String id;
    private String userId;
    private String email;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;

    public PasswordResetToken() {}

    public PasswordResetToken(String userId, String email, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.email = email;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public boolean isExpired(Clock clock) {
        return LocalDateTime.now(clock).isAfter(expiresAt);
    }
}
