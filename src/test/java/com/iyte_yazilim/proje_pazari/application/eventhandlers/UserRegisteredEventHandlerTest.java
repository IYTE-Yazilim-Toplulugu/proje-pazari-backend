package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.application.services.FrontendVerificationLinkBuilder;
import com.iyte_yazilim.proje_pazari.domain.events.UserRegisteredEvent;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegisteredEventHandler Tests")
class UserRegisteredEventHandlerTest {

    @Mock private EmailService emailService;
    @Mock private FrontendVerificationLinkBuilder verificationLinkBuilder;

    @InjectMocks private UserRegisteredEventHandler eventHandler;

    @Captor private ArgumentCaptor<String> emailCaptor;

    @Captor private ArgumentCaptor<String> templateCaptor;

    @Captor private ArgumentCaptor<Map<String, Object>> variablesCaptor;

    @BeforeEach
    void setUp() {
        lenient()
                .when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        lenient()
                .when(verificationLinkBuilder.build(anyString()))
                .thenAnswer(
                        invocation ->
                                "http://localhost:3000/verify-email?token="
                                        + invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should send welcome email when user registered event is received")
    void shouldSendWelcomeEmailOnUserRegistered() {
        // Given
        Ulid userId = Ulid.fast();
        String email = "newuser@example.com";
        String firstName = "John";
        String verificationToken = "abc123xyz";

        UserRegisteredEvent event =
                new UserRegisteredEvent(userId, email, firstName, verificationToken);

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService, times(1))
                .sendTemplateEmailAsync(
                        emailCaptor.capture(), templateCaptor.capture(), variablesCaptor.capture());

        assertEquals(email, emailCaptor.getValue());
        assertEquals("welcome.html", templateCaptor.getValue());

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("Welcome to Proje Pazarı!", variables.get("subject"));
        assertEquals(firstName, variables.get("userName"));
        assertEquals(
                "http://localhost:3000/verify-email?token=" + verificationToken,
                variables.get("verificationLink"));
    }

    @Test
    @DisplayName("Should handle email service exception gracefully")
    void shouldHandleEmailServiceExceptionGracefully() {
        // Given
        when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(
                        CompletableFuture.failedFuture(
                                new RuntimeException("Email service unavailable")));

        UserRegisteredEvent event =
                new UserRegisteredEvent(Ulid.fast(), "test@example.com", "Test", "token-123");

        // When & Then
        assertDoesNotThrow(() -> eventHandler.handle(event));
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should include correct verification link format")
    void shouldIncludeCorrectVerificationLinkFormat() {
        // Given
        String verificationToken = "test-token-12345";
        UserRegisteredEvent event =
                new UserRegisteredEvent(
                        Ulid.fast(), "test@example.com", "Test User", verificationToken);

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService).sendTemplateEmailAsync(any(), any(), variablesCaptor.capture());

        String verificationLink = (String) variablesCaptor.getValue().get("verificationLink");
        assertEquals(
                "http://localhost:3000/verify-email?token=" + verificationToken, verificationLink);
    }
}
