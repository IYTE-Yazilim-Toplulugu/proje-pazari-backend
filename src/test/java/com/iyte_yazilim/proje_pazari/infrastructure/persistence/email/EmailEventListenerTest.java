package com.iyte_yazilim.proje_pazari.infrastructure.persistence.email;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationSubmittedEvent;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailEventListener Tests")
class EmailEventListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailEventListener emailEventListener;

    @Captor
    private ArgumentCaptor<String> emailCaptor;

    @Captor
    private ArgumentCaptor<String> templateCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> variablesCaptor;

    @BeforeEach
    void setUp() {
        // Mock async method to return completed future (lenient for tests that don't use it)
        lenient().when(emailService.sendTemplateEmailAsync(any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        // Mock synchronous method (lenient for tests that don't use it)
        lenient().doNothing().when(emailService).sendTemplateEmail(any(), any(), any());
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
        emailEventListener.handleUserRegistered(event);

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
    @DisplayName("Should send two emails when application is submitted")
    void shouldSendTwoEmailsOnApplicationSubmitted() {
        // Given
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(
                "app-123",
                "project-456",
                "Machine Learning Project",
                "applicant-789",
                "applicant@example.com",
                "Alice",
                "owner@example.com",
                "Bob"
        );

        // When
        emailEventListener.handleApplicationSubmitted(event);

        // Then
        verify(emailService, times(2)).sendTemplateEmailAsync(
                emailCaptor.capture(),
                templateCaptor.capture(),
                variablesCaptor.capture()
        );

        // Verify applicant email
        assertEquals("applicant@example.com", emailCaptor.getAllValues().get(0));
        assertEquals("application-recived.html", templateCaptor.getAllValues().get(0));
        Map<String, Object> applicantVars = variablesCaptor.getAllValues().get(0);
        assertEquals("Alice", applicantVars.get("firstName"));
        assertEquals("Machine Learning Project", applicantVars.get("projectTitle"));

        // Verify owner email
        assertEquals("owner@example.com", emailCaptor.getAllValues().get(1));
        assertEquals("new-application-notification.html", templateCaptor.getAllValues().get(1));
        Map<String, Object> ownerVars = variablesCaptor.getAllValues().get(1);
        assertEquals("Bob", ownerVars.get("firstName"));
        assertEquals("Alice", ownerVars.get("applicantName"));
    }

    @Test
    @DisplayName("Should send approved template when application is approved")
    void shouldSendApprovedEmailWhenApplicationApproved() {
        // Given
        ApplicationReviewedEvent event = new ApplicationReviewedEvent(
                "app-123",
                "applicant@example.com",
                "AI Research Project",
                "Alice",
                "Bob",
                "owner@example.com",
                ApplicationStatus.APPROVED,
                "Great application! Welcome to the team."
        );

        // When
        emailEventListener.handleApplicationReviewed(event);

        // Then
        verify(emailService, times(1)).sendTemplateEmail(
                eq("owner@example.com"),
                eq("application-reviewed"),
                variablesCaptor.capture()
        );

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("Alice", variables.get("firstName"));
        assertEquals("AI Research Project", variables.get("projectTitle"));
        assertEquals("Great application! Welcome to the team.", variables.get("reviewMessage"));
    }

    @Test
    @DisplayName("Should send rejected template when application is rejected")
    void shouldSendRejectedEmailWhenApplicationRejected() {
        // Given
        ApplicationReviewedEvent event = new ApplicationReviewedEvent(
                "app-123",
                "applicant@example.com",
                "Web Development Project",
                "Charlie",
                "Diana",
                "owner@example.com",
                ApplicationStatus.REJECTED,
                "Thank you for your interest, but we've decided to move forward with other candidates."
        );

        // When
        emailEventListener.handleApplicationReviewed(event);

        // Then
        verify(emailService, times(1)).sendTemplateEmail(
                eq("owner@example.com"),
                eq("application-reviewed"),
                variablesCaptor.capture()
        );

        Map<String, Object> variables = variablesCaptor.getValue();
        assertEquals("Charlie", variables.get("firstName"));
        assertEquals("Web Development Project", variables.get("projectTitle"));
        assertTrue(variables.get("reviewMessage").toString().contains("other candidates"));
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
        assertDoesNotThrow(() -> emailEventListener.handleUserRegistered(event));
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
        emailEventListener.handleUserRegistered(event);

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

    @Test
    @DisplayName("Should include all required variables for application submission")
    void shouldIncludeAllRequiredVariablesForApplicationSubmission() {
        // Given
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(
                "app-001",
                "project-002",
                "Blockchain Platform",
                "user-003",
                "developer@example.com",
                "Eve",
                "founder@example.com",
                "Frank"
        );

        // When
        emailEventListener.handleApplicationSubmitted(event);

        // Then
        verify(emailService, times(2)).sendTemplateEmailAsync(
                any(),
                any(),
                variablesCaptor.capture()
        );

        // Check applicant email variables
        Map<String, Object> applicantVars = variablesCaptor.getAllValues().get(0);
        assertAll("Applicant email variables",
                () -> assertTrue(applicantVars.containsKey("subject")),
                () -> assertTrue(applicantVars.containsKey("firstName")),
                () -> assertTrue(applicantVars.containsKey("projectTitle")),
                () -> assertTrue(applicantVars.containsKey("userId"))
        );

        // Check owner email variables
        Map<String, Object> ownerVars = variablesCaptor.getAllValues().get(1);
        assertAll("Owner email variables",
                () -> assertTrue(ownerVars.containsKey("subject")),
                () -> assertTrue(ownerVars.containsKey("firstName")),
                () -> assertTrue(ownerVars.containsKey("projectTitle")),
                () -> assertTrue(ownerVars.containsKey("projectId")),
                () -> assertTrue(ownerVars.containsKey("applicantName"))
        );
    }
}
