package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationReviewedEventHandler implements IEventHandler<ApplicationReviewedEvent> {

    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String baseUrl;

    @Override
    @EventListener
    public void handle(ApplicationReviewedEvent event) {
        log.info(
                "Handling ApplicationReviewedEvent for application: {} with status: {}",
                event.applicationId(),
                event.status());

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
    }
}
