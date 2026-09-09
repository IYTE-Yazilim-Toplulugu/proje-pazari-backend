package com.iyte_yazilim.proje_pazari.application.commands.reviewApplication;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectApplicationMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles application review (approval/rejection).
 *
 * <p>Validates application existence, delegates status transition to the domain aggregate, and
 * publishes ApplicationReviewedEvent for email notifications. Enforces project capacity and status
 * constraints.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 2.0
 * @since 2026-02-01
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewApplicationHandler
        implements IRequestHandler<
                ReviewApplicationCommand, ApiResponse<ReviewApplicationCommandResult>> {

    private final ProjectApplicationRepository applicationRepository;
    private final ProjectRepository
            projectRepository; // <-- Added to persist the incremented team size
    private final ProjectApplicationMapper applicationMapper;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ProjectMapper projectMapper; // <-- Added mapper injection

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<ReviewApplicationCommandResult> handle(ReviewApplicationCommand command) {

        // --- 1. Verify Application Exists ---
        ProjectApplicationEntity applicationEntity =
                applicationRepository
                        .findById(command.applicationId())
                        .orElseThrow(
                                () -> new ApplicationNotFoundException(command.applicationId()));

        // --- 2. Authorize before any mutation or event publication ---
        boolean isProjectOwner =
                applicationEntity.getProject().getOwner().getId().equals(command.requesterId());
        boolean isAdministrator = command.requesterRole() == RoleType.ADMIN;
        if (!isProjectOwner && !isAdministrator) {
            return ApiResponse.forbidden(messageService.getMessage("project.owner.mismatch"));
        }

        // --- 3. Validate Review Status Input ---
        if (command.status() != ApplicationStatus.APPROVED
                && command.status() != ApplicationStatus.REJECTED) {
            return ApiResponse.badRequest(messageService.getMessage("error.invalid.review.status"));
        }

        // --- 4. Map to domain aggregate and perform guarded transition ---
        ProjectApplication application = applicationMapper.entityToDomain(applicationEntity);

        if (command.status() == ApplicationStatus.APPROVED) {
            // --- 4a. Enforce Domain Rules for Approvals ---
            ProjectEntity projectEntity = applicationEntity.getProject();
            Project projectDomain = projectMapper.entityToDomain(projectEntity);

            // Check if project is OPEN and not full
            if (!projectDomain.canAcceptApplications()) {
                return ApiResponse.badRequest(
                        messageService.getMessage("project.cannot.accept.applications"));
            }

            try {
                // Increment capacity via domain model
                projectDomain.incrementTeamSize();

                // Sync the incremented value back to the infrastructure entity
                projectEntity.setCurrentTeamSize(projectDomain.getCurrentTeamSize());
                projectRepository.save(projectEntity); // Persist the new team size

            } catch (IllegalStateException e) {
                log.warn("Project capacity check failed: {}", e.getMessage());
                return ApiResponse.failure(
                        ErrorCode.ILLEGAL_APPLICATION_STATE,
                        messageService.getMessage("application.illegal.state"));
            }

            // --- 4b. Approve via domain aggregate (enforces PENDING guard) ---
            application.approve(command.reviewMessage());
        } else {
            // --- 4c. Reject via domain aggregate (enforces PENDING guard) ---
            application.reject(command.reviewMessage());
        }

        // --- 5. Sync review result back to persistence entity ---
        applicationEntity.setStatus(application.getStatus());
        applicationEntity.setReviewMessage(application.getReviewMessage());

        // --- 6. Persistence ---
        ProjectApplicationEntity savedApplication = applicationRepository.save(applicationEntity);

        // --- 7. Publish Event for Email Notifications ---
        applicationEventPublisher.publishEvent(
                new ApplicationReviewedEvent(
                        savedApplication.getId(),
                        savedApplication.getProject().getId(),
                        savedApplication.getUser().getEmail(),
                        savedApplication.getProject().getTitle(),
                        savedApplication.getUser().getFirstName(),
                        savedApplication.getProject().getOwner().getFirstName(),
                        savedApplication.getProject().getOwner().getEmail(),
                        savedApplication.getStatus(),
                        command.reviewMessage() != null ? command.reviewMessage() : ""));

        // --- 8. Create Result ---
        ReviewApplicationCommandResult result =
                new ReviewApplicationCommandResult(
                        savedApplication.getId(),
                        savedApplication.getProject().getId(),
                        savedApplication.getProject().getTitle(),
                        savedApplication.getStatus().toString());

        // --- 9. Response ---
        return ApiResponse.success(
                result, messageService.getMessage("application.reviewed.success"));
    }
}
