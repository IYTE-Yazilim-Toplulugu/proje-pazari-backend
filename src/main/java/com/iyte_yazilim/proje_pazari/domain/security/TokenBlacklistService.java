package com.iyte_yazilim.proje_pazari.domain.security;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * TokenBlacklistService
 */
public interface TokenBlacklistService {
    void blacklistToken(String token);

    boolean isTokenBlacklisted(String token);

    void blacklistToken(String token, Duration ttl);
}
