package com.iyte_yazilim.proje_pazari;

import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration that provides an in-memory TokenBlacklistService when Redis is not available.
 */
@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    public TokenBlacklistService tokenBlacklistService() {
        return new InMemoryTokenBlacklistService();
    }

    private static class InMemoryTokenBlacklistService implements TokenBlacklistService {
        private final Map<String, Boolean> blacklist = new ConcurrentHashMap<>();
        private final Map<String, Boolean> userBlacklist = new ConcurrentHashMap<>();

        @Override
        public void blacklistToken(String token, Duration ttl) {
            blacklist.put(token, true);
        }

        @Override
        public boolean isTokenBlacklisted(String token) {
            return blacklist.containsKey(token);
        }

        @Override
        public void blacklistUser(String email, Duration ttl) {
            userBlacklist.put(email, true);
        }

        @Override
        public boolean isUserBlacklisted(String email) {
            return userBlacklist.containsKey(email);
        }
    }
}
