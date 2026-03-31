package com.iyte_yazilim.proje_pazari.infrastructure.security.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisTokenBlacklistServiceTest {

    @Mock private StringRedisTemplate redisTemplate;

    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private RedisTokenBlacklistService service;

    private static final String TOKEN = "test.jwt.token";
    private static final String KEY = "token:blacklist:" + TOKEN;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should blacklist token with given TTL when TTL is positive")
    void blacklistToken_withValidPositiveTtl_setsWithGivenTtl() {
        Duration ttl = Duration.ofMinutes(30);

        service.blacklistToken(TOKEN, ttl);

        verify(valueOperations).set(KEY, "blacklisted", ttl);
    }

    @Test
    @DisplayName("Should use default 24h TTL when TTL is null")
    void blacklistToken_withNullTtl_usesDefaultTtl() {
        service.blacklistToken(TOKEN, null);

        verify(valueOperations).set(KEY, "blacklisted", Duration.ofHours(24));
    }

    @Test
    @DisplayName("Should use default 24h TTL when TTL is zero")
    void blacklistToken_withZeroTtl_usesDefaultTtl() {
        service.blacklistToken(TOKEN, Duration.ZERO);

        verify(valueOperations).set(KEY, "blacklisted", Duration.ofHours(24));
    }

    @Test
    @DisplayName("Should use default 24h TTL when TTL is negative")
    void blacklistToken_withNegativeTtl_usesDefaultTtl() {
        service.blacklistToken(TOKEN, Duration.ofSeconds(-1));

        verify(valueOperations).set(KEY, "blacklisted", Duration.ofHours(24));
    }

    @Test
    @DisplayName("Should return true when token key exists in Redis")
    void isTokenBlacklisted_whenKeyExists_returnsTrue() {
        when(redisTemplate.hasKey(KEY)).thenReturn(true);

        assertTrue(service.isTokenBlacklisted(TOKEN));
    }

    @Test
    @DisplayName("Should return false when token key does not exist in Redis")
    void isTokenBlacklisted_whenKeyAbsent_returnsFalse() {
        when(redisTemplate.hasKey(KEY)).thenReturn(false);

        assertFalse(service.isTokenBlacklisted(TOKEN));
    }

    @Test
    @DisplayName("Should return false when Redis hasKey returns null")
    void isTokenBlacklisted_whenHasKeyReturnsNull_returnsFalse() {
        when(redisTemplate.hasKey(KEY)).thenReturn(null);

        assertFalse(service.isTokenBlacklisted(TOKEN));
    }

    @Test
    @DisplayName("Should use correct key prefix for token")
    void blacklistToken_usesCorrectKeyPrefix() {
        Duration ttl = Duration.ofHours(1);

        service.blacklistToken("someToken", ttl);

        verify(valueOperations).set(eq("token:blacklist:someToken"), anyString(), eq(ttl));
    }
}
