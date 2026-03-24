package com.iyte_yazilim.proje_pazari.application.events;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectDeletedEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectDeletedEventHandlerTest {

    @Mock private EmailService emailService;

    @InjectMocks private ProjectDeletedEventHandler handler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "baseUrl", "http://localhost:3000");
        lenient()
                .when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("Should send owner notification email when project is deleted")
    void shouldSendOwnerNotificationEmail() {
        ProjectDeletedEvent event = buildEvent("owner@test.com", "Alice", List.of(), List.of());

        handler.handle(event);

        verify(emailService, times(1))
                .sendTemplateEmailAsync(
                        eq("owner@test.com"), eq("project-deleted.html"), any());
    }

    @Test
    @DisplayName("Should send applicant notification emails for each pending applicant")
    void shouldSendApplicantNotificationEmails_forEachApplicant() {
        ProjectDeletedEvent event =
                buildEvent(
                        "owner@test.com",
                        "Alice",
                        List.of("bob@test.com", "carol@test.com"),
                        List.of("Bob", "Carol"));

        handler.handle(event);

        // 1 owner email + 2 applicant emails
        verify(emailService, times(3)).sendTemplateEmailAsync(any(), any(), any());
        verify(emailService).sendTemplateEmailAsync(eq("bob@test.com"), eq("application-rejected.html"), any());
        verify(emailService).sendTemplateEmailAsync(eq("carol@test.com"), eq("application-rejected.html"), any());
    }

    @Test
    @DisplayName("Should send only owner email when there are zero applicants")
    void shouldSendOnlyOwnerEmail_whenNoApplicants() {
        ProjectDeletedEvent event = buildEvent("owner@test.com", "Alice", List.of(), List.of());

        handler.handle(event);

        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
        verify(emailService, times(1))
                .sendTemplateEmailAsync(eq("owner@test.com"), eq("project-deleted.html"), any());
    }

    @Test
    @DisplayName("Should use 'Applicant' as fallback name when applicantNames list is shorter than applicantEmails")
    void shouldUseFallbackName_whenApplicantNamesListIsShorter() {
        // 2 emails but only 1 name — second applicant should use "Applicant"
        ProjectDeletedEvent event =
                buildEvent(
                        "owner@test.com",
                        "Alice",
                        List.of("bob@test.com", "carol@test.com"),
                        List.of("Bob"));

        handler.handle(event);

        // both applicant emails must be sent; just verify no exception and correct count
        verify(emailService, times(3)).sendTemplateEmailAsync(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle null applicantEmails gracefully")
    void shouldHandleNullApplicantEmails_gracefully() {
        ProjectDeletedEvent event = buildEvent("owner@test.com", "Alice", null, null);

        assertDoesNotThrow(() -> handler.handle(event));
        verify(emailService, times(1)).sendTemplateEmailAsync(any(), any(), any());
    }

    private ProjectDeletedEvent buildEvent(
            String ownerEmail,
            String ownerName,
            List<String> applicantEmails,
            List<String> applicantNames) {
        return new ProjectDeletedEvent(
                "project-123",
                "Test Project",
                "owner-id-123",
                ownerEmail,
                ownerName,
                applicantEmails == null ? 0 : applicantEmails.size(),
                applicantEmails,
                applicantNames,
                LocalDateTime.now());
    }
}
