package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectUpdatedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import com.iyte_yazilim.proje_pazari.domain.models.TeamMemberInfo;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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

    @Async
    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProjectUpdatedEvent event) {
        log.info("Handling ProjectUpdatedEvent for project: {}", event.projectId());

        sendOwnerNotification(event);

        if (event.teamMembers() != null && !event.teamMembers().isEmpty()) {
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
        for (TeamMemberInfo member : event.teamMembers()) {
            Map<String, Object> variables =
                    Map.of(
                            "subject",
                            "Project Updated - " + event.projectTitle(),
                            "firstName",
                            member.firstName(),
                            "projectTitle",
                            event.projectTitle(),
                            "projectId",
                            event.projectId(),
                            "baseUrl",
                            baseUrl);

            emailService.sendTemplateEmailAsync(member.email(), "project-updated.html", variables);
        }
    }
}
