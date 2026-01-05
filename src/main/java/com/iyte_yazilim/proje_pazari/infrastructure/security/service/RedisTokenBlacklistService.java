package com.iyte_yazilim.proje_pazari.infrastructure.security.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklistToken(String token, Duration ttl) {
        String key = BLACKLIST_PREFIX + token;

        if (ttl != null && !ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttl);
        } else {
            redisTemplate.opsForValue().set(key, "blacklisted", DEFAULT_TTL);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
