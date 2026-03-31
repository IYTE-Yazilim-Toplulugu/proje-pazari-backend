package com.iyte_yazilim.proje_pazari.domain.interfaces;

import java.time.Duration;

/** TokenBlacklistService */
public interface TokenBlacklistService {
    void blacklistToken(String token, Duration ttl);

    boolean isTokenBlacklisted(String token);
}
