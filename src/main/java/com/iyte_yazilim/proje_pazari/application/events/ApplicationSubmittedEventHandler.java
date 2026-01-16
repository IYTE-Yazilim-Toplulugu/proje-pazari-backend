package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationSubmittedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

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
        Map<String, Object> applicantVariables =
                Map.of(
                        "subject", "Application Received - " + event.projectTitle(),
                        "firstName", event.applicantFirstName(),
                        "projectTitle", event.projectTitle(),
                        "userId", event.applicantId());

        emailService.sendTemplateEmailAsync(
                event.applicantEmail(), "application-recived.html", applicantVariables);
    }

    private void sendOwnerNotification(ApplicationSubmittedEvent event) {
        Map<String, Object> ownerVariables =
                Map.of(
                        "subject", "New Application Received - " + event.projectTitle(),
                        "firstName", event.ownerFirstName(),
                        "projectTitle", event.projectTitle(),
                        "projectId", event.projectId(),
                        "applicantName", event.applicantFirstName());

        emailService.sendTemplateEmailAsync(
                event.ownerEmail(), "new-application-notification.html", ownerVariables);
    }
}
