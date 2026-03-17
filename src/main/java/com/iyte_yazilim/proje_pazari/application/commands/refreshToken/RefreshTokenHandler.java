package com.iyte_yazilim.proje_pazari.application.commands.refreshToken;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.security.service.RefreshTokenService;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenHandler
        implements IRequestHandler<RefreshTokenCommand, ApiResponse<RefreshTokenResult>> {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final MessageService messageService;

    @Override
    public ApiResponse<RefreshTokenResult> handle(RefreshTokenCommand command) {
        String refreshToken = command.refreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ValidationException("Refresh token is required");
        }

        var userIdOpt = refreshTokenService.validateRefreshToken(refreshToken);
        if (userIdOpt.isEmpty()) {
            throw new ValidationException("Invalid or expired refresh token");
        }

        String userId = userIdOpt.get();
        UserEntity user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(userId));

        refreshTokenService.revokeRefreshToken(refreshToken);
        String newRefreshToken = refreshTokenService.createRefreshToken(userId);
        String role = user.getRole() != null ? user.getRole().toString() : "APPLICANT";
        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), role);

        var result =
                new RefreshTokenResult(
                        user.getId(), user.getEmail(), role, newAccessToken, newRefreshToken);

        return ApiResponse.success(result, messageService.getMessage("auth.token.refreshed"));
    }
}
