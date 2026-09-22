package com.iyte_yazilim.proje_pazari.application.commands.reviewApplication;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.ApplicationReviewService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Authorizes an individual review and delegates its state change to the shared review service. */
@Component
@RequiredArgsConstructor
public class ReviewApplicationHandler
        implements IRequestHandler<
                ReviewApplicationCommand, ApiResponse<ReviewApplicationCommandResult>> {

    private final ProjectApplicationRepository applicationRepository;
    private final ApplicationReviewService reviewService;
    private final MessageService messageService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<ReviewApplicationCommandResult> handle(ReviewApplicationCommand command) {
        ProjectApplicationEntity application =
                applicationRepository
                        .findById(command.applicationId())
                        .orElseThrow(
                                () -> new ApplicationNotFoundException(command.applicationId()));

        boolean isProjectOwner =
                application.getProject().getOwner().getId().equals(command.requesterId());
        boolean isAdministrator = command.requesterRole() == RoleType.ADMIN;
        if (!isProjectOwner && !isAdministrator) {
            return ApiResponse.forbidden(messageService.getMessage("project.owner.mismatch"));
        }

        if (command.status() != ApplicationStatus.APPROVED
                && command.status() != ApplicationStatus.REJECTED) {
            return ApiResponse.badRequest(messageService.getMessage("error.invalid.review.status"));
        }

        ReviewApplicationCommandResult result =
                reviewService.review(
                        command.applicationId(), command.status(), command.reviewMessage());
        return ApiResponse.success(
                result, messageService.getMessage("application.reviewed.success"));
    }
}
