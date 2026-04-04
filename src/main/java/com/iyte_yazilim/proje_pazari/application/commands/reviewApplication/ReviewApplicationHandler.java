package com.iyte_yazilim.proje_pazari.application.commands.reviewApplication;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles application review (approval/rejection).
 *
 * <p>Validates application existence, updates status, and publishes ApplicationReviewedEvent for
 * email notifications.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 */
@Component
@RequiredArgsConstructor
public class ReviewApplicationHandler
        implements IRequestHandler<
                ReviewApplicationCommand, ApiResponse<ReviewApplicationCommandResult>> {

    private final ProjectApplicationRepository applicationRepository;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;

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

        // --- 2. Validate Review Status ---
        if (command.status()
                        != com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus.APPROVED
                && command.status()
                        != com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus.REJECTED) {
            return ApiResponse.badRequest(messageService.getMessage("error.invalid.review.status"));
        }

        // --- 3. Update Status ---
        applicationEntity.setStatus(command.status());

        // --- 4. Persistence ---
        ProjectApplicationEntity savedApplication = applicationRepository.save(applicationEntity);

        // --- 5. Publish Event for Email Notifications ---
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

        // --- 6. Create Result ---
        ReviewApplicationCommandResult result =
                new ReviewApplicationCommandResult(
                        savedApplication.getId(),
                        savedApplication.getProject().getId(),
                        savedApplication.getProject().getTitle(),
                        savedApplication.getStatus().toString());

        // --- 7. Response ---
        return ApiResponse.success(
                result, messageService.getMessage("application.reviewed.success"));
    }
}
