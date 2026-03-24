package com.iyte_yazilim.proje_pazari.application.commands.forgotPassword;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.application.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.PasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PasswordResetTokenEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForgotPasswordHandler
        implements IRequestHandler<ForgotPasswordCommand, ApiResponse<Void>> {

    private static final int RESET_TOKEN_EXPIRY_HOURS = 1;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final MessageService messageService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Transactional
    public ApiResponse<Void> handle(ForgotPasswordCommand command) {

        // --- 1. Look up user — always return success to prevent user enumeration ---
        Optional<UserEntity> userOpt = userRepository.findByEmail(command.email());

        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", command.email());
            return ApiResponse.success(
                    null, messageService.getMessage("auth.password.reset.email.sent"));
        }

        UserEntity user = userOpt.get();

        // --- 2. Delete any existing tokens for this user ---
        passwordResetTokenRepository.deleteByUserId(user.getId());

        // --- 3. Generate new reset token (1-hour TTL) ---
        String token = verificationTokenService.generateToken();

        PasswordResetTokenEntity resetToken = new PasswordResetTokenEntity();
        resetToken.setUserId(user.getId());
        resetToken.setEmail(user.getEmail());
        resetToken.setToken(token);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(RESET_TOKEN_EXPIRY_HOURS));

        passwordResetTokenRepository.save(resetToken);

        // --- 4. Send password reset email asynchronously ---
        String resetLink = frontendUrl + "/reset_password?token=" + token;

        emailService.sendTemplateEmailAsync(
                user.getEmail(),
                "password-reset.html",
                Map.of(
                        "subject",
                        "Şifre Sıfırlama / Password Reset",
                        "firstName",
                        user.getFirstName(),
                        "resetLink",
                        resetLink));

        log.info("Password reset email sent to user: {}", user.getId());

        return ApiResponse.success(
                null, messageService.getMessage("auth.password.reset.email.sent"));
    }
}
