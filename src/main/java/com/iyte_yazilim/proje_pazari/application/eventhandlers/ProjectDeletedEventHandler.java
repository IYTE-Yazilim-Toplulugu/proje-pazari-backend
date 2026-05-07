package com.iyte_yazilim.proje_pazari.application.eventhandlers;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectDeletedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IEventHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Handles ProjectDeletedEvent by sending a notification email to the project owner.
 *
 * <p>This handler notifies the project owner about the deletion of their project, including
 * information about any pending applications that were automatically rejected.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-03-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectDeletedEventHandler implements IEventHandler<ProjectDeletedEvent> {

    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String baseUrl;

    @Async
    @Override
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProjectDeletedEvent event) {
        log.info(
                "Handling ProjectDeletedEvent for project: {} (rejected {} pending applications)",
                event.projectId(),
                event.rejectedApplicationCount());

        // --- 1. Notify the project owner ---
        Map<String, Object> ownerVariables =
                Map.of(
                        "subject",
                        "Project Deleted - " + event.projectTitle(),
                        "firstName",
                        event.ownerName(),
                        "projectTitle",
                        event.projectTitle(),
                        "projectId",
                        event.projectId(),
                        "rejectedApplicationCount",
                        event.rejectedApplicationCount(),
                        "baseUrl",
                        baseUrl);

        try {
            emailService.sendTemplateEmailAsync(
                    event.ownerEmail(), "project-deleted.html", ownerVariables);
        } catch (Exception e) {
            log.error(
                    "Failed to send owner notification for ProjectDeletedEvent [projectId={}]: {}",
                    event.projectId(),
                    e.getMessage(),
                    e);
        }

        // --- 2. Notify affected applicants ---
        if (event.applicantEmails() != null && !event.applicantEmails().isEmpty()) {
            for (int i = 0; i < event.applicantEmails().size(); i++) {
                String applicantEmail = event.applicantEmails().get(i);
                String applicantName =
                        (event.applicantNames() != null && i < event.applicantNames().size())
                                ? event.applicantNames().get(i)
                                : "Applicant";

                Map<String, Object> applicantVariables =
                        Map.of(
                                "subject",
                                "Application Update - " + event.projectTitle(),
                                "firstName",
                                applicantName,
                                "projectTitle",
                                event.projectTitle(),
                                "projectId",
                                event.projectId(),
                                "baseUrl",
                                baseUrl);

                try {
                    emailService.sendTemplateEmailAsync(
                            applicantEmail, "application-rejected.html", applicantVariables);
                } catch (Exception e) {
                    log.error(
                            "Failed to notify applicant {} for ProjectDeletedEvent [projectId={}]: {}",
                            applicantEmail,
                            event.projectId(),
                            e.getMessage(),
                            e);
                }
            }
            log.info(
                    "Sent notification emails to {} affected applicants",
                    event.applicantEmails().size());
        }
    }
}
