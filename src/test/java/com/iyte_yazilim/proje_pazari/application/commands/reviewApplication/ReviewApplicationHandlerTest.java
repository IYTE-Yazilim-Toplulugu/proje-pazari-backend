package com.iyte_yazilim.proje_pazari.application.commands.reviewApplication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
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
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReviewApplicationHandlerTest {

    @Mock private ProjectApplicationRepository applicationRepository;

    // --- ADDED MOCKS FOR DOMAIN REFACTORING ---
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMapper projectMapper;
    @Mock private ProjectApplicationMapper applicationMapper;
    // ------------------------------------------

    @Mock private MessageService messageService;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks private ReviewApplicationHandler handler;

    private String applicationId;
    private String ownerId;
    private String applicantId;
    private ProjectApplicationEntity applicationEntity;

    @BeforeEach
    void setUp() {
        applicationId = Ulid.fast().toString();
        ownerId = Ulid.fast().toString();
        applicantId = Ulid.fast().toString();

        UserEntity owner = new UserEntity();
        owner.setId(ownerId);
        owner.setFirstName("Owner");
        owner.setEmail("owner@std.iyte.edu.tr");

        ProjectEntity project = new ProjectEntity();
        project.setId(Ulid.fast().toString());
        project.setTitle("Test Project");
        project.setOwner(owner);

        UserEntity applicant = new UserEntity();
        applicant.setId(applicantId);
        applicant.setFirstName("Applicant");
        applicant.setEmail("applicant@std.iyte.edu.tr");

        applicationEntity = new ProjectApplicationEntity();
        applicationEntity.setId(applicationId);
        applicationEntity.setStatus(ApplicationStatus.PENDING);
        applicationEntity.setProject(project);
        applicationEntity.setUser(applicant);

        lenient()
                .when(messageService.getMessage("application.reviewed.success"))
                .thenReturn("Application reviewed successfully");
        lenient()
                .when(messageService.getMessage("project.owner.mismatch"))
                .thenReturn("You must be the project owner to perform this action");
    }

    @Test
    @DisplayName("Should approve application successfully")
    void shouldApproveApplication_whenApplicationExists() {
        ReviewApplicationCommand command =
                new ReviewApplicationCommand(
                        applicationId,
                        ownerId,
                        RoleType.USER,
                        ApplicationStatus.APPROVED,
                        "Good fit");

        // --- DOMAIN MOCK BEHAVIOR ---
        Project mockDomainProject = new Project();
        mockDomainProject.reconstitute(ProjectStatus.OPEN, 0);
        mockDomainProject.setMaxTeamSize(5);
        when(projectMapper.entityToDomain(any())).thenReturn(mockDomainProject);

        ProjectApplication domainApp = new ProjectApplication();
        when(applicationMapper.entityToDomain(applicationEntity)).thenReturn(domainApp);
        // ----------------------------------

        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(applicationEntity));
        when(applicationRepository.save(applicationEntity)).thenReturn(applicationEntity);

        ApiResponse<ReviewApplicationCommandResult> response = handler.handle(command);

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(ApplicationStatus.APPROVED, applicationEntity.getStatus());
        assertEquals("Good fit", applicationEntity.getReviewMessage());
        assertEquals(applicationId, response.getData().applicationId());
        assertEquals(applicationEntity.getProject().getId(), response.getData().projectId());
        assertEquals("Test Project", response.getData().projectTitle());
        assertEquals("APPROVED", response.getData().status());

        ArgumentCaptor<ApplicationReviewedEvent> eventCaptor =
                ArgumentCaptor.forClass(ApplicationReviewedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("Good fit", eventCaptor.getValue().reviewMessage());

        // Verify the team size was persisted
        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    @DisplayName("Should reject application and persist review message")
    void shouldRejectApplicationAndPersistReviewMessage_whenApplicationExists() {
        ReviewApplicationCommand command =
                new ReviewApplicationCommand(
                        applicationId,
                        ownerId,
                        RoleType.USER,
                        ApplicationStatus.REJECTED,
                        "Need a different skill set");

        ProjectApplication domainApp = new ProjectApplication();
        when(applicationMapper.entityToDomain(applicationEntity)).thenReturn(domainApp);

        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(applicationEntity));
        when(applicationRepository.save(applicationEntity)).thenReturn(applicationEntity);

        ApiResponse<ReviewApplicationCommandResult> response = handler.handle(command);

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(ApplicationStatus.REJECTED, applicationEntity.getStatus());
        assertEquals("Need a different skill set", applicationEntity.getReviewMessage());
        assertEquals(applicationId, response.getData().applicationId());
        assertEquals(applicationEntity.getProject().getId(), response.getData().projectId());
        assertEquals("Test Project", response.getData().projectTitle());
        assertEquals("REJECTED", response.getData().status());

        ArgumentCaptor<ApplicationReviewedEvent> eventCaptor =
                ArgumentCaptor.forClass(ApplicationReviewedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("Need a different skill set", eventCaptor.getValue().reviewMessage());
        verify(projectRepository, never()).save(any(ProjectEntity.class));
    }

    @Test
    @DisplayName("Should reject WITHDRAWN as an invalid review status")
    void shouldRejectWithdrawnStatus() {
        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(applicationEntity));
        when(messageService.getMessage("error.invalid.review.status"))
                .thenReturn("Invalid review status");

        ApiResponse<ReviewApplicationCommandResult> response =
                handler.handle(
                        new ReviewApplicationCommand(
                                applicationId,
                                ownerId,
                                RoleType.USER,
                                ApplicationStatus.WITHDRAWN,
                                "Not a review"));

        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertNull(response.getData());
        verify(applicationRepository, never()).save(any());
        verify(projectRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Should throw ApplicationNotFoundException when application does not exist")
    void shouldThrowException_whenApplicationNotFound() {
        String unknownId = Ulid.fast().toString();
        ReviewApplicationCommand command =
                new ReviewApplicationCommand(
                        unknownId, ownerId, RoleType.USER, ApplicationStatus.APPROVED, null);

        when(applicationRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> handler.handle(command));
        verify(applicationRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Should forbid applicant review without side effects")
    void shouldForbidReview_whenRequesterDoesNotOwnProject() {
        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(applicationEntity));

        ApiResponse<ReviewApplicationCommandResult> response =
                handler.handle(
                        new ReviewApplicationCommand(
                                applicationId,
                                applicantId,
                                RoleType.USER,
                                ApplicationStatus.APPROVED,
                                "self-approved"));

        assertEquals(ResponseCode.FORBIDDEN, response.getCode());
        assertEquals(ApplicationStatus.PENDING, applicationEntity.getStatus());
        verifyNoInteractions(applicationMapper, projectMapper);
        verify(applicationRepository, never()).save(any());
        verify(projectRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Should allow explicit administrator override")
    void shouldAllowReview_whenRequesterIsAdministrator() {
        ProjectApplication domainApp = new ProjectApplication();
        when(applicationMapper.entityToDomain(applicationEntity)).thenReturn(domainApp);
        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(applicationEntity));
        when(applicationRepository.save(applicationEntity)).thenReturn(applicationEntity);

        ApiResponse<ReviewApplicationCommandResult> response =
                handler.handle(
                        new ReviewApplicationCommand(
                                applicationId,
                                applicantId,
                                RoleType.ADMIN,
                                ApplicationStatus.REJECTED,
                                "Admin decision"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(ApplicationStatus.REJECTED, applicationEntity.getStatus());
        verify(applicationRepository).save(applicationEntity);
        verify(applicationEventPublisher).publishEvent(any(ApplicationReviewedEvent.class));
    }
}
