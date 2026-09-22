package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.events.UserDeactivatedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRefreshTokenService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserSessionRevokedEventHandlerTest {

    @Mock private IRefreshTokenService refreshTokenService;
    @Mock private TokenBlacklistService tokenBlacklistService;

    @InjectMocks private UserSessionRevokedEventHandler handler;

    private static final String USER_ID = "user-123";
    private static final String EMAIL = "user@std.iyte.edu.tr";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "accessTokenTtlSeconds", 86400L);
    }

    @Test
    @DisplayName("Deactivated event revokes refresh tokens and blacklists user")
    void handleUserDeactivated_revokesSessionsAndBlacklists() {
        UserDeactivatedEvent event = new UserDeactivatedEvent(USER_ID, EMAIL);

        handler.handleUserDeactivated(event);

        verify(refreshTokenService).revokeAllUserTokens(USER_ID);
        verify(tokenBlacklistService).blacklistUser(eq(EMAIL), any(Duration.class));
    }

    @Test
    @DisplayName("Exception from refreshTokenService is caught and logged, no rethrow")
    void handleUserDeactivated_refreshServiceThrows_doesNotPropagate() {
        UserDeactivatedEvent event = new UserDeactivatedEvent(USER_ID, EMAIL);
        doThrow(new RuntimeException("Redis down"))
                .when(refreshTokenService)
                .revokeAllUserTokens(USER_ID);

        handler.handleUserDeactivated(event);

        verify(tokenBlacklistService, never()).blacklistUser(any(), any());
    }

    @Test
    @DisplayName("Exception from tokenBlacklistService is caught and logged, no rethrow")
    void handleUserDeactivated_blacklistServiceThrows_doesNotPropagate() {
        UserDeactivatedEvent event = new UserDeactivatedEvent(USER_ID, EMAIL);
        doThrow(new RuntimeException("Redis down"))
                .when(tokenBlacklistService)
                .blacklistUser(any(), any());

        handler.handleUserDeactivated(event);

        verify(refreshTokenService).revokeAllUserTokens(USER_ID);
    }
}
