package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.application.services.FrontendVerificationLinkBuilder;
import com.iyte_yazilim.proje_pazari.domain.events.UserRegisteredEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Application event handler for user registration.
 *
 * <p>Handles the email verification flow when a new user registers. This is an application-level
 * concern that coordinates domain events with infrastructure services (email).
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-01-16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredEventHandler implements IEventHandler<UserRegisteredEvent> {

    private final EmailService emailService;
    private final FrontendVerificationLinkBuilder verificationLinkBuilder;

    @Override
    @Async // ← Email gönderme async olmalı
    @EventListener
    public void handle(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for user: {}", event.getEmail());

        try {
            Map<String, Object> variables =
                    Map.of(
                            "subject",
                            "Welcome to Proje Pazarı!",
                            "userName",
                            event.getFirstName(),
                            "verificationLink",
                            verificationLinkBuilder.build(event.getVerificationToken()));

            emailService.sendTemplateEmailAsync(event.getEmail(), "welcome.html", variables);

            log.info("Verification email sent successfully to: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to: {}", event.getEmail(), e);
        }
    }
}
