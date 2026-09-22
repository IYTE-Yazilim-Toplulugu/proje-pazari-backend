package com.iyte_yazilim.proje_pazari.application.commands.submitApplication;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationSubmittedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.DuplicateApplicationException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ProjectNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.results.SubmitApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.metrics.BusinessMetricsService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectApplicationMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
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

    private static final String UNIQUE_APPLICATION_CONSTRAINT =
            "uk_project_applications_project_user";

    private final ProjectApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectApplicationMapper applicationMapper;
    private final ProjectMapper projectMapper;
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
        ProjectEntity projectEntity =
                projectRepository.findByIdWithLock(command.projectId()).orElse(null);
        if (projectEntity == null) {
            metricsService.incrementApplicationSubmissionFailure();
            throw new ProjectNotFoundException(command.projectId());
        }

        // --- 3. Verify User Exists ---
        UserEntity userEntity = userRepository.findById(command.userId()).orElse(null);
        if (userEntity == null) {
            metricsService.incrementApplicationSubmissionFailure();
            throw new UserNotFoundException(command.userId());
        }

        // --- 4. Enforce application eligibility at the server boundary ---
        if (projectEntity.getOwner().getId().equals(userEntity.getId())) {
            return reject(ErrorCode.SELF_APPLICATION_NOT_ALLOWED);
        }

        Project project = projectMapper.entityToDomain(projectEntity);
        if (!project.canAcceptApplications()) {
            if (!project.isOpen()) {
                return reject(ErrorCode.PROJECT_NOT_OPEN);
            }
            if (project.isExpired()) {
                return reject(ErrorCode.PROJECT_APPLICATION_DEADLINE_PASSED);
            }
            return reject(ErrorCode.PROJECT_FULL);
        }

        // --- 5. Check for Duplicate Application ---
        boolean alreadyApplied =
                applicationRepository.existsByProjectIdAndUserId(
                        command.projectId(), command.userId());
        if (alreadyApplied) {
            return reject(ErrorCode.APPLICATION_ALREADY_EXISTS);
        }

        // --- 6. Create Application Entity (defaults to PENDING status) ---
        ProjectApplicationEntity applicationEntity = new ProjectApplicationEntity();
        applicationEntity.setProject(projectEntity);
        applicationEntity.setUser(userEntity);

        // --- 7. Persistence ---
        ProjectApplicationEntity savedApplication;
        try {
            savedApplication = applicationRepository.saveAndFlush(applicationEntity);
        } catch (DataIntegrityViolationException exception) {
            if (!isDuplicateApplicationConstraint(exception)) {
                throw exception;
            }
            metricsService.incrementApplicationSubmissionFailure();
            throw new DuplicateApplicationException(
                    command.projectId(), command.userId(), exception);
        }

        // --- 8. Publish Event for Email Notifications ---
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

        // --- 9. Create Result ---
        SubmitApplicationCommandResult result =
                new SubmitApplicationCommandResult(
                        savedApplication.getId(),
                        projectEntity.getId(),
                        projectEntity.getTitle(),
                        savedApplication.getStatus().toString());

        // --- 10. Response ---
        metricsService.incrementApplicationSubmissionSuccess();
        return ApiResponse.created(
                result, messageService.getMessage("application.submitted.success"));
    }

    private ApiResponse<SubmitApplicationCommandResult> reject(ErrorCode errorCode) {
        metricsService.incrementApplicationSubmissionFailure();
        return ApiResponse.failure(errorCode, messageService.getMessage(errorCode.getMessageKey()));
    }

    private boolean isDuplicateApplicationConstraint(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof ConstraintViolationException constraintViolation
                    && UNIQUE_APPLICATION_CONSTRAINT.equals(
                            constraintViolation.getConstraintName())) {
                return true;
            }
            if (current.getMessage() != null
                    && current.getMessage().contains(UNIQUE_APPLICATION_CONSTRAINT)) {
                return true;
            }
        }
        return false;
    }
}
