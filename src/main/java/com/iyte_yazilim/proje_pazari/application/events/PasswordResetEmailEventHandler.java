package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.PasswordResetEmailRequestedEvent;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Dispatches the password-reset email only after the token has been durably committed, preventing a
 * race condition where the email link is clicked before the token is visible to other transactions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetEmailEventHandler {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PasswordResetEmailRequestedEvent event) {
        log.info("Dispatching password reset email for user: {}", event.getUserId());
        try {
            emailService.sendTemplateEmailAsync(
                    event.getEmail(),
                    "password-reset.html",
                    Map.of(
                            "subject",
                            "Şifre Sıfırlama / Password Reset",
                            "firstName",
                            event.getFirstName(),
                            "resetLink",
                            event.getResetLink()));
        } catch (Exception e) {
            log.error(
                    "Failed to dispatch password reset email for user {}: {}",
                    event.getUserId(),
                    e.getMessage(),
                    e);
        }
    }
}
