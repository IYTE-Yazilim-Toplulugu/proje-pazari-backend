package com.iyte_yazilim.proje_pazari.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationReviewFailure;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationReviewException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.IllegalApplicationStateException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectApplicationMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;

@ExtendWith(MockitoExtension.class)
class ApplicationReviewServiceTest {

    private static final String APPLICATION_ID = "01APPLICATION000000000000";
    private static final String PROJECT_ID = "01PROJECT00000000000000000";

    @Mock private ProjectApplicationRepository applicationRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectApplicationMapper applicationMapper;
    @Mock private ProjectMapper projectMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ApplicationReviewService service;

    private ProjectEntity project;
    private ProjectApplicationEntity application;

    @BeforeEach
    void setUp() {
        UserEntity owner = user("01OWNER000000000000000000", "owner@test.dev", "Owner");
        UserEntity applicant = user("01APPLICANT00000000000000", "applicant@test.dev", "Applicant");

        project = new ProjectEntity();
        project.setId(PROJECT_ID);
        project.setTitle("Consistent Reviews");
        project.setOwner(owner);
        project.setStatus(ProjectStatus.OPEN);
        project.setCurrentTeamSize(1);
        project.setMaxTeamSize(3);
        project.setDeadline(LocalDateTime.now().plusDays(1));

        application = new ProjectApplicationEntity();
        application.setId(APPLICATION_ID);
        application.setProject(project);
        application.setUser(applicant);
        application.setStatus(ApplicationStatus.PENDING);

        when(applicationRepository.findByIdWithLock(APPLICATION_ID))
                .thenReturn(Optional.of(application));
        lenient()
                .when(applicationMapper.entityToDomain(application))
                .thenAnswer(
                        invocation -> {
                            ProjectApplication domain = new ProjectApplication();
                            domain.reconstitute(
                                    null,
                                    null,
                                    application.getStatus(),
                                    application.getReviewMessage());
                            return domain;
                        });
        lenient()
                .when(projectRepository.findByIdWithLock(PROJECT_ID))
                .thenReturn(Optional.of(project));
        lenient()
                .when(projectMapper.entityToDomain(project))
                .thenAnswer(
                        invocation -> {
                            Project domain = new Project();
                            domain.reconstitute(project.getStatus(), project.getCurrentTeamSize());
                            domain.setMaxTeamSize(project.getMaxTeamSize());
                            domain.setDeadline(project.getDeadline());
                            return domain;
                        });
        lenient().when(applicationRepository.saveAndFlush(application)).thenReturn(application);
    }

    @Test
    void approvalLocksBothAggregatesIncrementsTeamSizeAndPublishesEvent() {
        var result = service.review(APPLICATION_ID, ApplicationStatus.APPROVED, "Welcome");

        assertThat(result.status()).isEqualTo("APPROVED");
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(application.getReviewMessage()).isEqualTo("Welcome");
        assertThat(project.getCurrentTeamSize()).isEqualTo(2);
        verify(applicationRepository).findByIdWithLock(APPLICATION_ID);
        verify(projectRepository).findByIdWithLock(PROJECT_ID);
        verify(projectRepository).save(project);
        verify(applicationRepository).saveAndFlush(application);
        ArgumentCaptor<ApplicationReviewedEvent> event =
                ArgumentCaptor.forClass(ApplicationReviewedEvent.class);
        verify(eventPublisher).publishEvent(event.capture());
        assertThat(event.getValue().status()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(event.getValue().reviewMessage()).isEqualTo("Welcome");
    }

    @Test
    void rejectionUsesTheSamePersistenceAndEventPathWithoutChangingTeamSize() {
        var result = service.review(APPLICATION_ID, ApplicationStatus.REJECTED, "Not this time");

        assertThat(result.status()).isEqualTo("REJECTED");
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(application.getReviewMessage()).isEqualTo("Not this time");
        assertThat(project.getCurrentTeamSize()).isEqualTo(1);
        verifyNoInteractions(projectMapper);
        verify(projectRepository, never()).findByIdWithLock(any());
        verify(projectRepository, never()).save(any());
        verify(applicationRepository).saveAndFlush(application);
        verify(eventPublisher).publishEvent(any(ApplicationReviewedEvent.class));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("ineligibleProjects")
    void ineligibleProjectRejectsApprovalWithoutMutationOrEvent(
            ProjectStatus status,
            ApplicationReviewFailure expectedFailure,
            LocalDateTime deadline,
            int currentTeamSize,
            int maxTeamSize) {
        project.setStatus(status);
        project.setDeadline(deadline);
        project.setCurrentTeamSize(currentTeamSize);
        project.setMaxTeamSize(maxTeamSize);

        assertThatThrownBy(() -> service.review(APPLICATION_ID, ApplicationStatus.APPROVED, null))
                .isInstanceOfSatisfying(
                        ApplicationReviewException.class,
                        exception -> assertThat(exception.getFailure()).isEqualTo(expectedFailure));

        assertThat(project.getCurrentTeamSize()).isEqualTo(currentTeamSize);
        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        verify(projectRepository, never()).save(any());
        verify(applicationRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any(ApplicationReviewedEvent.class));
    }

    @Test
    void nonPendingApplicationDoesNotIncrementTeamSizeOrPublishEvent() {
        application.setStatus(ApplicationStatus.APPROVED);

        assertThatThrownBy(() -> service.review(APPLICATION_ID, ApplicationStatus.APPROVED, null))
                .isInstanceOf(IllegalApplicationStateException.class);

        assertThat(project.getCurrentTeamSize()).isEqualTo(1);
        verifyNoInteractions(projectMapper);
        verify(projectRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(ApplicationReviewedEvent.class));
    }

    @Test
    void optimisticConflictBecomesDeterministicAndPublishesNoEvent() {
        when(applicationRepository.saveAndFlush(application))
                .thenThrow(new OptimisticLockingFailureException("stale project version"));

        assertThatThrownBy(() -> service.review(APPLICATION_ID, ApplicationStatus.APPROVED, null))
                .isInstanceOfSatisfying(
                        ApplicationReviewException.class,
                        exception ->
                                assertThat(exception.getFailure())
                                        .isEqualTo(
                                                ApplicationReviewFailure
                                                        .CONCURRENT_REVIEW_CONFLICT));

        verify(eventPublisher, never()).publishEvent(any(ApplicationReviewedEvent.class));
    }

    private static Stream<Arguments> ineligibleProjects() {
        return Stream.of(
                Arguments.of(
                        ProjectStatus.DRAFT,
                        ApplicationReviewFailure.PROJECT_NOT_OPEN,
                        LocalDateTime.now().plusDays(1),
                        1,
                        3),
                Arguments.of(
                        ProjectStatus.OPEN,
                        ApplicationReviewFailure.PROJECT_APPLICATION_DEADLINE_PASSED,
                        LocalDateTime.now().minusDays(1),
                        1,
                        3),
                Arguments.of(
                        ProjectStatus.OPEN,
                        ApplicationReviewFailure.PROJECT_FULL,
                        LocalDateTime.now().plusDays(1),
                        3,
                        3));
    }

    private static UserEntity user(String id, String email, String firstName) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        return user;
    }
}
