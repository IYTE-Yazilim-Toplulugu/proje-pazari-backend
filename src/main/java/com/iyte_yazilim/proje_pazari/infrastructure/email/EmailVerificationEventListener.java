package com.iyte_yazilim.proje_pazari.infrastructure.email;

import com.iyte_yazilim.proje_pazari.domain.events.UserRegisteredEvent;
import com.iyte_yazilim.proje_pazari.domain.events.VerificationEmailRequestedEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationEventListener {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.email.from:noreply@projepazari.com}")
    private String fromEmail;

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Sending verification email to: {}", event.getEmail());
        sendVerificationEmail(event.getEmail(), event.getFirstName(), event.getVerificationToken());
    }

    @Async
    @EventListener
    public void handleVerificationEmailRequested(VerificationEmailRequestedEvent event) {
        log.info("Resending verification email to: {}", event.getEmail());
        sendVerificationEmail(event.getEmail(), event.getFirstName(), event.getVerificationToken());
    }

    private void sendVerificationEmail(String toEmail, String userName, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your email - Proje Pazarı");

            // Build verification link
            String verificationLink = frontendUrl + "/api/v1/auth/verify-email?token=" + token;

            // Prepare template context
            Context context = new Context();
            context.setVariable("userName", userName);
            context.setVariable("verificationLink", verificationLink);

            // Process template
            String htmlContent = templateEngine.process("verification", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
