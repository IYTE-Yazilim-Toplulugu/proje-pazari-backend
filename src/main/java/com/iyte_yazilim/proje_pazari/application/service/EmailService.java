package com.iyte_yazilim.proje_pazari.application.service;

import com.iyte_yazilim.proje_pazari.application.dtos.EmailDto;
import com.iyte_yazilim.proje_pazari.domain.exceptions.EmailSendException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    /**
     * Send email synchronously (for critical emails)
     */
    public void sendEmail(EmailDto emailDto) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailDto.getTo());
            helper.setSubject(emailDto.getSubject());
            helper.setText(emailDto.getBody(), true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", emailDto.getTo());

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", emailDto.getTo(), e);
            throw new EmailSendException("Failed to send email", e);
        }
    }

    /**
     * Send email asynchronously with CompletableFuture
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendEmailAsync(EmailDto emailDto) {
        log.info("Starting async email send to: {}", emailDto.getTo());
        try {
            sendEmail(emailDto);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Async email send failed to: {}", emailDto.getTo(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Send email using Thymeleaf template (synchronous)
     */
    public void sendTemplateEmail(String to, String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        String htmlContent = templateEngine.process("emails/" + templateName, context);

        String subject = (String) variables.getOrDefault("subject", "Notification from Proje Pazarı");
        EmailDto email = new EmailDto(to, subject, htmlContent);
        sendEmail(email);
    }

    /**
     * Send email using Thymeleaf template (asynchronous)
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendTemplateEmailAsync(String to, String templateName, Map<String, Object> variables) {
        log.info("Starting async template email to: {} with template: {}", to, templateName);
        try {
            sendTemplateEmail(to, templateName, variables);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Async template email failed to: {} with template: {}", to, templateName, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    // ==================== EVENT-SPECIFIC EMAIL METHODS ====================

    /**
     * Send welcome email (async)
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendWelcomeEmail(String to, String username) {
        Map<String, Object> variables = Map.of(
                "subject", "Welcome to Proje Pazarı!",
                "username", username
        );
        return sendTemplateEmailAsync(to, "welcome", variables);
    }

    /**
     * Send verification email (async)
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendVerificationEmail(String to, String username, String verificationLink) {
        Map<String, Object> variables = Map.of(
                "subject", "Verify Your Email",
                "username", username,
                "verificationLink", verificationLink
        );
        return sendTemplateEmailAsync(to, "verification", variables);
    }

    /**
     * Send application received email (async)
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendApplicationReceivedEmail(String to, String username, String projectName) {
        Map<String, Object> variables = Map.of(
                "subject", "Application Received",
                "username", username,
                "projectName", projectName
        );
        return sendTemplateEmailAsync(to, "application-received", variables);
    }

    /**
     * Send application approved email (async)
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendApplicationApprovedEmail(String to, String username, String projectName) {
        Map<String, Object> variables = Map.of(
                "subject", "Application Approved - Congratulations!",
                "username", username,
                "projectName", projectName
        );
        return sendTemplateEmailAsync(to, "application-approved", variables);
    }

    /**
     * Send application rejected email (async)
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendApplicationRejectedEmail(String to, String username, String projectName) {
        Map<String, Object> variables = Map.of(
                "subject", "Application Status Update",
                "username", username,
                "projectName", projectName
        );
        return sendTemplateEmailAsync(to, "application-rejected", variables);
    }

    /**
     * Send project status changed email (async)
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendProjectStatusChangedEmail(String to, String username, String projectName, String newStatus) {
        Map<String, Object> variables = Map.of(
                "subject", "Project Status Changed",
                "username", username,
                "projectName", projectName,
                "newStatus", newStatus
        );
        return sendTemplateEmailAsync(to, "project-status-changed", variables);
    }

    /**
     * Send multiple emails in parallel
     */
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendBulkEmails(Map<String, EmailDto> recipients) {
        log.info("Starting bulk email send to {} recipients", recipients.size());

        CompletableFuture<?>[] futures = recipients.entrySet().stream()
                .map(entry -> sendEmailAsync(entry.getValue()))
                .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures)
                .thenRun(() -> log.info("Bulk email send completed"))
                .exceptionally(ex -> {
                    log.error("Bulk email send failed", ex);
                    return null;
                });
    }
}