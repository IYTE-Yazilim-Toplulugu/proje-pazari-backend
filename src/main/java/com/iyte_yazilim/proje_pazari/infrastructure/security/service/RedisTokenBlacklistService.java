package com.iyte_yazilim.proje_pazari.infrastructure.security.service;

import com.iyte_yazilim.proje_pazari.domain.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklistToken(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", DEFAULT_TTL);
    }

    public void blacklistToken(String token, Duration ttl) {
        String key = BLACKLIST_PREFIX + token;

        if (ttl != null && !ttl.isNegative() && !ttl.isZero()) {
            redisTemplate.opsForValue().set(key, "blaclklisted", ttl);
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
