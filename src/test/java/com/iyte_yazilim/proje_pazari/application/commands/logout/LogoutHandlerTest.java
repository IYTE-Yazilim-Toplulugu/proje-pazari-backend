package com.iyte_yazilim.proje_pazari.application.commands.logout;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRefreshTokenService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.ITokenService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutHandlerTest {

    @Mock private IRefreshTokenService refreshTokenService;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private ITokenService tokenService;
    @Mock private MessageService messageService;

    @InjectMocks private LogoutHandler logoutHandler;

    private static final String USER_ID = "user-123";
    private static final String REFRESH_TOKEN = "valid-refresh-token";
    private static final String ACCESS_TOKEN = "valid.access.token";

    @BeforeEach
    void setUp() {
        lenient()
                .when(messageService.getMessage("auth.logout.success"))
                .thenReturn("Logout successful");
        lenient().when(messageService.getMessage("auth.token.invalid")).thenReturn("Invalid token");
    }

    @Test
    @DisplayName("Should revoke refresh token and blacklist access token on logout")
    void shouldLogout_WhenBothTokensAreValid() {
        long futureMillis = System.currentTimeMillis() + 60_000;
        Date expiration = new Date(futureMillis);

        when(refreshTokenService.validateAndRevoke(REFRESH_TOKEN)).thenReturn(Optional.of(USER_ID));
        when(tokenService.extractExpiration(ACCESS_TOKEN)).thenReturn(expiration);

        ApiResponse<Void> response =
                logoutHandler.handle(new LogoutCommand(ACCESS_TOKEN, REFRESH_TOKEN, USER_ID));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Logout successful", response.getMessage());
        verify(refreshTokenService).validateAndRevoke(REFRESH_TOKEN);
        verify(tokenBlacklistService).blacklistToken(eq(ACCESS_TOKEN), any(Duration.class));
    }

    @Test
    @DisplayName("Should revoke refresh token even when access token is null")
    void shouldRevokeRefreshToken_WhenAccessTokenIsNull() {
        when(refreshTokenService.validateAndRevoke(REFRESH_TOKEN)).thenReturn(Optional.of(USER_ID));

        ApiResponse<Void> response =
                logoutHandler.handle(new LogoutCommand(null, REFRESH_TOKEN, USER_ID));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(refreshTokenService).validateAndRevoke(REFRESH_TOKEN);
        verify(tokenBlacklistService, never()).blacklistToken(any(), any());
    }

    @Test
    @DisplayName("Should skip Redis blacklisting when access token is already expired")
    void shouldSkipBlacklist_WhenAccessTokenIsExpired() {
        Date pastExpiration = new Date(System.currentTimeMillis() - 60_000);

        when(refreshTokenService.validateAndRevoke(REFRESH_TOKEN)).thenReturn(Optional.of(USER_ID));
        when(tokenService.extractExpiration(ACCESS_TOKEN)).thenReturn(pastExpiration);

        ApiResponse<Void> response =
                logoutHandler.handle(new LogoutCommand(ACCESS_TOKEN, REFRESH_TOKEN, USER_ID));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(refreshTokenService).validateAndRevoke(REFRESH_TOKEN);
        verify(tokenBlacklistService, never()).blacklistToken(any(), any());
    }

    @Test
    @DisplayName("Should skip Redis blacklisting when access token is malformed")
    void shouldSkipBlacklist_WhenAccessTokenIsMalformed() {
        when(refreshTokenService.validateAndRevoke(REFRESH_TOKEN)).thenReturn(Optional.of(USER_ID));
        when(tokenService.extractExpiration(ACCESS_TOKEN))
                .thenThrow(new RuntimeException("Malformed JWT"));

        ApiResponse<Void> response =
                logoutHandler.handle(new LogoutCommand(ACCESS_TOKEN, REFRESH_TOKEN, USER_ID));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(refreshTokenService).validateAndRevoke(REFRESH_TOKEN);
        verify(tokenBlacklistService, never()).blacklistToken(any(), any());
    }

    @Test
    @DisplayName("Should throw ValidationException when refresh token is not found or expired")
    void shouldThrow_WhenRefreshTokenNotFound() {
        when(refreshTokenService.validateAndRevoke(REFRESH_TOKEN)).thenReturn(Optional.empty());

        assertThrows(
                ValidationException.class,
                () ->
                        logoutHandler.handle(
                                new LogoutCommand(ACCESS_TOKEN, REFRESH_TOKEN, USER_ID)));

        verify(refreshTokenService, never()).revokeRefreshToken(any());
        verify(tokenBlacklistService, never()).blacklistToken(any(), any());
    }

    @Test
    @DisplayName("Should throw ValidationException when refresh token belongs to a different user")
    void shouldThrow_WhenRefreshTokenOwnershipMismatch() {
        when(refreshTokenService.validateAndRevoke(REFRESH_TOKEN))
                .thenReturn(Optional.of("different-user-456"));

        assertThrows(
                ValidationException.class,
                () ->
                        logoutHandler.handle(
                                new LogoutCommand(ACCESS_TOKEN, REFRESH_TOKEN, USER_ID)));

        verify(refreshTokenService, never()).revokeRefreshToken(any());
        verify(tokenBlacklistService, never()).blacklistToken(any(), any());
    }
}
