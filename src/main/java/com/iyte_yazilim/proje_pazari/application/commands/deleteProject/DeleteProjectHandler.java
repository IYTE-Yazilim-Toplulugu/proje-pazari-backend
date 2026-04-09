package com.iyte_yazilim.proje_pazari.application.commands.deleteProject;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectDeletedEvent;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles project deletion with proper cleanup of related entities.
 *
 * <p>When a project is deleted:
 *
 * <ul>
 *   <li>Pending application count and applicant contact info are collected for notifications
 *   <li>The project is hard-deleted from the database (cascade removes applications)
 *   <li>A {@link ProjectDeletedEvent} is published for notifications to owner and applicants
 * </ul>
 *
 * <p>Projects in {@link ProjectStatus#IN_PROGRESS} cannot be deleted; they must be cancelled first.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.2
 * @since 2026-03-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteProjectHandler
        implements IRequestHandler<DeleteProjectCommand, ApiResponse<Void>> {

    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<Void> handle(DeleteProjectCommand command) {

        // --- 1. Verify Project Exists ---
        ProjectEntity projectEntity = projectRepository.findById(command.projectId()).orElse(null);
        if (projectEntity == null) {
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "project.not.found", new Object[] {command.projectId()}));
        }

        // --- 2. Verify Ownership ---
        if (!projectEntity.getOwner().getId().equals(command.ownerId())) {
            return ApiResponse.forbidden(messageService.getMessage("project.owner.mismatch"));
        }

        // --- 3. Verify Project Status Allows Deletion ---
        if (projectEntity.getStatus() == ProjectStatus.IN_PROGRESS) {
            return ApiResponse.forbidden(
                    messageService.getMessage("project.delete.has.active.work"));
        }

        // --- 4. Collect Pending Application Info (for notifications) ---
        List<ProjectApplicationEntity> pendingApplications =
                applicationRepository.findByProjectId(command.projectId()).stream()
                        .filter(app -> app.getStatus() == ApplicationStatus.PENDING)
                        .toList();

        int rejectedCount = pendingApplications.size();
        List<String> applicantEmails =
                pendingApplications.stream().map(app -> app.getUser().getEmail()).toList();
        List<String> applicantNames =
                pendingApplications.stream().map(app -> app.getUser().getFirstName()).toList();

        if (rejectedCount > 0) {
            log.info(
                    "Project {} deletion will cascade-remove {} pending applications",
                    command.projectId(),
                    rejectedCount);
        }

        // --- 5. Capture owner info before deletion ---
        String ownerId = projectEntity.getOwner().getId();
        String ownerEmail = projectEntity.getOwner().getEmail();
        String ownerName = projectEntity.getOwner().getFirstName();
        String projectTitle = projectEntity.getTitle();

        // --- 6. Delete ---
        projectRepository.delete(projectEntity);

        // --- 7. Publish Event ---
        applicationEventPublisher.publishEvent(
                new ProjectDeletedEvent(
                        command.projectId(),
                        projectTitle,
                        ownerId,
                        ownerEmail,
                        ownerName,
                        rejectedCount,
                        applicantEmails,
                        applicantNames,
                        LocalDateTime.now()));

        // --- 8. Response ---
        return ApiResponse.success(null, messageService.getMessage("project.deleted.success"));
    }
}
