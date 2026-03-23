package com.iyte_yazilim.proje_pazari.application.commands.logout;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.security.service.RefreshTokenService;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.Duration;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutHandlerTest {

    @Mock private RefreshTokenService refreshTokenService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private JwtUtil jwtUtil;
    @Mock private MessageService messageService;

    @InjectMocks private LogoutHandler logoutHandler;

    @BeforeEach
    void setUp() {
        lenient()
                .when(messageService.getMessage("auth.logout.success"))
                .thenReturn("Logout successful");
    }

    @Test
    @DisplayName("Should revoke refresh token and blacklist access token on logout")
    void shouldLogout_WhenBothTokensAreValid() {
        // Given
        String refreshToken = "valid-refresh-token";
        String accessToken = "valid.access.token";
        long futureMillis = System.currentTimeMillis() + 60_000; // 1 minute from now
        Date expiration = new Date(futureMillis);

        when(jwtUtil.extractExpiration(accessToken)).thenReturn(expiration);

        LogoutCommand command = new LogoutCommand(accessToken, refreshToken);

        // When
        ApiResponse<Void> response = logoutHandler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Logout successful", response.getMessage());
        verify(refreshTokenService).revokeRefreshToken(refreshToken);
        verify(tokenBlacklistService).blacklistToken(eq(accessToken), any(Duration.class));
    }

    @Test
    @DisplayName("Should still revoke refresh token when access token is null")
    void shouldRevokeRefreshToken_WhenAccessTokenIsNull() {
        // Given
        String refreshToken = "valid-refresh-token";
        LogoutCommand command = new LogoutCommand(null, refreshToken);

        // When
        ApiResponse<Void> response = logoutHandler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(refreshTokenService).revokeRefreshToken(refreshToken);
        verify(tokenBlacklistService, never()).blacklistToken(any(), any());
    }

    @Test
    @DisplayName("Should still revoke refresh token when access token is already expired")
    void shouldRevokeRefreshToken_WhenAccessTokenIsExpired() {
        // Given
        String refreshToken = "valid-refresh-token";
        String expiredAccessToken = "expired.access.token";
        Date pastExpiration = new Date(System.currentTimeMillis() - 60_000); // 1 minute ago

        when(jwtUtil.extractExpiration(expiredAccessToken)).thenReturn(pastExpiration);

        LogoutCommand command = new LogoutCommand(expiredAccessToken, refreshToken);

        // When
        ApiResponse<Void> response = logoutHandler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(refreshTokenService).revokeRefreshToken(refreshToken);
        // Expired token has no remaining TTL, so it should not be blacklisted
        verify(tokenBlacklistService, never()).blacklistToken(any(), any());
    }

    @Test
    @DisplayName("Should still revoke refresh token when access token is malformed")
    void shouldRevokeRefreshToken_WhenAccessTokenIsMalformed() {
        // Given
        String refreshToken = "valid-refresh-token";
        String malformedToken = "not.a.jwt";

        when(jwtUtil.extractExpiration(malformedToken))
                .thenThrow(new RuntimeException("Malformed JWT"));

        LogoutCommand command = new LogoutCommand(malformedToken, refreshToken);

        // When
        ApiResponse<Void> response = logoutHandler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(refreshTokenService).revokeRefreshToken(refreshToken);
        verify(tokenBlacklistService, never()).blacklistToken(any(), any());
    }

    @Test
    @DisplayName("Should throw ValidationException when refresh token is missing")
    void shouldThrowValidationException_WhenRefreshTokenIsNull() {
        // Given
        LogoutCommand command = new LogoutCommand("some.access.token", null);

        // When & Then
        assertThrows(ValidationException.class, () -> logoutHandler.handle(command));
        verify(refreshTokenService, never()).revokeRefreshToken(any());
        verify(tokenBlacklistService, never()).blacklistToken(any(), any());
    }

    @Test
    @DisplayName("Should throw ValidationException when refresh token is blank")
    void shouldThrowValidationException_WhenRefreshTokenIsBlank() {
        // Given
        LogoutCommand command = new LogoutCommand("some.access.token", "   ");

        // When & Then
        assertThrows(ValidationException.class, () -> logoutHandler.handle(command));
        verify(refreshTokenService, never()).revokeRefreshToken(any());
    }
}
