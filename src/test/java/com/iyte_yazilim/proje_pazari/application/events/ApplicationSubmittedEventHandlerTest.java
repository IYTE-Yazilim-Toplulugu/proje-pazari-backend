package com.iyte_yazilim.proje_pazari.application.events;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationSubmittedEvent;
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
@DisplayName("ApplicationSubmittedEventHandler Tests")
class ApplicationSubmittedEventHandlerTest {

    @Mock private EmailService emailService;

    @InjectMocks private ApplicationSubmittedEventHandler eventHandler;

    @Captor private ArgumentCaptor<String> emailCaptor;

    @Captor private ArgumentCaptor<String> templateCaptor;

    @Captor private ArgumentCaptor<Map<String, Object>> variablesCaptor;

    @BeforeEach
    void setUp() {
        lenient()
                .when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("Should send emails to both applicant and owner when application is submitted")
    void shouldSendEmailsToBothApplicantAndOwner() {
        // Given
        ApplicationSubmittedEvent event =
                new ApplicationSubmittedEvent(
                        "app-123",
                        "project-123",
                        "AI Research Platform",
                        "user-123",
                        "applicant@example.com",
                        "John",
                        "owner@example.com",
                        "Jane");

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService, times(2))
                .sendTemplateEmailAsync(
                        emailCaptor.capture(), templateCaptor.capture(), variablesCaptor.capture());

        var capturedEmails = emailCaptor.getAllValues();
        assertTrue(capturedEmails.contains("applicant@example.com"));
        assertTrue(capturedEmails.contains("owner@example.com"));
    }

    @Test
    @DisplayName("Should send applicant confirmation with correct template and variables")
    void shouldSendApplicantConfirmationWithCorrectData() {
        // Given
        ApplicationSubmittedEvent event =
                new ApplicationSubmittedEvent(
                        "app-123",
                        "project-123",
                        "AI Research Platform",
                        "user-123",
                        "applicant@example.com",
                        "John",
                        "owner@example.com",
                        "Jane");

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService, atLeastOnce())
                .sendTemplateEmailAsync(
                        eq("applicant@example.com"),
                        eq("application-recived.html"),
                        variablesCaptor.capture());

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("Application Received - AI Research Platform", variables.get("subject"));
        assertEquals("John", variables.get("firstName"));
        assertEquals("AI Research Platform", variables.get("projectTitle"));
        assertEquals("user-123", variables.get("userId"));
    }

    @Test
    @DisplayName("Should send owner notification with correct template and variables")
    void shouldSendOwnerNotificationWithCorrectData() {
        // Given
        ApplicationSubmittedEvent event =
                new ApplicationSubmittedEvent(
                        "app-123",
                        "project-123",
                        "AI Research Platform",
                        "user-123",
                        "applicant@example.com",
                        "John",
                        "owner@example.com",
                        "Jane");

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService, atLeastOnce())
                .sendTemplateEmailAsync(
                        eq("owner@example.com"),
                        eq("new-application-notification.html"),
                        variablesCaptor.capture());

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("New Application Received - AI Research Platform", variables.get("subject"));
        assertEquals("Jane", variables.get("firstName"));
        assertEquals("AI Research Platform", variables.get("projectTitle"));
        assertEquals("project-123", variables.get("projectId"));
        assertEquals("John", variables.get("applicantName"));
    }

    @Test
    @DisplayName("Should handle email service exception gracefully")
    void shouldHandleEmailServiceExceptionGracefully() {
        // Given
        when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(
                        CompletableFuture.failedFuture(
                                new RuntimeException("Email service unavailable")));

        ApplicationSubmittedEvent event =
                new ApplicationSubmittedEvent(
                        "app-123",
                        "project-123",
                        "Test Project",
                        "user-123",
                        "test@example.com",
                        "Test",
                        "owner@example.com",
                        "Owner");

        // When & Then
        assertDoesNotThrow(() -> eventHandler.handle(event));
        verify(emailService, times(2)).sendTemplateEmailAsync(any(), any(), any());
    }
}
