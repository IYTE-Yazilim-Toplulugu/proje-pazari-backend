package com.iyte_yazilim.proje_pazari.infrastructure.security.service;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.RefreshTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.RefreshTokenEntity;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing refresh tokens.
 *
 * <p>Handles creation, validation, and revocation of refresh tokens for JWT authentication.
 *
 * <h2>Features:</h2>
 *
 * <ul>
 *   <li>Generate secure refresh tokens
 *   <li>Validate and verify tokens
 *   <li>Revoke tokens on logout
 *   <li>Automatic cleanup of expired tokens
 * </ul>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days default
    private Long refreshTokenExpiration;

    /**
     * Creates a new refresh token for a user.
     *
     * @param userId the user's unique identifier
     * @return the generated refresh token string
     */
    @Transactional
    public String createRefreshToken(String userId) {
        // Generate a secure random token
        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setToken(token);
        refreshToken.setUserId(userId);
        refreshToken.setExpiresAt(
                Instant.now().plus(Duration.ofMillis(refreshTokenExpiration)));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
        log.debug("Created refresh token for user: {}", userId);

        return token;
    }

    /**
     * Validates a refresh token and returns the user ID if valid.
     *
     * @param token the refresh token to validate
     * @return Optional containing userId if valid, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<String> validateRefreshToken(String token) {
        Optional<RefreshTokenEntity> refreshToken = refreshTokenRepository.findByToken(token);

        if (refreshToken.isEmpty()) {
            log.warn("Refresh token not found");
            return Optional.empty();
        }

        RefreshTokenEntity tokenEntity = refreshToken.get();

        // Check if token is revoked
        if (tokenEntity.getRevoked()) {
            log.warn("Attempted use of revoked refresh token for user: {}", tokenEntity.getUserId());
            return Optional.empty();
        }

        // Check if token is expired
        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Expired refresh token used for user: {}", tokenEntity.getUserId());
            return Optional.empty();
        }

        return Optional.of(tokenEntity.getUserId());
    }

    /**
     * Revokes a specific refresh token.
     *
     * @param token the token to revoke
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository
                .findByToken(token)
                .ifPresent(
                        refreshToken -> {
                            refreshToken.setRevoked(true);
                            refreshTokenRepository.save(refreshToken);
                            log.debug(
                                    "Revoked refresh token for user: {}",
                                    refreshToken.getUserId());
                        });
    }

    /**
     * Revokes all refresh tokens for a user (e.g., on logout from all devices).
     *
     * @param userId the user's ID
     */
    @Transactional
    public void revokeAllUserTokens(String userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
        log.debug("Revoked all refresh tokens for user: {}", userId);
    }

    /**
     * Deletes expired tokens from the database. Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("Cleaned up expired refresh tokens");
    }
}
