package com.iyte_yazilim.proje_pazari.infrastructure.security.service;

import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String USER_BLACKLIST_PREFIX = "user:blacklist:";
    private static final Duration DEFAULT_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklistToken(String token, Duration ttl) {
        String key = BLACKLIST_PREFIX + token;
        Duration effective =
                (ttl != null && !ttl.isNegative() && !ttl.isZero()) ? ttl : DEFAULT_TTL;
        redisTemplate.opsForValue().set(key, "blacklisted", effective);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }

    @Override
    public void blacklistUser(String email, Duration ttl) {
        String key = USER_BLACKLIST_PREFIX + email;
        Duration effective =
                (ttl != null && !ttl.isNegative() && !ttl.isZero()) ? ttl : DEFAULT_TTL;
        redisTemplate.opsForValue().set(key, "blacklisted", effective);
    }

    @Override
    public boolean isUserBlacklisted(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(USER_BLACKLIST_PREFIX + email));
    }
}
