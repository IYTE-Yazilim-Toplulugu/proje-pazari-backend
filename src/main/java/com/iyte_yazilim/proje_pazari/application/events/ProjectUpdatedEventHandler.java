package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectUpdatedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handles ProjectUpdatedEvent by sending notification emails to the project owner and approved team
 * members.
 *
 * <p>This handler is part of the application layer and coordinates the email sending process when a
 * project's details are updated.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-04-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectUpdatedEventHandler implements IEventHandler<ProjectUpdatedEvent> {

    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String baseUrl;

    @Override
    @EventListener
    public void handle(ProjectUpdatedEvent event) {
        log.info("Handling ProjectUpdatedEvent for project: {}", event.projectId());

        sendOwnerNotification(event);

        if (event.teamMemberEmails() != null && !event.teamMemberEmails().isEmpty()) {
            sendTeamMemberNotifications(event);
        }
    }

    private void sendOwnerNotification(ProjectUpdatedEvent event) {
        Map<String, Object> variables =
                Map.of(
                        "subject",
                        "Project Updated - " + event.projectTitle(),
                        "firstName",
                        event.ownerName(),
                        "projectTitle",
                        event.projectTitle(),
                        "projectId",
                        event.projectId(),
                        "baseUrl",
                        baseUrl);

        emailService.sendTemplateEmailAsync(event.ownerEmail(), "project-updated.html", variables);
    }

    private void sendTeamMemberNotifications(ProjectUpdatedEvent event) {
        Map<String, Object> variables =
                Map.of(
                        "subject",
                        "Project Updated - " + event.projectTitle(),
                        "firstName",
                        "Team Member",
                        "projectTitle",
                        event.projectTitle(),
                        "projectId",
                        event.projectId(),
                        "baseUrl",
                        baseUrl);

        for (String memberEmail : event.teamMemberEmails()) {
            emailService.sendTemplateEmailAsync(memberEmail, "project-updated.html", variables);
        }
    }
}
