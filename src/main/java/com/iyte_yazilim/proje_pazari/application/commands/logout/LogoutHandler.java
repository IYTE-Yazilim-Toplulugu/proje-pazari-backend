package com.iyte_yazilim.proje_pazari.application.commands.logout;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRefreshTokenService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.ITokenService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogoutHandler implements IRequestHandler<LogoutCommand, ApiResponse<Void>> {

    private final IRefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ITokenService tokenService;
    private final MessageService messageService;

    @Override
    public ApiResponse<Void> handle(LogoutCommand command) {
        String refreshToken = command.refreshToken();

        // Validate and revoke the refresh token in a single transaction.
        // Returns empty if the token does not exist, is expired, or is already revoked —
        // all cases where we should not silently return 200.
        Optional<String> ownerIdOpt = refreshTokenService.validateAndRevoke(refreshToken);
        if (ownerIdOpt.isEmpty()) {
            throw new ValidationException(messageService.getMessage("auth.token.invalid"));
        }

        // Ownership check — the token must belong to the authenticated user.
        String tokenOwnerId = ownerIdOpt.get();
        if (!tokenOwnerId.equals(command.userId())) {
            throw new ValidationException(messageService.getMessage("auth.token.invalid"));
        }

        log.info("Refresh token revoked for user: {}", command.userId());

        // Blacklist the access token in Redis so it cannot be reused until it expires naturally.
        // If the access token is already expired or malformed, we skip — it is rejected by the
        // JwtAuthenticationFilter regardless.
        String accessToken = command.accessToken();
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                Date expiration = tokenService.extractExpiration(accessToken);
                long remainingMillis = expiration.getTime() - System.currentTimeMillis();
                if (remainingMillis > 0) {
                    tokenBlacklistService.blacklistToken(
                            accessToken, Duration.ofMillis(remainingMillis));
                    log.info(
                            "Access token blacklisted for user: {}, TTL={}ms",
                            command.userId(),
                            remainingMillis);
                }
            } catch (Exception e) {
                log.debug(
                        "Access token not blacklisted (expired or malformed): {}", e.getMessage());
            }
        }

        return ApiResponse.success(null, messageService.getMessage("auth.logout.success"));
    }
}
