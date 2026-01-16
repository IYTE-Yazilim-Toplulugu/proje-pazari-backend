package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.UserRegisteredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegisteredEventHandler Tests")
class UserRegisteredEventHandlerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserRegisteredEventHandler eventHandler;

    @Captor
    private ArgumentCaptor<String> emailCaptor;

    @Captor
    private ArgumentCaptor<String> templateCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> variablesCaptor;

    @BeforeEach
    void setUp() {
        lenient().when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("Should send welcome email when user registered event is received")
    void shouldSendWelcomeEmailOnUserRegistered() {
        // Given
        String userId = "01HQXYZ123";
        String email = "newuser@example.com";
        String firstName = "John";
        String verificationToken = "abc123xyz";
        LocalDateTime occurredOn = LocalDateTime.now();

        UserRegisteredEvent event = new UserRegisteredEvent(
                userId,
                email,
                firstName,
                verificationToken,
                occurredOn
        );

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService, times(1)).sendTemplateEmailAsync(
                emailCaptor.capture(),
                templateCaptor.capture(),
                variablesCaptor.capture()
        );

        assertEquals(email, emailCaptor.getValue());
        assertEquals("welcome.html", templateCaptor.getValue());

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("Welcome to Proje Pazarı!", variables.get("subject"));
        assertEquals(firstName, variables.get("userName"));
        assertTrue(variables.get("verificationLink").toString().contains(verificationToken));
    }

    @Test
    @DisplayName("Should handle email service exception gracefully")
    void shouldHandleEmailServiceExceptionGracefully() {
        // Given
        when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Email service unavailable")));

        UserRegisteredEvent event = new UserRegisteredEvent(
                "user-123",
                "test@example.com",
                "Test",
                "token-123",
                LocalDateTime.now()
        );

        // When & Then
        assertDoesNotThrow(() -> eventHandler.handle(event));
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should include correct verification link format")
    void shouldIncludeCorrectVerificationLinkFormat() {
        // Given
        String verificationToken = "test-token-12345";
        UserRegisteredEvent event = new UserRegisteredEvent(
                "user-123",
                "test@example.com",
                "Test User",
                verificationToken,
                LocalDateTime.now()
        );

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService).sendTemplateEmailAsync(
                any(),
                any(),
                variablesCaptor.capture()
        );

        String verificationLink = (String) variablesCaptor.getValue().get("verificationLink");
        assertTrue(verificationLink.startsWith("http://localhost:3000/verify?token="));
        assertTrue(verificationLink.endsWith(verificationToken));
    }
}
