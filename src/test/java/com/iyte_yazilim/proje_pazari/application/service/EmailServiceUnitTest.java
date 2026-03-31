package com.iyte_yazilim.proje_pazari.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.dtos.EmailDto;
import com.iyte_yazilim.proje_pazari.domain.exceptions.EmailSendException;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@ExtendWith(MockitoExtension.class)
class EmailServiceUnitTest {

    @Mock private JavaMailSender mailSender;
    @Mock private TemplateEngine templateEngine;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Should send email via JavaMailSender on success")
    void sendEmail_success_callsMailSenderSend() {
        emailService.sendEmail(new EmailDto("to@test.com", "Subject", "Body"));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should throw EmailSendException when MessagingException occurs")
    void sendEmail_messagingException_throwsEmailSendException() throws MessagingException {
        doThrow(new MessagingException("error")).when(mimeMessage).setFrom(any(Address.class));

        assertThrows(
                EmailSendException.class,
                () -> emailService.sendEmail(new EmailDto("to@test.com", "Subject", "Body")));
    }

    @Test
    @DisplayName("Should process template and send email synchronously")
    void sendTemplateEmail_processesTemplateAndSendsEmail() {
        when(templateEngine.process(eq("emails/myTemplate"), any(Context.class)))
                .thenReturn("<html>content</html>");

        emailService.sendTemplateEmail("to@test.com", "myTemplate", Map.of("subject", "My Subject"));

        verify(templateEngine).process(eq("emails/myTemplate"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should use default subject when 'subject' key is absent from variables")
    void sendTemplateEmail_noSubjectInVars_usesDefaultSubject() {
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html/>");

        assertDoesNotThrow(
                () -> emailService.sendTemplateEmail("to@test.com", "tmpl", Map.of()));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should return completed future on successful async template send")
    void sendTemplateEmailAsync_success_returnsCompletedFuture() {
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html/>");

        CompletableFuture<Void> result =
                emailService.sendTemplateEmailAsync(
                        "to@test.com", "tmpl", Map.of("subject", "Subj"));

        assertNotNull(result);
        assertFalse(result.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Should use 'emails/welcome' template for welcome emails")
    void sendWelcomeEmail_usesWelcomeTemplate() {
        when(templateEngine.process(eq("emails/welcome"), any(Context.class)))
                .thenReturn("<html>Welcome!</html>");

        emailService.sendWelcomeEmail("user@test.com", "John");

        verify(templateEngine).process(eq("emails/welcome"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should use 'emails/verification' template for verification emails")
    void sendVerificationEmail_usesVerificationTemplate() {
        when(templateEngine.process(eq("emails/verification"), any(Context.class)))
                .thenReturn("<html>Verify</html>");

        emailService.sendVerificationEmail("user@test.com", "John", "https://verify.link");

        verify(templateEngine).process(eq("emails/verification"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should use 'emails/application-received' template")
    void sendApplicationReceivedEmail_usesCorrectTemplate() {
        when(templateEngine.process(eq("emails/application-received"), any(Context.class)))
                .thenReturn("<html/>");

        emailService.sendApplicationReceivedEmail("user@test.com", "John", "Project X");

        verify(templateEngine).process(eq("emails/application-received"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should use 'emails/application-approved' template")
    void sendApplicationApprovedEmail_usesCorrectTemplate() {
        when(templateEngine.process(eq("emails/application-approved"), any(Context.class)))
                .thenReturn("<html/>");

        emailService.sendApplicationApprovedEmail("user@test.com", "John", "Project X");

        verify(templateEngine).process(eq("emails/application-approved"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should use 'emails/application-rejected' template")
    void sendApplicationRejectedEmail_usesCorrectTemplate() {
        when(templateEngine.process(eq("emails/application-rejected"), any(Context.class)))
                .thenReturn("<html/>");

        emailService.sendApplicationRejectedEmail("user@test.com", "John", "Project X");

        verify(templateEngine).process(eq("emails/application-rejected"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should use 'emails/project-status-changed' template")
    void sendProjectStatusChangedEmail_usesCorrectTemplate() {
        when(templateEngine.process(eq("emails/project-status-changed"), any(Context.class)))
                .thenReturn("<html/>");

        emailService.sendProjectStatusChangedEmail("user@test.com", "John", "Project X", "OPEN");

        verify(templateEngine).process(eq("emails/project-status-changed"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send email to every recipient in the map")
    void sendBulkEmails_sendsToAllRecipients() {
        Map<String, EmailDto> recipients =
                Map.of(
                        "user1", new EmailDto("u1@test.com", "Sub", "Body"),
                        "user2", new EmailDto("u2@test.com", "Sub", "Body"));

        emailService.sendBulkEmails(recipients);

        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }
}
