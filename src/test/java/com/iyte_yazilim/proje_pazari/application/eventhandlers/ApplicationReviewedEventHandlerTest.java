package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationReviewedEventHandler Tests")
class ApplicationReviewedEventHandlerTest {

    @Mock private EmailService emailService;

    @InjectMocks private ApplicationReviewedEventHandler eventHandler;

    @Captor private ArgumentCaptor<String> emailCaptor;

    @Captor private ArgumentCaptor<String> templateCaptor;

    @Captor private ArgumentCaptor<Map<String, Object>> variablesCaptor;

    @BeforeEach
    void setUp() {
        // Set baseUrl using reflection for testing
        ReflectionTestUtils.setField(eventHandler, "baseUrl", "http://localhost:3000");

        lenient()
                .when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("Should send approval email with correct template and variables")
    void shouldSendApprovalEmailWithCorrectData() {
        // Given
        ApplicationReviewedEvent event =
                new ApplicationReviewedEvent(
                        "app-123",
                        "project-123",
                        "applicant@example.com",
                        "AI Research Platform",
                        "John",
                        "Jane",
                        "owner@example.com",
                        ApplicationStatus.APPROVED,
                        "Great experience and skills!");

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService, times(1))
                .sendTemplateEmailAsync(
                        emailCaptor.capture(), templateCaptor.capture(), variablesCaptor.capture());

        assertEquals("applicant@example.com", emailCaptor.getValue());
        assertEquals("application-approved.html", templateCaptor.getValue());

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("Application Approved - Congratulations!", variables.get("subject"));
        assertEquals("John", variables.get("firstName"));
        assertEquals("AI Research Platform", variables.get("projectTitle"));
        assertEquals("project-123", variables.get("projectId"));
        assertEquals("http://localhost:3000", variables.get("baseUrl"));
    }

    @Test
    @DisplayName("Should send rejection email with correct template and variables")
    void shouldSendRejectionEmailWithCorrectData() {
        // Given
        ApplicationReviewedEvent event =
                new ApplicationReviewedEvent(
                        "app-123",
                        "project-123",
                        "applicant@example.com",
                        "AI Research Platform",
                        "John",
                        "Jane",
                        "owner@example.com",
                        ApplicationStatus.REJECTED,
                        "Thank you for applying");

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService, times(1))
                .sendTemplateEmailAsync(
                        emailCaptor.capture(), templateCaptor.capture(), variablesCaptor.capture());

        assertEquals("applicant@example.com", emailCaptor.getValue());
        assertEquals("application-rejected.html", templateCaptor.getValue());

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("Application Status Update", variables.get("subject"));
        assertEquals("John", variables.get("firstName"));
        assertEquals("AI Research Platform", variables.get("projectTitle"));
        assertEquals("http://localhost:3000", variables.get("baseUrl"));
    }

    @Test
    @DisplayName("Should use async email sending")
    void shouldUseAsyncEmailSending() {
        // Given
        ApplicationReviewedEvent event =
                new ApplicationReviewedEvent(
                        "app-123",
                        "project-123",
                        "test@example.com",
                        "Test Project",
                        "Test",
                        "Owner",
                        "owner@example.com",
                        ApplicationStatus.APPROVED,
                        "");

        // When
        eventHandler.handle(event);

        // Then - verify sendTemplateEmailAsync is called, not sendTemplateEmail
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
        verify(emailService, never()).sendTemplateEmail(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle email service exception gracefully")
    void shouldHandleEmailServiceExceptionGracefully() {
        // Given
        when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(
                        CompletableFuture.failedFuture(
                                new RuntimeException("Email service unavailable")));

        ApplicationReviewedEvent event =
                new ApplicationReviewedEvent(
                        "app-123",
                        "project-123",
                        "test@example.com",
                        "Test",
                        "Test",
                        "Owner",
                        "owner@example.com",
                        ApplicationStatus.APPROVED,
                        "");

        // When & Then
        assertDoesNotThrow(() -> eventHandler.handle(event));
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }
}
