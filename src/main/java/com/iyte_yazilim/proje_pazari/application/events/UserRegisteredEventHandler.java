package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.UserRegisteredEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handles UserRegisteredEvent by sending a welcome email.
 *
 * <p>This handler is part of the application layer and coordinates
 * the email sending process when a new user registers.
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

    @Override
    @EventListener
    public void handle(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for user: {}", event.email());

        Map<String, Object> variables = Map.of(
                "subject", "Welcome to Proje Pazarı!",
                "userName", event.firstName(),
                "verificationLink", "http://localhost:3000/verify?token=" + event.verificationToken());

        emailService.sendTemplateEmailAsync(
                event.email(),
                "welcome.html",
                variables);
    }
}
