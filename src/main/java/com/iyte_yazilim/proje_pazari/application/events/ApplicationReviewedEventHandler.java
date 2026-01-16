package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationReviewedEventHandler implements IEventHandler<ApplicationReviewedEvent> {

    private final EmailService emailService;

    @Override
    @EventListener
    public void handle(ApplicationReviewedEvent event) {
        log.info(
                "Handling ApplicationReviewedEvent for application: {} with status: {}",
                event.applicationId(),
                event.status());

        String template =
                event.status() == ApplicationStatus.APPROVED
                        ? "application-approved.html"
                        : "application-rejected.html";

        Map<String, Object> variables =
                Map.of(
                        "subject", "Application " + event.status(),
                        "firstName", event.applicantFirstName(),
                        "projectTitle", event.projectTitle(),
                        "reviewMessage", event.reviewMessage());

        emailService.sendTemplateEmail(event.applicantEmail(), template, variables);
    }
}
