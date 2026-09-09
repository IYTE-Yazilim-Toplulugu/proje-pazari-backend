package com.iyte_yazilim.proje_pazari.application.services;

import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationReviewFailure;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationReviewException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ProjectNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectApplicationMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Applies the same transactional state transition for every application-review entry point. */
@Service
@RequiredArgsConstructor
public class ApplicationReviewService {

    private final ProjectApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectApplicationMapper applicationMapper;
    private final ProjectMapper projectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(propagation = Propagation.MANDATORY)
    public ReviewApplicationCommandResult review(
            String applicationId, ApplicationStatus status, String reviewMessage) {
        try {
            return reviewWithLocks(applicationId, status, reviewMessage);
        } catch (OptimisticLockingFailureException | PessimisticLockingFailureException exception) {
            throw new ApplicationReviewException(
                    ApplicationReviewFailure.CONCURRENT_REVIEW_CONFLICT, exception);
        }
    }

    private ReviewApplicationCommandResult reviewWithLocks(
            String applicationId, ApplicationStatus status, String reviewMessage) {
        ProjectApplicationEntity applicationEntity =
                applicationRepository
                        .findByIdWithLock(applicationId)
                        .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
        ProjectApplication application = applicationMapper.entityToDomain(applicationEntity);

        if (status == ApplicationStatus.APPROVED) {
            application.approve(reviewMessage);
            approveAgainstLockedProject(applicationEntity);
        } else if (status == ApplicationStatus.REJECTED) {
            application.reject(reviewMessage);
        } else {
            throw new IllegalArgumentException("Review status must be APPROVED or REJECTED");
        }

        applicationEntity.setStatus(application.getStatus());
        applicationEntity.setReviewMessage(application.getReviewMessage());
        ProjectApplicationEntity savedApplication =
                applicationRepository.saveAndFlush(applicationEntity);
        publishReviewEvent(savedApplication, reviewMessage);

        return new ReviewApplicationCommandResult(
                savedApplication.getId(),
                savedApplication.getProject().getId(),
                savedApplication.getProject().getTitle(),
                savedApplication.getStatus().toString());
    }

    private void approveAgainstLockedProject(ProjectApplicationEntity applicationEntity) {
        String projectId = applicationEntity.getProject().getId();
        ProjectEntity projectEntity =
                projectRepository
                        .findByIdWithLock(projectId)
                        .orElseThrow(() -> new ProjectNotFoundException(projectId));
        Project project = projectMapper.entityToDomain(projectEntity);

        if (!project.isOpen()) {
            throw new ApplicationReviewException(ApplicationReviewFailure.PROJECT_NOT_OPEN);
        }
        if (project.isExpired()) {
            throw new ApplicationReviewException(
                    ApplicationReviewFailure.PROJECT_APPLICATION_DEADLINE_PASSED);
        }
        if (project.isFull()) {
            throw new ApplicationReviewException(ApplicationReviewFailure.PROJECT_FULL);
        }

        project.incrementTeamSize();
        projectEntity.setCurrentTeamSize(project.getCurrentTeamSize());
        projectRepository.save(projectEntity);
    }

    private void publishReviewEvent(ProjectApplicationEntity application, String reviewMessage) {
        applicationEventPublisher.publishEvent(
                new ApplicationReviewedEvent(
                        application.getId(),
                        application.getProject().getId(),
                        application.getUser().getEmail(),
                        application.getProject().getTitle(),
                        application.getUser().getFirstName(),
                        application.getProject().getOwner().getFirstName(),
                        application.getProject().getOwner().getEmail(),
                        application.getStatus(),
                        reviewMessage != null ? reviewMessage : ""));
    }
}
