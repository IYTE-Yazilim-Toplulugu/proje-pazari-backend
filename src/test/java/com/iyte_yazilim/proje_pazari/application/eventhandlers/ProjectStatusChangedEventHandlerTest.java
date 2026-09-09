package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectStatusChangedEvent;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
@DisplayName("ProjectStatusChangedEventHandler Tests")
class ProjectStatusChangedEventHandlerTest {

    @Mock private EmailService emailService;

    @InjectMocks private ProjectStatusChangedEventHandler eventHandler;

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
    @DisplayName("Should send email to owner when project status changes")
    void shouldSendEmailToOwnerWhenStatusChanges() {
        // Given
        ProjectStatusChangedEvent event =
                new ProjectStatusChangedEvent(
                        "project-123",
                        "AI Research Platform",
                        "owner-123",
                        "owner@example.com",
                        "Jane",
                        ProjectStatus.OPEN,
                        ProjectStatus.IN_PROGRESS,
                        Collections.emptyList(),
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then - should send one email to owner
        verify(emailService, times(1))
                .sendTemplateEmailAsync(
                        emailCaptor.capture(), templateCaptor.capture(), variablesCaptor.capture());

        assertEquals("owner@example.com", emailCaptor.getValue());
        assertEquals("project-status-changed.html", templateCaptor.getValue());
    }

    @Test
    @DisplayName("Should send emails to both owner and team members")
    void shouldSendEmailsToBothOwnerAndTeamMembers() {
        // Given
        List<String> teamMemberEmails = Arrays.asList("member1@example.com", "member2@example.com");

        ProjectStatusChangedEvent event =
                new ProjectStatusChangedEvent(
                        "project-123",
                        "AI Research Platform",
                        "owner-123",
                        "owner@example.com",
                        "Jane",
                        ProjectStatus.IN_PROGRESS,
                        ProjectStatus.COMPLETED,
                        teamMemberEmails,
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then - should send 3 emails total (1 to owner + 2 to team members)
        verify(emailService, times(3)).sendTemplateEmailAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should include correct variables in owner notification")
    void shouldIncludeCorrectVariablesInOwnerNotification() {
        // Given
        ProjectStatusChangedEvent event =
                new ProjectStatusChangedEvent(
                        "project-123",
                        "AI Research Platform",
                        "owner-123",
                        "owner@example.com",
                        "Jane",
                        ProjectStatus.DRAFT,
                        ProjectStatus.OPEN,
                        Collections.emptyList(),
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService)
                .sendTemplateEmailAsync(
                        eq("owner@example.com"),
                        eq("project-status-changed.html"),
                        variablesCaptor.capture());

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("Project Status Updated - AI Research Platform", variables.get("subject"));
        assertEquals("Jane", variables.get("firstName"));
        assertEquals("AI Research Platform", variables.get("projectTitle"));
        assertEquals("OPEN", variables.get("newStatus"));
        assertEquals("project-123", variables.get("projectId"));
        assertEquals("http://localhost:3000", variables.get("baseUrl"));
    }

    @Test
    @DisplayName("Should not send team member emails when list is empty")
    void shouldNotSendTeamMemberEmailsWhenListIsEmpty() {
        // Given
        ProjectStatusChangedEvent event =
                new ProjectStatusChangedEvent(
                        "project-123",
                        "Test Project",
                        "owner-123",
                        "owner@example.com",
                        "Owner",
                        ProjectStatus.OPEN,
                        ProjectStatus.CANCELLED,
                        Collections.emptyList(),
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then - only owner email should be sent
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should not send team member emails when list is null")
    void shouldNotSendTeamMemberEmailsWhenListIsNull() {
        // Given
        ProjectStatusChangedEvent event =
                new ProjectStatusChangedEvent(
                        "project-123",
                        "Test Project",
                        "owner-123",
                        "owner@example.com",
                        "Owner",
                        ProjectStatus.OPEN,
                        ProjectStatus.CANCELLED,
                        null,
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then - only owner email should be sent
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should use async email sending")
    void shouldUseAsyncEmailSending() {
        // Given
        ProjectStatusChangedEvent event =
                new ProjectStatusChangedEvent(
                        "project-123",
                        "Test",
                        "owner-123",
                        "test@example.com",
                        "Test",
                        ProjectStatus.DRAFT,
                        ProjectStatus.OPEN,
                        Collections.emptyList(),
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

        ProjectStatusChangedEvent event =
                new ProjectStatusChangedEvent(
                        "project-123",
                        "Test",
                        "owner-123",
                        "test@example.com",
                        "Test",
                        ProjectStatus.DRAFT,
                        ProjectStatus.OPEN,
                        Collections.emptyList(),
                        LocalDateTime.now());

        // When & Then
        assertDoesNotThrow(() -> eventHandler.handle(event));
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }
}
