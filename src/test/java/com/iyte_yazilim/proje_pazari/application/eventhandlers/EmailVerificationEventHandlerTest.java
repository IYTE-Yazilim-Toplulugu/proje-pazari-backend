package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.services.FrontendVerificationLinkBuilder;
import com.iyte_yazilim.proje_pazari.domain.events.UserRegisteredEvent;
import com.iyte_yazilim.proje_pazari.domain.events.VerificationEmailRequestedEvent;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@ExtendWith(MockitoExtension.class)
class EmailVerificationEventHandlerTest {

    @Mock private JavaMailSender mailSender;
    @Mock private TemplateEngine templateEngine;
    @Mock private FrontendVerificationLinkBuilder verificationLinkBuilder;

    private EmailVerificationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler =
                new EmailVerificationEventHandler(
                        mailSender, templateEngine, verificationLinkBuilder);
        ReflectionTestUtils.setField(handler, "fromEmail", "noreply@projepazari.site");

        when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getInstance(new Properties())));
        when(templateEngine.process(eq("verification"), any(Context.class)))
                .thenReturn("<html>Verification email</html>");
    }

    @Test
    @DisplayName("Initial registration email should contain frontend verification link")
    void shouldUseFrontendLinkForInitialRegistration() {
        String token = "registration-token";
        String verificationUrl = "https://projepazari.site/verify-email?token=registration-token";
        when(verificationLinkBuilder.build(token)).thenReturn(verificationUrl);

        handler.handleUserRegistered(
                new UserRegisteredEvent(
                        Ulid.fast(), "new-user@std.iyte.edu.tr", "New User", token));

        assertVerificationEmail(token, verificationUrl);
    }

    @Test
    @DisplayName("Resent verification email should contain frontend verification link")
    void shouldUseFrontendLinkForResend() {
        String token = "resend-token";
        String verificationUrl = "https://projepazari.site/verify-email?token=resend-token";
        when(verificationLinkBuilder.build(token)).thenReturn(verificationUrl);

        handler.handleVerificationEmailRequested(
                new VerificationEmailRequestedEvent(
                        Ulid.fast(), "existing-user@std.iyte.edu.tr", "Existing User", token));

        assertVerificationEmail(token, verificationUrl);
    }

    private void assertVerificationEmail(String token, String verificationUrl) {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);

        verify(verificationLinkBuilder).build(token);
        verify(templateEngine).process(eq("verification"), contextCaptor.capture());
        verify(mailSender).send(any(MimeMessage.class));
        assertEquals(verificationUrl, contextCaptor.getValue().getVariable("verificationLink"));
    }
}
