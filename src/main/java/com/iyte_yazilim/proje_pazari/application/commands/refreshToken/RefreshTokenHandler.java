package com.iyte_yazilim.proje_pazari.application.commands.refreshToken;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRefreshTokenService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.ITokenService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenHandler
        implements IRequestHandler<RefreshTokenCommand, ApiResponse<RefreshTokenResult>> {

    private final IRefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final ITokenService tokenService;
    private final MessageService messageService;

    @Override
    public ApiResponse<RefreshTokenResult> handle(RefreshTokenCommand command) {
        String refreshToken = command.refreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ValidationException("Refresh token is required");
        }

        var userIdOpt = refreshTokenService.validateAndRevoke(refreshToken);
        if (userIdOpt.isEmpty()) {
            throw new ValidationException("Invalid or expired refresh token");
        }

        String userId = userIdOpt.get();
        UserEntity user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(userId));
        String newRefreshToken = refreshTokenService.createRefreshToken(userId);
        String role = user.getRoles().contains(RoleType.ADMIN) ? "ADMIN" : "USER";
        String newAccessToken = tokenService.generateToken(user.getId(), user.getEmail(), role);

        var result =
                new RefreshTokenResult(
                        user.getId(), user.getEmail(), role, newAccessToken, newRefreshToken);

        return ApiResponse.success(result, messageService.getMessage("auth.token.refreshed"));
    }
}
