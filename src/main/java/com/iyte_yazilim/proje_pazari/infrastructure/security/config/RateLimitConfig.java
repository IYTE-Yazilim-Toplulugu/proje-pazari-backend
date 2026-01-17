package com.iyte_yazilim.proje_pazari.infrastructure.security.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.cache.CacheBuilder;

@Configuration
public class RateLimitConfig {

    private final ConcurrentMap<String, Bucket> ipBuckets = CacheBuilder.newBuilder()
            // Evict buckets that have not been accessed for 15 minutes
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .build()
            .asMap();

    @Bean
    public ConcurrentMap<String, Bucket> rateLimitBuckets() {
        return ipBuckets;
    }

    public Bucket resolveBucket(String ip) {
        return ipBuckets.computeIfAbsent(ip, this::createNewBucket);
    }

    private Bucket createNewBucket(String ip) {
        Bandwidth limit = Bandwidth.builder().capacity(5).refillIntervally(5, Duration.ofMinutes(1)).build();
        return Bucket.builder().addLimit(limit).build();
    }
}
