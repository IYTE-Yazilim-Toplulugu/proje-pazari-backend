package com.iyte_yazilim.proje_pazari.application.commands.reviewApplication;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
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

        // --- 2. Validate Review Status Input ---
        if (command.status() != ApplicationStatus.APPROVED
                && command.status() != ApplicationStatus.REJECTED) {
            return ApiResponse.badRequest(messageService.getMessage("error.invalid.review.status"));
        }

        // --- 3. Map to domain aggregate and perform guarded transition ---
        ProjectApplication application = applicationMapper.entityToDomain(applicationEntity);

        if (command.status() == ApplicationStatus.APPROVED) {
            // --- 3a. Enforce Domain Rules for Approvals ---
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
                // Catch the capacity limit exception thrown by the domain
                return ApiResponse.badRequest(e.getMessage());
            }

            // --- 3b. Approve via domain aggregate (enforces PENDING guard) ---
            application.approve(command.reviewMessage());
        } else {
            // --- 3c. Reject via domain aggregate (enforces PENDING guard) ---
            application.reject(command.reviewMessage());
        }

        // --- 4. Sync review result back to persistence entity ---
        applicationEntity.setStatus(application.getStatus());
        applicationEntity.setReviewMessage(application.getReviewMessage());

        // --- 5. Persistence ---
        ProjectApplicationEntity savedApplication = applicationRepository.save(applicationEntity);

        // --- 6. Publish Event for Email Notifications ---
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

        // --- 7. Create Result ---
        ReviewApplicationCommandResult result =
                new ReviewApplicationCommandResult(
                        savedApplication.getId(),
                        savedApplication.getProject().getId(),
                        savedApplication.getProject().getTitle(),
                        savedApplication.getStatus().toString());

        // --- 8. Response ---
        return ApiResponse.success(
                result, messageService.getMessage("application.reviewed.success"));
    }
}
