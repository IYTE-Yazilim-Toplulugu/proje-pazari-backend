package com.iyte_yazilim.proje_pazari.application.commands.submitApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationSubmittedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.DomainException;
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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class SubmitApplicationHandlerTest {

    private static final String PROJECT_ID = "01PROJECT00000000000000000";
    private static final String OWNER_ID = "01OWNER000000000000000000";
    private static final String APPLICANT_ID = "01APPLICANT00000000000000";
    private static final String APPLICATION_ID = "01APPLICATION000000000000";

    @Mock private ProjectApplicationRepository applicationRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProjectApplicationMapper applicationMapper;
    @Mock private ProjectMapper projectMapper;
    @Mock private MessageService messageService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private BusinessMetricsService metricsService;

    @InjectMocks private SubmitApplicationHandler handler;

    private UserEntity owner;
    private UserEntity applicant;
    private ProjectEntity project;

    @BeforeEach
    void setUp() {
        owner = user(OWNER_ID, "owner@std.iyte.edu.tr", "Owner");
        applicant = user(APPLICANT_ID, "applicant@std.iyte.edu.tr", "Applicant");

        project = new ProjectEntity();
        project.setId(PROJECT_ID);
        project.setTitle("Safe Project");
        project.setOwner(owner);
        project.setStatus(ProjectStatus.OPEN);
        project.setCurrentTeamSize(1);
        project.setMaxTeamSize(3);
        project.setDeadline(LocalDateTime.now().plusDays(1));

        when(projectRepository.findByIdWithLock(PROJECT_ID)).thenReturn(Optional.of(project));
        when(userRepository.findById(APPLICANT_ID)).thenReturn(Optional.of(applicant));
        lenient()
                .when(applicationRepository.existsByProjectIdAndUserId(PROJECT_ID, APPLICANT_ID))
                .thenReturn(false);
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
        lenient()
                .when(applicationRepository.saveAndFlush(any(ProjectApplicationEntity.class)))
                .thenAnswer(
                        invocation -> {
                            ProjectApplicationEntity entity = invocation.getArgument(0);
                            entity.setId(APPLICATION_ID);
                            entity.setStatus(ApplicationStatus.PENDING);
                            return entity;
                        });
    }

    @Test
    void projectOwnerCannotApplyToOwnProject() {
        project.setOwner(applicant);

        ApiResponse<SubmitApplicationCommandResult> response = submit();

        assertFailure(response, ResponseCode.FORBIDDEN, "SELF_APPLICATION_NOT_ALLOWED");
        verify(applicationRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any(ApplicationSubmittedEvent.class));
        verify(metricsService).incrementApplicationSubmissionFailure();
    }

    @ParameterizedTest(name = "status {0}")
    @MethodSource("nonOpenStatuses")
    void nonOpenProjectsRejectApplications(ProjectStatus status) {
        project.setStatus(status);

        ApiResponse<SubmitApplicationCommandResult> response = submit();

        assertFailure(response, ResponseCode.CONFLICT, "PROJECT_NOT_OPEN");
        verify(applicationRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any(ApplicationSubmittedEvent.class));
    }

    @Test
    void expiredProjectsRejectApplications() {
        project.setDeadline(LocalDateTime.now().minusSeconds(1));

        ApiResponse<SubmitApplicationCommandResult> response = submit();

        assertFailure(response, ResponseCode.CONFLICT, "PROJECT_APPLICATION_DEADLINE_PASSED");
        verify(applicationRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any(ApplicationSubmittedEvent.class));
    }

    @Test
    void fullProjectsRejectApplications() {
        project.setCurrentTeamSize(3);
        project.setMaxTeamSize(3);

        ApiResponse<SubmitApplicationCommandResult> response = submit();

        assertFailure(response, ResponseCode.CONFLICT, "PROJECT_FULL");
        verify(applicationRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any(ApplicationSubmittedEvent.class));
    }

    @Test
    void validSubmissionCreatesOnePendingApplicationAndPublishesOneEvent() {
        ApiResponse<SubmitApplicationCommandResult> response = submit();

        assertThat(response.getCode()).isEqualTo(ResponseCode.CREATED);
        assertThat(response.getData().applicationId()).isEqualTo(APPLICATION_ID);
        assertThat(response.getData().status()).isEqualTo("PENDING");
        verify(applicationRepository).saveAndFlush(any(ProjectApplicationEntity.class));
        verify(projectRepository).findByIdWithLock(PROJECT_ID);
        verify(eventPublisher).publishEvent(any(ApplicationSubmittedEvent.class));
        verify(metricsService).incrementApplicationSubmissionSuccess();
    }

    @Test
    void duplicatePolicyAlsoBlocksTerminalApplications() {
        when(applicationRepository.existsByProjectIdAndUserId(PROJECT_ID, APPLICANT_ID))
                .thenReturn(true);

        ApiResponse<SubmitApplicationCommandResult> response = submit();

        assertFailure(response, ResponseCode.CONFLICT, "APPLICATION_ALREADY_EXISTS");
        verify(applicationRepository, never()).saveAndFlush(any());
        verify(eventPublisher, never()).publishEvent(any(ApplicationSubmittedEvent.class));
    }

    @Test
    void uniqueConstraintRaceThrowsTheSameDuplicateErrorWithoutPublishingAnEvent() {
        when(applicationRepository.saveAndFlush(any(ProjectApplicationEntity.class)))
                .thenThrow(
                        new DataIntegrityViolationException(
                                "duplicate key violates constraint uk_project_applications_project_user"));

        assertThatThrownBy(this::submit)
                .isInstanceOfSatisfying(
                        DomainException.class,
                        exception ->
                                assertThat(exception.getErrorCode().name())
                                        .isEqualTo("APPLICATION_ALREADY_EXISTS"));
        verify(eventPublisher, never()).publishEvent(any(ApplicationSubmittedEvent.class));
        verify(metricsService).incrementApplicationSubmissionFailure();
    }

    @Test
    void unrelatedPersistenceFailureIsNotMisreportedAsADuplicate() {
        DataIntegrityViolationException failure =
                new DataIntegrityViolationException("null value in column project_id");
        when(applicationRepository.saveAndFlush(any(ProjectApplicationEntity.class)))
                .thenThrow(failure);

        assertThatThrownBy(this::submit).isSameAs(failure);
        verify(eventPublisher, never()).publishEvent(any(ApplicationSubmittedEvent.class));
    }

    private ApiResponse<SubmitApplicationCommandResult> submit() {
        return handler.handle(new SubmitApplicationCommand(PROJECT_ID, APPLICANT_ID));
    }

    private static void assertFailure(
            ApiResponse<SubmitApplicationCommandResult> response,
            ResponseCode responseCode,
            String errorCode) {
        assertThat(response.getCode()).isEqualTo(responseCode);
        assertThat(response.getErrorCode()).isNotNull();
        assertThat(response.getErrorCode().name()).isEqualTo(errorCode);
        assertThat(response.getData()).isNull();
    }

    private static Stream<Arguments> nonOpenStatuses() {
        return Stream.of(
                Arguments.of(ProjectStatus.DRAFT),
                Arguments.of(ProjectStatus.IN_PROGRESS),
                Arguments.of(ProjectStatus.COMPLETED),
                Arguments.of(ProjectStatus.CANCELLED));
    }

    private static UserEntity user(String id, String email, String firstName) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        return user;
    }
}
