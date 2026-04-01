package com.iyte_yazilim.proje_pazari.application.commands.forgotPassword;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.application.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.domain.entities.PasswordResetToken;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.events.PasswordResetEmailRequestedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IPasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IUserRepository;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForgotPasswordHandler
        implements IRequestHandler<ForgotPasswordCommand, ApiResponse<Void>> {

    private static final int RESET_TOKEN_EXPIRY_HOURS = 1;

    private final IUserRepository userRepository;
    private final IPasswordResetTokenRepository passwordResetTokenRepository;
    private final VerificationTokenService verificationTokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageService messageService;
    private final Clock clock;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.frontend.reset-password-path:/reset-password}")
    private String resetPasswordPath;

    @Override
    @Transactional
    public ApiResponse<Void> handle(ForgotPasswordCommand command) {

        // --- 1. Look up user — always return success to prevent user enumeration ---
        Optional<User> userOpt = userRepository.findByEmail(command.email());

        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existent email address");
            return ApiResponse.success(
                    null, messageService.getMessage("auth.password.reset.email.sent"));
        }

        User user = userOpt.get();

        // --- 2. Delete any existing tokens for this user ---
        passwordResetTokenRepository.deleteByUserId(user.getId().toString());

        // --- 3. Generate new reset token (1-hour TTL) ---
        String token = verificationTokenService.generateToken();

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        user.getId().toString(),
                        user.getEmail(),
                        token,
                        LocalDateTime.now(clock).plusHours(RESET_TOKEN_EXPIRY_HOURS));

        passwordResetTokenRepository.save(resetToken);

        // --- 4. Publish event — email is dispatched after the transaction commits so the token
        //        is durably persisted before the user can click the link ---
        String resetLink =
                UriComponentsBuilder.fromUriString(frontendUrl)
                        .path(resetPasswordPath)
                        .queryParam("token", token)
                        .build()
                        .toUriString();

        eventPublisher.publishEvent(
                new PasswordResetEmailRequestedEvent(
                        user.getId().toString(), user.getEmail(), user.getFirstName(), resetLink));

        log.info("Password reset token saved for user: {}", user.getId());

        return ApiResponse.success(
                null, messageService.getMessage("auth.password.reset.email.sent"));
    }
}
