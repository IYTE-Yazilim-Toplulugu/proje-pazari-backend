package com.iyte_yazilim.proje_pazari.application.commands.updateProject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.mappers.ProjectDetailDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateProjectHandlerTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMapper projectMapper;
    @Mock private ProjectDetailDtoMapper projectDetailDtoMapper;
    @Mock private MessageService messageService;

    @InjectMocks private UpdateProjectHandler handler;

    private String projectId;
    private String ownerId;
    private ProjectEntity projectEntity;
    private UserEntity ownerEntity;
    private Project domainProject;
    private ProjectDetailDto projectDetailDto;

    @BeforeEach
    void setUp() {
        projectId = Ulid.fast().toString();
        ownerId = Ulid.fast().toString();

        ownerEntity = new UserEntity();
        ownerEntity.setId(ownerId);

        projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);
        projectEntity.setTitle("Original Title");
        projectEntity.setDescription("Original Description");
        projectEntity.setSummary("Original Summary");
        projectEntity.setStatus(ProjectStatus.OPEN);
        projectEntity.setOwner(ownerEntity);
        projectEntity.setRequiredSkills(Arrays.asList("Java", "Spring"));

        domainProject = new Project();
        projectDetailDto = mock(ProjectDetailDto.class);
    }

    @Test
    @DisplayName("Should update project with all fields successfully")
    void shouldUpdateProject_withAllFields() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(any())).thenReturn(projectEntity);
        when(projectMapper.entityToDomain(any())).thenReturn(domainProject);
        when(projectDetailDtoMapper.domainToDto(any())).thenReturn(projectDetailDto);
        when(messageService.getMessage("project.updated.success")).thenReturn("Updated");

        UpdateProjectCommand command =
                new UpdateProjectCommand(
                        projectId,
                        ownerId,
                        "New Title",
                        "New Description",
                        "New Summary",
                        10,
                        new String[] {"React", "TypeScript"},
                        "Web Dev",
                        null);

        ApiResponse<ProjectDetailDto> response = handler.handle(command);

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("New Title", projectEntity.getTitle());
        assertEquals("New Description", projectEntity.getDescription());
        assertEquals("New Summary", projectEntity.getSummary());
        assertEquals(10, projectEntity.getMaxTeamSize());
        assertEquals(List.of("React", "TypeScript"), projectEntity.getRequiredSkills());
        assertEquals("Web Dev", projectEntity.getCategory());
        verify(projectRepository).save(projectEntity);
    }

    @Test
    @DisplayName("Should preserve existing values when optional fields are null")
    void shouldPreserveExistingValues_whenFieldsAreNull() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(any())).thenReturn(projectEntity);
        when(projectMapper.entityToDomain(any())).thenReturn(domainProject);
        when(projectDetailDtoMapper.domainToDto(any())).thenReturn(projectDetailDto);
        when(messageService.getMessage("project.updated.success")).thenReturn("Updated");

        UpdateProjectCommand command =
                new UpdateProjectCommand(
                        projectId,
                        ownerId,
                        "New Title",
                        "New Description",
                        null,
                        null,
                        null,
                        null,
                        null);

        ApiResponse<ProjectDetailDto> response = handler.handle(command);

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Original Summary", projectEntity.getSummary());
        assertEquals(Arrays.asList("Java", "Spring"), projectEntity.getRequiredSkills());
    }

    @Test
    @DisplayName("Should clear required skills when empty array is provided")
    void shouldClearSkills_whenEmptyArrayProvided() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(any())).thenReturn(projectEntity);
        when(projectMapper.entityToDomain(any())).thenReturn(domainProject);
        when(projectDetailDtoMapper.domainToDto(any())).thenReturn(projectDetailDto);
        when(messageService.getMessage("project.updated.success")).thenReturn("Updated");

        UpdateProjectCommand command =
                new UpdateProjectCommand(
                        projectId,
                        ownerId,
                        "Title",
                        "Description",
                        null,
                        null,
                        new String[] {},
                        null,
                        null);

        ApiResponse<ProjectDetailDto> response = handler.handle(command);

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertTrue(projectEntity.getRequiredSkills().isEmpty());
    }

    @Test
    @DisplayName("Should return 404 when project not found")
    void shouldReturnNotFound_whenProjectDoesNotExist() {
        String unknownId = Ulid.fast().toString();
        when(projectRepository.findById(unknownId)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("project.not.found"), any(Object[].class)))
                .thenReturn("Not found");

        UpdateProjectCommand command =
                new UpdateProjectCommand(
                        unknownId, ownerId, "Title", "Description", null, null, null, null, null);

        ApiResponse<ProjectDetailDto> response = handler.handle(command);

        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return 403 when user is not the owner")
    void shouldReturnForbidden_whenNotOwner() {
        String otherUserId = Ulid.fast().toString();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(messageService.getMessage("project.owner.mismatch")).thenReturn("Forbidden");

        UpdateProjectCommand command =
                new UpdateProjectCommand(
                        projectId,
                        otherUserId,
                        "Title",
                        "Description",
                        null,
                        null,
                        null,
                        null,
                        null);

        ApiResponse<ProjectDetailDto> response = handler.handle(command);

        assertEquals(ResponseCode.FORBIDDEN, response.getCode());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return 403 when project is COMPLETED")
    void shouldReturnForbidden_whenProjectIsCompleted() {
        projectEntity.setStatus(ProjectStatus.COMPLETED);

        // --- ADDED MOCK BEHAVIOR ---
        domainProject.reconstitute(ProjectStatus.COMPLETED, 0);
        when(projectMapper.entityToDomain(any())).thenReturn(domainProject);
        // ---------------------------

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(messageService.getMessage("project.update.not.allowed")).thenReturn("Not allowed");

        UpdateProjectCommand command =
                new UpdateProjectCommand(
                        projectId, ownerId, "Title", "Description", null, null, null, null, null);

        ApiResponse<ProjectDetailDto> response = handler.handle(command);

        assertEquals(ResponseCode.FORBIDDEN, response.getCode());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return 403 when project is CANCELLED")
    void shouldReturnForbidden_whenProjectIsCancelled() {
        projectEntity.setStatus(ProjectStatus.CANCELLED);

        // --- ADDED MOCK BEHAVIOR ---
        domainProject.reconstitute(ProjectStatus.CANCELLED, 0);
        when(projectMapper.entityToDomain(any())).thenReturn(domainProject);
        // ---------------------------

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(messageService.getMessage("project.update.not.allowed")).thenReturn("Not allowed");

        UpdateProjectCommand command =
                new UpdateProjectCommand(
                        projectId, ownerId, "Title", "Description", null, null, null, null, null);

        ApiResponse<ProjectDetailDto> response = handler.handle(command);

        assertEquals(ResponseCode.FORBIDDEN, response.getCode());
        verify(projectRepository, never()).save(any());
    }
}
