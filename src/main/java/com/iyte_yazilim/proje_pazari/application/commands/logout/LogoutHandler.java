package com.iyte_yazilim.proje_pazari.application.commands.logout;

import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.security.service.RefreshTokenService;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutHandler implements IRequestHandler<LogoutCommand, ApiResponse<Void>> {

    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;
    private final MessageService messageService;

    @Override
    public ApiResponse<Void> handle(LogoutCommand command) {
        String refreshToken = command.refreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ValidationException("Refresh token is required");
        }

        // Revoke the refresh token from DB — this is the primary logout action.
        // We revoke regardless of whether the access token is still valid.
        refreshTokenService.revokeRefreshToken(refreshToken);
        log.info("Refresh token revoked during logout");

        // Blacklist the access token in Redis so it cannot be used until it expires naturally.
        // If the access token is already expired or invalid, we skip this step — it won't be
        // accepted anyway.
        String accessToken = command.accessToken();
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                Date expiration = jwtUtil.extractExpiration(accessToken);
                long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                if (remainingMillis > 0) {
                    tokenBlacklistService.blacklistToken(
                            accessToken, Duration.ofMillis(remainingMillis));
                    log.info("Access token blacklisted during logout, TTL={}ms", remainingMillis);
                }
            } catch (Exception e) {
                // Token is already expired or malformed — no need to blacklist it.
                log.debug(
                        "Access token not blacklisted (expired or malformed): {}", e.getMessage());
            }
        }

        return ApiResponse.success(null, messageService.getMessage("auth.logout.success"));
    }
}
