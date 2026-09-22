package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationReviewedEventHandler implements IEventHandler<ApplicationReviewedEvent> {

    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String baseUrl;

    @Async
    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ApplicationReviewedEvent event) {
        log.info(
                "Handling ApplicationReviewedEvent for application: {} with status: {}",
                event.applicationId(),
                event.status());

        try {
            if (event.status() == ApplicationStatus.APPROVED) {
                Map<String, Object> variables =
                        Map.of(
                                "subject", "Application Approved - Congratulations!",
                                "firstName", event.applicantFirstName(),
                                "projectTitle", event.projectTitle(),
                                "projectId", event.projectId(),
                                "baseUrl", baseUrl);

                emailService.sendTemplateEmailAsync(
                        event.applicantEmail(), "application-approved.html", variables);
            } else {
                Map<String, Object> variables =
                        Map.of(
                                "subject",
                                "Application Status Update",
                                "firstName",
                                event.applicantFirstName(),
                                "projectTitle",
                                event.projectTitle(),
                                "baseUrl",
                                baseUrl);

                emailService.sendTemplateEmailAsync(
                        event.applicantEmail(), "application-rejected.html", variables);
            }
        } catch (Exception e) {
            log.error(
                    "Failed to send notification for ApplicationReviewedEvent [applicationId={}]: {}",
                    event.applicationId(),
                    e.getMessage(),
                    e);
        }
    }
}
