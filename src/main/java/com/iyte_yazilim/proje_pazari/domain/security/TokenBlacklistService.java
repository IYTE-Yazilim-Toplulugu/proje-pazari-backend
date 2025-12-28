package com.iyte_yazilim.proje_pazari.domain.security;

/**
 * TokenBlacklistService
 */
public interface TokenBlacklistService {
    void blacklistToken(String token);

    boolean isTokenBlacklisted(String token);
}
