package com.iyte_yazilim.proje_pazari.infrastructure.security.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${security.ip-ban-cache-ttl-seconds:60}")
    private long ipBanCacheTtlSeconds;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofSeconds(ipBanCacheTtlSeconds))
                        .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .withCacheConfiguration("ip-ban-cache", config)
                .build();
    }
}
