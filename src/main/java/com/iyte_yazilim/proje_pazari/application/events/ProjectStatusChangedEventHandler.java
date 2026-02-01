package com.iyte_yazilim.proje_pazari.application.events;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectStatusChangedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Handles ProjectStatusChangedEvent by sending notification emails to project owner and team
 * members.
 *
 * <p>This handler is part of the application layer and coordinates the email sending process when a
 * project's status changes.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectStatusChangedEventHandler implements IEventHandler<ProjectStatusChangedEvent> {

    private final EmailService emailService;

    @Value("${app.mail.base-url:http://localhost:3000}")
    private String baseUrl;

    @Override
    @EventListener
    public void handle(ProjectStatusChangedEvent event) {
        log.info(
                "Handling ProjectStatusChangedEvent for project: {} - status changed from {} to {}",
                event.projectId(),
                event.oldStatus(),
                event.newStatus());

        // Send email to project owner
        sendOwnerNotification(event);

        // Send emails to team members if any
        if (event.teamMemberEmails() != null && !event.teamMemberEmails().isEmpty()) {
            sendTeamMemberNotifications(event);
        }
    }

    private void sendOwnerNotification(ProjectStatusChangedEvent event) {
        Map<String, Object> variables =
                Map.of(
                        "subject",
                        "Project Status Updated - " + event.projectTitle(),
                        "firstName",
                        event.ownerName(),
                        "projectTitle",
                        event.projectTitle(),
                        "newStatus",
                        event.newStatus().toString(),
                        "projectId",
                        event.projectId(),
                        "baseUrl",
                        baseUrl);

        emailService.sendTemplateEmailAsync(
                event.ownerEmail(), "project-status-changed.html", variables);
    }

    private void sendTeamMemberNotifications(ProjectStatusChangedEvent event) {
        Map<String, Object> variables =
                Map.of(
                        "subject",
                        "Project Status Updated - " + event.projectTitle(),
                        "firstName",
                        "Team Member",
                        "projectTitle",
                        event.projectTitle(),
                        "newStatus",
                        event.newStatus().toString(),
                        "projectId",
                        event.projectId(),
                        "baseUrl",
                        baseUrl);

        for (String memberEmail : event.teamMemberEmails()) {
            emailService.sendTemplateEmailAsync(
                    memberEmail, "project-status-changed.html", variables);
        }
    }
}
