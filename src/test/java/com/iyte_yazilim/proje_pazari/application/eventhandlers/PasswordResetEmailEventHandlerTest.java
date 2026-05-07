package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.PasswordResetEmailRequestedEvent;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordResetEmailEventHandlerTest {

    @Mock private EmailService emailService;

    @InjectMocks private PasswordResetEmailEventHandler handler;

    @BeforeEach
    void setUp() {
        lenient()
                .when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("Should send password reset email with correct template and recipient")
    void shouldSendPasswordResetEmail() {
        var event =
                new PasswordResetEmailRequestedEvent(
                        "user-123",
                        "user@std.iyte.edu.tr",
                        "John",
                        "https://example.com/reset?t=abc");

        handler.handle(event);

        verify(emailService, times(1))
                .sendTemplateEmailAsync(
                        eq("user@std.iyte.edu.tr"), eq("password-reset.html"), any());
    }

    @Test
    @DisplayName("Should not throw when email service fails")
    void shouldNotThrowWhenEmailServiceFails() {
        when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenThrow(new RuntimeException("SMTP error"));

        var event =
                new PasswordResetEmailRequestedEvent(
                        "user-123",
                        "user@std.iyte.edu.tr",
                        "John",
                        "https://example.com/reset?t=abc");

        assertDoesNotThrow(() -> handler.handle(event));
    }
}
