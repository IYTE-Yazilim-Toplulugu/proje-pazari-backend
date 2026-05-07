package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectCreatedEvent;
import java.time.LocalDateTime;
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
@DisplayName("ProjectCreatedEventHandler Tests")
class ProjectCreatedEventHandlerTest {

    @Mock private EmailService emailService;

    @InjectMocks private ProjectCreatedEventHandler eventHandler;

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
    @DisplayName("Should send project creation email to owner")
    void shouldSendProjectCreationEmailToOwner() {
        // Given
        ProjectCreatedEvent event =
                new ProjectCreatedEvent(
                        "project-123",
                        "AI Research Platform",
                        "owner-123",
                        "owner@example.com",
                        "Jane",
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService, times(1))
                .sendTemplateEmailAsync(
                        emailCaptor.capture(), templateCaptor.capture(), variablesCaptor.capture());

        assertEquals("owner@example.com", emailCaptor.getValue());
        assertEquals("project-created.html", templateCaptor.getValue());
    }

    @Test
    @DisplayName("Should include correct variables in project creation email")
    void shouldIncludeCorrectVariablesInEmail() {
        // Given
        ProjectCreatedEvent event =
                new ProjectCreatedEvent(
                        "project-123",
                        "AI Research Platform",
                        "owner-123",
                        "owner@example.com",
                        "Jane",
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService).sendTemplateEmailAsync(any(), any(), variablesCaptor.capture());

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals(
                "Project Created Successfully - AI Research Platform", variables.get("subject"));
        assertEquals("Jane", variables.get("firstName"));
        assertEquals("AI Research Platform", variables.get("projectTitle"));
        assertEquals("project-123", variables.get("projectId"));
        assertEquals("http://localhost:3000", variables.get("baseUrl"));
    }

    @Test
    @DisplayName("Should use async email sending")
    void shouldUseAsyncEmailSending() {
        // Given
        ProjectCreatedEvent event =
                new ProjectCreatedEvent(
                        "project-123",
                        "Test Project",
                        "owner-123",
                        "test@example.com",
                        "Test",
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then
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

        ProjectCreatedEvent event =
                new ProjectCreatedEvent(
                        "project-123",
                        "Test",
                        "owner-123",
                        "test@example.com",
                        "Test",
                        LocalDateTime.now());

        // When & Then
        assertDoesNotThrow(() -> eventHandler.handle(event));
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }
}
