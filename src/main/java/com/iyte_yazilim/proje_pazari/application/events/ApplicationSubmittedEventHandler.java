package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationSubmittedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handles ApplicationSubmittedEvent by sending notifications.
 *
 * <p>Sends two emails:
 * <ul>
 *   <li>Confirmation email to the applicant</li>
 *   <li>Notification email to the project owner</li>
 * </ul>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-01-16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationSubmittedEventHandler implements IEventHandler<ApplicationSubmittedEvent> {

    private final EmailService emailService;

    @Override
    @EventListener
    public void handle(ApplicationSubmittedEvent event) {
        log.info("Handling ApplicationSubmittedEvent for application: {}", event.applicationId());

        sendApplicantConfirmation(event);
        sendOwnerNotification(event);
    }

    private void sendApplicantConfirmation(ApplicationSubmittedEvent event) {
        Map<String, Object> applicantVariables = Map.of(
                "subject", "Application Received - " + event.projectTitle(),
                "firstName", event.applicantFirstName(),
                "projectTitle", event.projectTitle(),
                "userId", event.applicantId());

        emailService.sendTemplateEmailAsync(
                event.applicantEmail(),
                "application-recived.html",
                applicantVariables);
    }

    private void sendOwnerNotification(ApplicationSubmittedEvent event) {
        Map<String, Object> ownerVariables = Map.of(
                "subject", "New Application Received - " + event.projectTitle(),
                "firstName", event.ownerFirstName(),
                "projectTitle", event.projectTitle(),
                "projectId", event.projectId(),
                "applicantName", event.applicantFirstName());

        emailService.sendTemplateEmailAsync(
                event.ownerEmail(),
                "new-application-notification.html",
                ownerVariables);
    }
}
