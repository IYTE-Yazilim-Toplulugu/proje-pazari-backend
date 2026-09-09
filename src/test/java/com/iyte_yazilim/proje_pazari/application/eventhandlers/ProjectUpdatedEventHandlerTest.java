package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectUpdatedEvent;
import com.iyte_yazilim.proje_pazari.domain.models.TeamMemberInfo;
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
@DisplayName("ProjectUpdatedEventHandler Tests")
class ProjectUpdatedEventHandlerTest {

    @Mock private EmailService emailService;

    @InjectMocks private ProjectUpdatedEventHandler eventHandler;

    @Captor private ArgumentCaptor<String> emailCaptor;

    @Captor private ArgumentCaptor<String> templateCaptor;

    @Captor private ArgumentCaptor<Map<String, Object>> variablesCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventHandler, "baseUrl", "http://localhost:3000");

        lenient()
                .when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("Should send project update email to owner")
    void shouldSendProjectUpdateEmailToOwner() {
        // Given
        ProjectUpdatedEvent event =
                new ProjectUpdatedEvent(
                        "project-123",
                        "AI Research Platform",
                        "owner-123",
                        "owner@example.com",
                        "Jane",
                        Collections.emptyList(),
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService, times(1))
                .sendTemplateEmailAsync(
                        emailCaptor.capture(), templateCaptor.capture(), variablesCaptor.capture());

        assertEquals("owner@example.com", emailCaptor.getValue());
        assertEquals("project-updated.html", templateCaptor.getValue());
    }

    @Test
    @DisplayName("Should include correct variables in owner notification")
    void shouldIncludeCorrectVariablesInOwnerNotification() {
        // Given
        ProjectUpdatedEvent event =
                new ProjectUpdatedEvent(
                        "project-123",
                        "AI Research Platform",
                        "owner-123",
                        "owner@example.com",
                        "Jane",
                        Collections.emptyList(),
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then
        verify(emailService)
                .sendTemplateEmailAsync(
                        eq("owner@example.com"),
                        eq("project-updated.html"),
                        variablesCaptor.capture());

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("Project Updated - AI Research Platform", variables.get("subject"));
        assertEquals("Jane", variables.get("firstName"));
        assertEquals("AI Research Platform", variables.get("projectTitle"));
        assertEquals("project-123", variables.get("projectId"));
        assertEquals("http://localhost:3000", variables.get("baseUrl"));
    }

    @Test
    @DisplayName("Should send emails to both owner and team members")
    void shouldSendEmailsToBothOwnerAndTeamMembers() {
        // Given
        List<TeamMemberInfo> teamMembers =
                Arrays.asList(
                        new TeamMemberInfo("member1@example.com", "Alice"),
                        new TeamMemberInfo("member2@example.com", "Bob"));

        ProjectUpdatedEvent event =
                new ProjectUpdatedEvent(
                        "project-123",
                        "AI Research Platform",
                        "owner-123",
                        "owner@example.com",
                        "Jane",
                        teamMembers,
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then - 1 to owner + 2 to team members
        verify(emailService, times(3)).sendTemplateEmailAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should not send team member emails when list is empty")
    void shouldNotSendTeamMemberEmailsWhenListIsEmpty() {
        // Given
        ProjectUpdatedEvent event =
                new ProjectUpdatedEvent(
                        "project-123",
                        "Test Project",
                        "owner-123",
                        "owner@example.com",
                        "Owner",
                        Collections.emptyList(),
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then - only owner email
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should not send team member emails when list is null")
    void shouldNotSendTeamMemberEmailsWhenListIsNull() {
        // Given
        ProjectUpdatedEvent event =
                new ProjectUpdatedEvent(
                        "project-123",
                        "Test Project",
                        "owner-123",
                        "owner@example.com",
                        "Owner",
                        null,
                        LocalDateTime.now());

        // When
        eventHandler.handle(event);

        // Then - only owner email
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should use async email sending")
    void shouldUseAsyncEmailSending() {
        // Given
        ProjectUpdatedEvent event =
                new ProjectUpdatedEvent(
                        "project-123",
                        "Test",
                        "owner-123",
                        "test@example.com",
                        "Test",
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

        ProjectUpdatedEvent event =
                new ProjectUpdatedEvent(
                        "project-123",
                        "Test",
                        "owner-123",
                        "test@example.com",
                        "Test",
                        Collections.emptyList(),
                        LocalDateTime.now());

        // When & Then
        assertDoesNotThrow(() -> eventHandler.handle(event));
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }
}
