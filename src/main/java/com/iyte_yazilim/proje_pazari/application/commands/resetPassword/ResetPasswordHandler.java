package com.iyte_yazilim.proje_pazari.application.commands.resetPassword;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.InvalidVerificationTokenException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.VerificationTokenExpiredException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.PasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PasswordResetTokenEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.security.service.RefreshTokenService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResetPasswordHandler
        implements IRequestHandler<ResetPasswordCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final MessageService messageService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<Void> handle(ResetPasswordCommand command) {

        // --- 1. Validate token exists ---
        PasswordResetTokenEntity resetToken =
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
        if (resetToken.isExpired()) {
            throw new VerificationTokenExpiredException(
                    messageService.getMessage("auth.password.reset.token.expired"));
        }

        // --- 4. Validate password confirmation ---
        if (!command.newPassword().equals(command.confirmPassword())) {
            return ApiResponse.validationError(
                    messageService.getMessage("validation.password.mismatch"));
        }

        // --- 5. Validate password strength ---
        if (!isPasswordStrong(command.newPassword())) {
            return ApiResponse.validationError(
                    messageService.getMessage("validation.password.requirements"));
        }

        // --- 6. Load user ---
        UserEntity user =
                userRepository
                        .findById(resetToken.getUserId())
                        .orElseThrow(() -> new UserNotFoundException(resetToken.getUserId()));

        // --- 7. Hash and save new password ---
        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);

        // --- 8. Mark token as used ---
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        // --- 9. Revoke all refresh tokens — force re-login on all devices ---
        refreshTokenService.revokeAllUserTokens(user.getId());

        log.info("Password reset completed for user: {}", user.getId());

        return ApiResponse.success(null, messageService.getMessage("auth.password.reset.success"));
    }

    private boolean isPasswordStrong(String password) {
        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }
}
