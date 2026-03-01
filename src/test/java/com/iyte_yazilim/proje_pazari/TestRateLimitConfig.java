package com.iyte_yazilim.proje_pazari;

import com.iyte_yazilim.proje_pazari.infrastructure.security.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration that provides a RateLimitConfig with very high capacity to
 * avoid 429 errors in
 * tests.
 */
@TestConfiguration
public class TestRateLimitConfig {

    @Bean
    @Primary
    public RateLimitConfig testRateLimitConfig() {
        return new RateLimitConfig() {
            private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

            @Override
            public Bucket resolveBucket(String ip) {
                return buckets.computeIfAbsent(ip, this::createHighCapacityBucket);
            }

            private Bucket createHighCapacityBucket(String ip) {
                Bandwidth limit = Bandwidth.builder()
                        .capacity(10000)
                        .refillIntervally(10000, Duration.ofMinutes(1))
                        .build();
                return Bucket.builder().addLimit(limit).build();
            }
        };
    }
}
