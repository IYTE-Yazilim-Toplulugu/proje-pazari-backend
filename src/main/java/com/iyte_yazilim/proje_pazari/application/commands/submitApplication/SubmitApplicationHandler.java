package com.iyte_yazilim.proje_pazari.application.commands.submitApplication;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationSubmittedEvent;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.results.SubmitApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.metrics.BusinessMetricsService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectApplicationMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles application submission to projects.
 *
 * <p>Validates project and user existence, checks for duplicate applications, creates the
 * application entity, and publishes ApplicationSubmittedEvent for email notifications.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 */
@Component
@RequiredArgsConstructor
public class SubmitApplicationHandler
        implements IRequestHandler<
                SubmitApplicationCommand, ApiResponse<SubmitApplicationCommandResult>> {

    private final ProjectApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectApplicationMapper applicationMapper;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BusinessMetricsService metricsService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<SubmitApplicationCommandResult> handle(SubmitApplicationCommand command) {

        // --- 1. Verify Project Exists ---
        ProjectEntity projectEntity = projectRepository.findById(command.projectId()).orElse(null);
        if (projectEntity == null) {
            metricsService.incrementApplicationSubmissionFailure();
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "project.not.found", new Object[] {command.projectId()}));
        }

        // --- 3. Verify User Exists ---
        UserEntity userEntity = userRepository.findById(command.userId()).orElse(null);
        if (userEntity == null) {
            metricsService.incrementApplicationSubmissionFailure();
            return ApiResponse.notFound(
                    messageService.getMessage("user.not.found", new Object[] {command.userId()}));
        }

        // --- 4. Check for Duplicate Application ---
        boolean alreadyApplied =
                applicationRepository.existsByProjectIdAndUserId(
                        command.projectId(), command.userId());
        if (alreadyApplied) {
            metricsService.incrementApplicationSubmissionFailure();
            return ApiResponse.badRequest(messageService.getMessage("application.already.exists"));
        }

        // --- 5. Create Application Entity ---
        ProjectApplicationEntity applicationEntity = new ProjectApplicationEntity();
        applicationEntity.setProject(projectEntity);
        applicationEntity.setUser(userEntity);
        applicationEntity.setStatus(ApplicationStatus.PENDING);

        // --- 6. Persistence ---
        ProjectApplicationEntity savedApplication = applicationRepository.save(applicationEntity);

        // --- 7. Publish Event for Email Notifications ---
        applicationEventPublisher.publishEvent(
                new ApplicationSubmittedEvent(
                        savedApplication.getId(),
                        projectEntity.getId(),
                        projectEntity.getTitle(),
                        userEntity.getId(),
                        userEntity.getEmail(),
                        userEntity.getFirstName(),
                        projectEntity.getOwner().getEmail(),
                        projectEntity.getOwner().getFirstName()));

        // --- 8. Create Result ---
        SubmitApplicationCommandResult result =
                new SubmitApplicationCommandResult(
                        savedApplication.getId(),
                        projectEntity.getId(),
                        projectEntity.getTitle(),
                        savedApplication.getStatus().toString());

        // --- 9. Response ---
        metricsService.incrementApplicationSubmissionSuccess();
        return ApiResponse.created(
                result, messageService.getMessage("application.submitted.success"));
    }
}
