package com.iyte_yazilim.proje_pazari.application.commands.resetPassword;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.PasswordResetToken;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.exceptions.InvalidVerificationTokenException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.VerificationTokenExpiredException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IPasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRefreshTokenService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IUserRepository;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResetPasswordHandler
        implements IRequestHandler<ResetPasswordCommand, ApiResponse<Void>> {

    private final IUserRepository userRepository;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final IRefreshTokenService refreshTokenService;
    private final MessageService messageService;
    private final Clock clock;

    @Override
    @Transactional(timeoutString = "${spring.transaction.timeout:30}")
    public ApiResponse<Void> handle(ResetPasswordCommand command) {

        // --- 1. Validate token exists ---
        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByToken(command.token())
                        .orElseThrow(
                                () ->
                                        new InvalidVerificationTokenException(
                                                messageService.getMessage(
                                                        "auth.password.reset.token.invalid")));

        // --- 2. Validate token not already used ---
        if (resetToken.isUsed()) {
            throw new InvalidVerificationTokenException(
                    messageService.getMessage("auth.password.reset.token.invalid"));
        }

        // --- 3. Validate token not expired ---
        if (resetToken.isExpired(clock)) {
            throw new VerificationTokenExpiredException(
                    messageService.getMessage("auth.password.reset.token.expired"));
        }

        // --- 4. Load user ---
        User user =
                userRepository
                        .findById(resetToken.getUserId())
                        .orElseThrow(() -> new UserNotFoundException(resetToken.getUserId()));

        // --- 5. Hash and save new password ---
        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);

        // --- 6. Mark token as used ---
        resetToken.setUsedAt(LocalDateTime.now(clock));
        passwordResetTokenRepository.save(resetToken);

        // --- 7. Revoke all refresh tokens — force re-login on all devices ---
        refreshTokenService.revokeAllUserTokens(resetToken.getUserId());

        log.info("Password reset completed for user: {}", resetToken.getUserId());

        return ApiResponse.success(null, messageService.getMessage("auth.password.reset.success"));
    }
}
