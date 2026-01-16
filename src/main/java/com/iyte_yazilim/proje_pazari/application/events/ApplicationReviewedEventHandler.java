package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handles ApplicationReviewedEvent by sending notification emails.
 *
 * <p>Selects the appropriate template based on application status
 * (approved or rejected) and sends email to the applicant.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-01-16
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationReviewedEventHandler implements IEventHandler<ApplicationReviewedEvent> {

    private final EmailService emailService;

    @Override
    @EventListener
    public void handle(ApplicationReviewedEvent event) {
        log.info("Handling ApplicationReviewedEvent for application: {} with status: {}",
                event.applicationId(), event.status());

        String template = event.status() == ApplicationStatus.APPROVED
                ? "application-approved.html"
                : "application-rejected.html";

        Map<String, Object> variables = Map.of(
                "subject", "Application " + event.status(),
                "firstName", event.applicantFirstName(),
                "projectTitle", event.projectTitle(),
                "reviewMessage", event.reviewMessage());

        emailService.sendTemplateEmail(
                event.applicantEmail(),
                template,
                variables);
    }
}
