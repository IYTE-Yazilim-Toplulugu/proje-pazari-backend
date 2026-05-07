package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationSubmittedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationSubmittedEventHandler implements IEventHandler<ApplicationSubmittedEvent> {

    private final EmailService emailService;

    @Async
    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ApplicationSubmittedEvent event) {
        log.info("Handling ApplicationSubmittedEvent for application: {}", event.applicationId());

        try {
            sendApplicantConfirmation(event);
            sendOwnerNotification(event);
        } catch (Exception e) {
            log.error(
                    "Failed to send notification for ApplicationSubmittedEvent [applicationId={}]: {}",
                    event.applicationId(),
                    e.getMessage(),
                    e);
        }
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
