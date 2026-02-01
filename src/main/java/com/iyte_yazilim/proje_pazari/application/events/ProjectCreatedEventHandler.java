package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectCreatedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handles ProjectCreatedEvent by sending a confirmation email to the project owner.
 *
 * <p>This handler is part of the application layer and coordinates the email sending process when a
 * new project is created.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectCreatedEventHandler implements IEventHandler<ProjectCreatedEvent> {

    private final EmailService emailService;

    @Value("${app.mail.base-url}")
    private String baseUrl;

    @Override
    @EventListener
    public void handle(ProjectCreatedEvent event) {
        log.info("Handling ProjectCreatedEvent for project: {}", event.projectId());

        Map<String, Object> variables =
                Map.of(
                        "subject",
                        "Project Created Successfully - " + event.projectTitle(),
                        "firstName",
                        event.ownerName(),
                        "projectTitle",
                        event.projectTitle(),
                        "projectId",
                        event.projectId(),
                        "baseUrl",
                        baseUrl);

        emailService.sendTemplateEmailAsync(event.ownerEmail(), "project-created.html", variables);
    }
}
