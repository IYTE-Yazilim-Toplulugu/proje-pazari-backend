package com.iyte_yazilim.proje_pazari.application.commands.updateProjectStatus;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectStatusChangedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.UpdateProjectStatusCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles project status updates.
 *
 * <p>Validates project existence, updates status, and publishes ProjectStatusChangedEvent for email
 * notifications to owner and team members.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 */
@Component
@RequiredArgsConstructor
public class UpdateProjectStatusHandler
        implements IRequestHandler<
                UpdateProjectStatusCommand, ApiResponse<UpdateProjectStatusCommandResult>> {

    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<UpdateProjectStatusCommandResult> handle(
            UpdateProjectStatusCommand command) {

        // --- 1. Verify Project Exists ---
        ProjectEntity projectEntity = projectRepository.findById(command.projectId()).orElse(null);
        if (projectEntity == null) {
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "project.not.found", new Object[] {command.projectId()}));
        }

        // --- 2. Store Old Status ---
        ProjectStatus oldStatus = projectEntity.getStatus();

        // --- 3. Check if status is actually changing ---
        if (oldStatus == command.newStatus()) {
            return ApiResponse.badRequest(messageService.getMessage("project.status.unchanged"));
        }

        // --- 4. Validate and Apply Transition via Domain Model ---
        Project projectDomain = projectMapper.entityToDomain(projectEntity);
        try {
            // The domain dictates if this is legal!
            projectDomain.transitionTo(command.newStatus());
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Catch the domain exception and return it as a clean API response
            return ApiResponse.badRequest(e.getMessage());
        }

        // --- 5. Update Entity Status ---
        // We sync the DB entity with the newly validated domain state
        projectEntity.setStatus(projectDomain.getStatus());

        // --- 6. Persistence ---
        ProjectEntity savedProject = projectRepository.save(projectEntity);

        // --- 7. Get Team Member Emails (approved applications) ---
        List<String> teamMemberEmails =
                applicationRepository.findByProjectId(command.projectId()).stream()
                        .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                        .map(app -> app.getUser().getEmail())
                        .toList();

        // --- 8. Publish Event for Email Notifications ---
        applicationEventPublisher.publishEvent(
                new ProjectStatusChangedEvent(
                        savedProject.getId(),
                        savedProject.getTitle(),
                        savedProject.getOwner().getId(),
                        savedProject.getOwner().getEmail(),
                        savedProject.getOwner().getFirstName(),
                        oldStatus,
                        savedProject.getStatus(),
                        teamMemberEmails,
                        LocalDateTime.now()));

        // --- 9. Create Result ---
        UpdateProjectStatusCommandResult result =
                new UpdateProjectStatusCommandResult(
                        savedProject.getId(),
                        savedProject.getTitle(),
                        oldStatus.toString(),
                        savedProject.getStatus().toString());

        // --- 10. Response ---
        return ApiResponse.success(
                result, messageService.getMessage("project.status.updated.success"));
    }
}
