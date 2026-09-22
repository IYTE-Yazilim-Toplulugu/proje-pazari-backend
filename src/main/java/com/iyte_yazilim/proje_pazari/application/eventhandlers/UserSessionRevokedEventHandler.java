package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import com.iyte_yazilim.proje_pazari.domain.events.UserDeactivatedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRefreshTokenService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Revokes all active sessions when a user is deactivated or admin-deleted.
 *
 * <p>Listens to {@link UserDeactivatedEvent} (published by both {@code DeactivateAccountHandler}
 * and {@code AdminDeleteUserHandler}). Each listener fires asynchronously after the originating
 * transaction commits; email failures are isolated inside.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserSessionRevokedEventHandler {

    private final IRefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${security.access-token.ttl-seconds:86400}")
    private long accessTokenTtlSeconds;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserDeactivated(UserDeactivatedEvent event) {
        revokeAllSessions(event.getUserId(), event.getEmail());
    }

    private void revokeAllSessions(String userId, String email) {
        try {
            refreshTokenService.revokeAllUserTokens(userId);
            tokenBlacklistService.blacklistUser(email, Duration.ofSeconds(accessTokenTtlSeconds));
            log.info("Revoked all sessions for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to revoke sessions for user {}: {}", userId, e.getMessage(), e);
        }
    }
}
