package com.iyte_yazilim.proje_pazari.infrastructure.persistence.email;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationSubmittedEvent;
import com.iyte_yazilim.proje_pazari.domain.events.UserRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class EmailEventListener {

    private final EmailService emailService;

    public EmailEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        Map<String, Object> variables = Map.of(
                "subject", "Welcome to Proje Pazarı!",
                "userName", event.firstName(),
                "verificationLink", "http://localhost:3000/verify?token=" + event.verificationToken()
        );

        emailService.sendTemplateEmailAsync(
                event.email(),
                "welcome.html",
                variables
        );
    }

    @EventListener
    public void handleApplicationSubmitted(ApplicationSubmittedEvent event) {
        // Send confirmation to applicant
        Map<String, Object> applicantVariables = Map.of(
                "subject", "Application Received - " + event.projectTitle(),
                "firstName", event.applicantFirstName(),
                "projectTitle", event.projectTitle(),
                "userId", event.applicantId()
        );

        emailService.sendTemplateEmailAsync(
                event.applicantEmail(),
                "application-recived.html",
                applicantVariables
        );

        // Send notification to project owner
        Map<String, Object> ownerVariables = Map.of(
                "subject", "New Application Received - " + event.projectTitle(),
                "firstName", event.ownerFirstName(),
                "projectTitle", event.projectTitle(),
                "applicantName", event.applicantFirstName()
        );

        emailService.sendTemplateEmailAsync(
                event.ownerEmail(),
                "application-recived.html",
                ownerVariables
        );
    }

    @EventListener
    public void handleApplicationReviewed(ApplicationReviewedEvent event) {
        String template = event.status() == ApplicationStatus.APPROVED
                ? "application-approved.html"
                : "application-rejected.html";
        // Send email
    }
}
