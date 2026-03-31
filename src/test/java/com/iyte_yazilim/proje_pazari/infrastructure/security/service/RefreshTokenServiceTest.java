package com.iyte_yazilim.proje_pazari.infrastructure.security.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.RefreshTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.RefreshTokenEntity;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private RefreshTokenService refreshTokenService;

    private static final String TEST_USER_ID = "user-123";
    private static final String TEST_TOKEN = "test-token-uuid";
    private static final Long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7 days in ms

    @BeforeEach
    void setUp() {
        // Set the expiration time via reflection (simulating @Value injection)
        ReflectionTestUtils.setField(
                refreshTokenService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    @DisplayName("Should create refresh token successfully")
    void shouldCreateRefreshToken() {
        // Given
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String token = refreshTokenService.createRefreshToken(TEST_USER_ID);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("-")); // UUID format check

        ArgumentCaptor<RefreshTokenEntity> captor =
                ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshTokenEntity savedEntity = captor.getValue();
        assertEquals(TEST_USER_ID, savedEntity.getUserId());
        assertEquals(token, savedEntity.getToken());
        assertFalse(savedEntity.getRevoked());
        assertNotNull(savedEntity.getExpiresAt());
    }

    @Test
    @DisplayName("Should validate valid refresh token")
    void shouldValidateValidRefreshToken() {
        // Given
        RefreshTokenEntity validToken = new RefreshTokenEntity();
        validToken.setUserId(TEST_USER_ID);
        validToken.setToken(TEST_TOKEN);
        validToken.setRevoked(false);
        validToken.setExpiresAt(Instant.now().plusSeconds(3600)); // Expires in 1 hour

        when(refreshTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(validToken));

        // When
        Optional<String> result = refreshTokenService.validateRefreshToken(TEST_TOKEN);

        // Then
        assertTrue(result.isPresent());
        assertEquals(TEST_USER_ID, result.get());
        verify(refreshTokenRepository).findByToken(TEST_TOKEN);
    }

    @Test
    @DisplayName("Should reject non-existent refresh token")
    void shouldRejectNonExistentToken() {
        // Given
        when(refreshTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.empty());

        // When
        Optional<String> result = refreshTokenService.validateRefreshToken(TEST_TOKEN);

        // Then
        assertFalse(result.isPresent());
        verify(refreshTokenRepository).findByToken(TEST_TOKEN);
    }

    @Test
    @DisplayName("Should reject revoked refresh token")
    void shouldRejectRevokedToken() {
        // Given
        RefreshTokenEntity revokedToken = new RefreshTokenEntity();
        revokedToken.setUserId(TEST_USER_ID);
        revokedToken.setToken(TEST_TOKEN);
        revokedToken.setRevoked(true); // Token is revoked
        revokedToken.setExpiresAt(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(revokedToken));

        // When
        Optional<String> result = refreshTokenService.validateRefreshToken(TEST_TOKEN);

        // Then
        assertFalse(result.isPresent());
        verify(refreshTokenRepository).findByToken(TEST_TOKEN);
    }

    @Test
    @DisplayName("Should reject expired refresh token")
    void shouldRejectExpiredToken() {
        // Given
        RefreshTokenEntity expiredToken = new RefreshTokenEntity();
        expiredToken.setUserId(TEST_USER_ID);
        expiredToken.setToken(TEST_TOKEN);
        expiredToken.setRevoked(false);
        expiredToken.setExpiresAt(Instant.now().minusSeconds(3600)); // Expired 1 hour ago

        when(refreshTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(expiredToken));

        // When
        Optional<String> result = refreshTokenService.validateRefreshToken(TEST_TOKEN);

        // Then
        assertFalse(result.isPresent());
        verify(refreshTokenRepository).findByToken(TEST_TOKEN);
    }

    @Test
    @DisplayName("Should revoke refresh token")
    void shouldRevokeRefreshToken() {
        // Given
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setUserId(TEST_USER_ID);
        token.setToken(TEST_TOKEN);
        token.setRevoked(false);

        when(refreshTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        refreshTokenService.revokeRefreshToken(TEST_TOKEN);

        // Then
        ArgumentCaptor<RefreshTokenEntity> captor =
                ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshTokenEntity revokedToken = captor.getValue();
        assertTrue(revokedToken.getRevoked());
    }

    @Test
    @DisplayName("Should do nothing when revoking non-existent token")
    void shouldDoNothingWhenRevokingNonExistentToken() {
        // Given
        when(refreshTokenRepository.findByToken(TEST_TOKEN)).thenReturn(Optional.empty());

        // When
        refreshTokenService.revokeRefreshToken(TEST_TOKEN);

        // Then
        verify(refreshTokenRepository).findByToken(TEST_TOKEN);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should revoke all user tokens")
    void shouldRevokeAllUserTokens() {
        // Given
        doNothing().when(refreshTokenRepository).revokeAllUserTokens(TEST_USER_ID);

        // When
        refreshTokenService.revokeAllUserTokens(TEST_USER_ID);

        // Then
        verify(refreshTokenRepository).revokeAllUserTokens(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should cleanup expired tokens")
    void shouldCleanupExpiredTokens() {
        // Given
        doNothing().when(refreshTokenRepository).deleteExpiredTokens(any(Instant.class));

        // When
        refreshTokenService.cleanupExpiredTokens();

        // Then
        verify(refreshTokenRepository).deleteExpiredTokens(any(Instant.class));
    }

    @Test
    @DisplayName("Should generate unique tokens for different requests")
    void shouldGenerateUniqueTokens() {
        // Given
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String token1 = refreshTokenService.createRefreshToken(TEST_USER_ID);
        String token2 = refreshTokenService.createRefreshToken(TEST_USER_ID);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should set correct expiration time")
    void shouldSetCorrectExpirationTime() {
        // Given
        Instant beforeCreation = Instant.now();

        when(refreshTokenRepository.save(any(RefreshTokenEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        refreshTokenService.createRefreshToken(TEST_USER_ID);

        Instant afterCreation = Instant.now();

        // Then
        ArgumentCaptor<RefreshTokenEntity> captor =
                ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshTokenEntity savedEntity = captor.getValue();
        Instant expiresAt = savedEntity.getExpiresAt();

        // Check that expiration is approximately 7 days from now (within 1 second tolerance)
        long expectedExpirationMs = REFRESH_TOKEN_EXPIRATION;
        long actualDuration =
                expiresAt.toEpochMilli()
                        - (beforeCreation.toEpochMilli() + afterCreation.toEpochMilli()) / 2;

        assertTrue(
                Math.abs(actualDuration - expectedExpirationMs) < 1000,
                "Expiration should be approximately 7 days from creation");
    }
}
