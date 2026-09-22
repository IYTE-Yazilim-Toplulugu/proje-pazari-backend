package com.iyte_yazilim.proje_pazari.domain.entities;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Domain entity representing a single-use password reset token.
 *
 * <p>Tokens expire after a configurable TTL and are invalidated on use.
 */
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

    public String getId() {
        return this.id;
    }

    public String getUserId() {
        return this.userId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getToken() {
        return this.token;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return this.usedAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
