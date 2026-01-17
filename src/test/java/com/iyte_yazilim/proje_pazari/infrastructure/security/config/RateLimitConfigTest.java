package com.iyte_yazilim.proje_pazari.infrastructure.security.config;

import static org.junit.jupiter.api.Assertions.*;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RateLimitConfigTest {

    private RateLimitConfig rateLimitConfig;

    @BeforeEach
    void setUp() {
        rateLimitConfig = new RateLimitConfig();
    }

    @Test
    @DisplayName("Should create bucket for new IP")
    void shouldCreateBucketForNewIp() {
        // Given
        String ip = "192.168.1.100";

        // When
        Bucket bucket = rateLimitConfig.resolveBucket(ip);

        // Then
        assertNotNull(bucket);
    }

    @Test
    @DisplayName("Should return same bucket for same IP")
    void shouldReturnSameBucketForSameIp() {
        // Given
        String ip = "192.168.1.100";

        // When
        Bucket bucket1 = rateLimitConfig.resolveBucket(ip);
        Bucket bucket2 = rateLimitConfig.resolveBucket(ip);

        // Then
        assertSame(bucket1, bucket2);
    }

    @Test
    @DisplayName("Should create different buckets for different IPs")
    void shouldCreateDifferentBucketsForDifferentIps() {
        // Given
        String ip1 = "192.168.1.100";
        String ip2 = "192.168.1.101";

        // When
        Bucket bucket1 = rateLimitConfig.resolveBucket(ip1);
        Bucket bucket2 = rateLimitConfig.resolveBucket(ip2);

        // Then
        assertNotSame(bucket1, bucket2);
    }

    @Test
    @DisplayName("Should allow 5 requests initially")
    void shouldAllow5RequestsInitially() {
        // Given
        String ip = "192.168.1.100";
        Bucket bucket = rateLimitConfig.resolveBucket(ip);

        // When & Then
        for (int i = 0; i < 5; i++) {
            assertTrue(bucket.tryConsume(1), "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @DisplayName("Should block 6th request")
    void shouldBlock6thRequest() {
        // Given
        String ip = "192.168.1.100";
        Bucket bucket = rateLimitConfig.resolveBucket(ip);

        // When - Consume all 5 tokens
        for (int i = 0; i < 5; i++) {
            bucket.tryConsume(1);
        }

        // Then - 6th request should be blocked
        assertFalse(bucket.tryConsume(1), "6th request should be blocked");
    }

    @Test
    @DisplayName("Should track remaining tokens correctly")
    void shouldTrackRemainingTokensCorrectly() {
        // Given
        String ip = "192.168.1.100";
        Bucket bucket = rateLimitConfig.resolveBucket(ip);

        // When
        bucket.tryConsume(1);
        bucket.tryConsume(1);

        // Then
        var probe = bucket.tryConsumeAndReturnRemaining(1);
        assertTrue(probe.isConsumed());
        assertEquals(2, probe.getRemainingTokens());
    }

    @Test
    @DisplayName("Should handle IPv6 addresses")
    void shouldHandleIpv6Addresses() {
        // Given
        String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";

        // When
        Bucket bucket = rateLimitConfig.resolveBucket(ipv6);

        // Then
        assertNotNull(bucket);
        assertTrue(bucket.tryConsume(1));
    }

    @Test
    @DisplayName("Should handle empty IP string")
    void shouldHandleEmptyIp() {
        // Given
        String emptyIp = "";

        // When
        Bucket bucket = rateLimitConfig.resolveBucket(emptyIp);

        // Then
        assertNotNull(bucket);
    }

    @Test
    @DisplayName("Should create buckets for multiple concurrent IPs")
    void shouldCreateBucketsForMultipleConcurrentIps() {
        // Given
        String[] ips = {"192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"};

        // When
        Bucket[] buckets = new Bucket[ips.length];
        for (int i = 0; i < ips.length; i++) {
            buckets[i] = rateLimitConfig.resolveBucket(ips[i]);
        }

        // Then
        for (int i = 0; i < buckets.length; i++) {
            assertNotNull(buckets[i]);
            // Each bucket should have independent limits
            assertTrue(buckets[i].tryConsume(5));
        }
    }
}
