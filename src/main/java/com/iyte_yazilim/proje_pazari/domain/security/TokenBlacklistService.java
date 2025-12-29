package com.iyte_yazilim.proje_pazari.domain.security;

import java.time.Duration;

/**
 * TokenBlacklistService
 */
public interface TokenBlacklistService {
    void blacklistToken(String token);

    boolean isTokenBlacklisted(String token);

    void blacklistToken(String token, Duration ttl);
}
