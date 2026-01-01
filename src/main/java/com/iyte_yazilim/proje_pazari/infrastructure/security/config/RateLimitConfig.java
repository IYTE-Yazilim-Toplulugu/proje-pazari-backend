package com.iyte_yazilim.proje_pazari.infrastructure.security.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    private final ConcurrentHashMap<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    @Bean
    public ConcurrentHashMap<String, Bucket> rateLimitBuckets() {
        return ipBuckets;
    }

    public Bucket resolveBucket(String ip) {
        return ipBuckets.computeIfAbsent(ip, this::createNewBucket);
    }

    private Bucket createNewBucket(String ip) {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
