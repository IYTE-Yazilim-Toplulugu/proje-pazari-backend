package com.iyte_yazilim.proje_pazari.application.commands.deleteProject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectDeletedEvent;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.List;
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
class DeleteProjectHandlerTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectApplicationRepository applicationRepository;
    @Mock private MessageService messageService;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    // --- ADDED MOCK MAPPER ---
    @Mock private ProjectMapper projectMapper;

    @InjectMocks private DeleteProjectHandler handler;

    private String projectId;
    private String ownerId;
    private ProjectEntity projectEntity;
    private UserEntity ownerEntity;

    @BeforeEach
    void setUp() {
        projectId = Ulid.fast().toString();
        ownerId = Ulid.fast().toString();

        ownerEntity = new UserEntity();
        ownerEntity.setId(ownerId);
        ownerEntity.setEmail("owner@test.com");
        ownerEntity.setFirstName("Test");

        projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);
        projectEntity.setTitle("Test Project");
        projectEntity.setStatus(ProjectStatus.OPEN);
        projectEntity.setOwner(ownerEntity);
    }

    // Helper method to setup the mapper mock for a specific test case
    private void setupMockMapper() {
        Project mockDomainProject = new Project();
        // Hydrate the domain object with the entity's current status so it can evaluate business
        // rules
        mockDomainProject.reconstitute(projectEntity.getStatus(), 0);
        when(projectMapper.entityToDomain(any())).thenReturn(mockDomainProject);
    }

    @Test
    @DisplayName("Should delete project and collect pending application info for notifications")
    void shouldDeleteProject_andCollectPendingApplicationInfo() {
        // Arrange
        setupMockMapper(); // Setup the mapper for this test

        UserEntity applicantUser = new UserEntity();
        applicantUser.setId(Ulid.fast().toString());
        applicantUser.setEmail("applicant@test.com");
        applicantUser.setFirstName("Applicant");

        ProjectApplicationEntity pendingApp = new ProjectApplicationEntity();
        pendingApp.setId(Ulid.fast().toString());
        pendingApp.setStatus(ApplicationStatus.PENDING);
        pendingApp.setUser(applicantUser);

        ProjectApplicationEntity approvedApp = new ProjectApplicationEntity();
        approvedApp.setId(Ulid.fast().toString());
        approvedApp.setStatus(ApplicationStatus.APPROVED);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(applicationRepository.findByProjectId(projectId))
                .thenReturn(List.of(pendingApp, approvedApp));
        when(messageService.getMessage("project.deleted.success")).thenReturn("Deleted");

        // Act
        ApiResponse<Void> response = handler.handle(new DeleteProjectCommand(projectId, ownerId));

        // Assert
        assertEquals(ResponseCode.SUCCESS, response.getCode());

        // Verify no application saves occur (cascade handles deletion)
        verify(applicationRepository, never()).save(any());

        // Verify project was deleted
        verify(projectRepository).delete(projectEntity);

        // Verify event was published with correct data including applicant info
        ArgumentCaptor<ProjectDeletedEvent> eventCaptor =
                ArgumentCaptor.forClass(ProjectDeletedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        ProjectDeletedEvent event = eventCaptor.getValue();
        assertEquals(projectId, event.projectId());
        assertEquals("Test Project", event.projectTitle());
        assertEquals(ownerId, event.ownerId());
        assertEquals(1, event.rejectedApplicationCount());
        assertEquals(List.of("applicant@test.com"), event.applicantEmails());
        assertEquals(List.of("Applicant"), event.applicantNames());
    }

    @Test
    @DisplayName("Should return 404 when project not found")
    void shouldReturnNotFound_whenProjectDoesNotExist() {
        String unknownId = Ulid.fast().toString();
        when(projectRepository.findById(unknownId)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("project.not.found"), any(Object[].class)))
                .thenReturn("Not found");

        // Mapper is not called if project is not found, so we don't setupMockMapper() here

        ApiResponse<Void> response = handler.handle(new DeleteProjectCommand(unknownId, ownerId));

        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        verify(projectRepository, never()).delete(any());
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should return 403 when user is not the owner")
    void shouldReturnForbidden_whenNotOwner() {
        String otherUserId = Ulid.fast().toString();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(messageService.getMessage("project.owner.mismatch")).thenReturn("Forbidden");

        // Mapper is not called if ownership fails, so we don't setupMockMapper() here

        ApiResponse<Void> response =
                handler.handle(new DeleteProjectCommand(projectId, otherUserId));

        assertEquals(ResponseCode.FORBIDDEN, response.getCode());
        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should return 403 when project is IN_PROGRESS")
    void shouldReturnForbidden_whenProjectIsInProgress() {
        projectEntity.setStatus(ProjectStatus.IN_PROGRESS);
        setupMockMapper(); // Setup the mapper for this test

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(messageService.getMessage("project.delete.has.active.work"))
                .thenReturn("Cannot delete");

        ApiResponse<Void> response = handler.handle(new DeleteProjectCommand(projectId, ownerId));

        assertEquals(ResponseCode.FORBIDDEN, response.getCode());
        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should allow deletion of DRAFT project")
    void shouldAllowDeletion_whenProjectIsDraft() {
        projectEntity.setStatus(ProjectStatus.DRAFT);
        setupMockMapper(); // Setup the mapper for this test

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(applicationRepository.findByProjectId(projectId)).thenReturn(List.of());
        when(messageService.getMessage("project.deleted.success")).thenReturn("Deleted");

        ApiResponse<Void> response = handler.handle(new DeleteProjectCommand(projectId, ownerId));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(projectRepository).delete(projectEntity);
    }

    @Test
    @DisplayName("Should allow deletion of CANCELLED project")
    void shouldAllowDeletion_whenProjectIsCancelled() {
        projectEntity.setStatus(ProjectStatus.CANCELLED);
        setupMockMapper(); // Setup the mapper for this test

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(applicationRepository.findByProjectId(projectId)).thenReturn(List.of());
        when(messageService.getMessage("project.deleted.success")).thenReturn("Deleted");

        ApiResponse<Void> response = handler.handle(new DeleteProjectCommand(projectId, ownerId));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(projectRepository).delete(projectEntity);
    }
}
