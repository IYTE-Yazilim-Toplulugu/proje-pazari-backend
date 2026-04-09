package com.iyte_yazilim.proje_pazari.application.commands.invalidateUserSessions;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.RefreshTokenRepository;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvalidateUserSessionsHandler
        implements IRequestHandler<InvalidateUserSessionsCommand, ApiResponse<Void>> {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(InvalidateUserSessionsCommand command) {
        if (command.userId() == null || command.userId().isBlank()) {
            return ApiResponse.validationError("User ID is required");
        }

        refreshTokenRepository.revokeAllUserTokens(command.userId());
        return ApiResponse.success(null, "All sessions invalidated for user: " + command.userId());
    }
}
