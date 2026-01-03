package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.mappers.CreateProjectMapper;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateProjectHandlerTest {

    @Mock private ProjectRepository projectRepository;

    @Mock private UserRepository userRepository;

    @Mock private IValidator<CreateProjectCommand> validator;

    @Mock private CreateProjectMapper createProjectMapper;

    @Mock private ProjectMapper projectMapper;

    @Mock private UserMapper userMapper;

    @InjectMocks private CreateProjectHandler handler;

    @Test
    @DisplayName("Should create project successfully when valid command provided")
    void shouldCreateProject_whenValidCommand() {
        // Given
        Ulid ownerUlid = Ulid.fast();
        Ulid projectUlid = Ulid.fast();
        String ownerId = ownerUlid.toString();
        String projectId = projectUlid.toString();

        CreateProjectCommand command =
                new CreateProjectCommand(
                        "Test Project",
                        "This is a test project description",
                        ownerId,
                        new String[] {},
                        new String[] {"java", "spring"});

        UserEntity ownerEntity = new UserEntity();
        ownerEntity.setId(ownerId);
        ownerEntity.setEmail("owner@std.iyte.edu.tr");

        User ownerDomain = new User();
        ownerDomain.setId(ownerUlid);
        ownerDomain.setEmail("owner@std.iyte.edu.tr");

        Project domainProject = new Project();
        domainProject.setTitle(command.projectName());
        domainProject.setDescription(command.description());

        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);
        projectEntity.setTitle(command.projectName());
        projectEntity.setOwner(ownerEntity);

        Project savedDomainProject = new Project();
        savedDomainProject.setId(projectUlid);
        savedDomainProject.setTitle(command.projectName());
        savedDomainProject.setOwner(ownerDomain);

        CreateProjectCommandResult expectedResult =
                new CreateProjectCommandResult(
                        projectId,
                        command.projectName(),
                        command.description(),
                        ownerId,
                        new String[] {},
                        new String[] {});

        when(validator.validate(command)).thenReturn(null);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(ownerEntity));
        when(createProjectMapper.commandToDomain(command)).thenReturn(domainProject);
        when(userMapper.entityToDomain(ownerEntity)).thenReturn(ownerDomain);
        when(projectMapper.domainToEntity(domainProject)).thenReturn(projectEntity);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectMapper.entityToDomain(projectEntity)).thenReturn(savedDomainProject);
        when(createProjectMapper.domainToResult(savedDomainProject)).thenReturn(expectedResult);

        // When
        ApiResponse<CreateProjectCommandResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.CREATED, response.getCode());
        assertEquals("Project created successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(expectedResult.projectId(), response.getData().projectId());
        assertEquals(expectedResult.projectName(), response.getData().projectName());
        assertEquals(ownerId, response.getData().ownerId());
        verify(projectRepository).save(projectEntity);
    }

    @Test
    @DisplayName("Should return validation error when command is invalid")
    void shouldReturnError_whenValidationFails() {
        // Given
        CreateProjectCommand command = new CreateProjectCommand("", "Short", "", null, null);

        when(validator.validate(command))
                .thenReturn(
                        new String[] {
                            "Project name is required", "Description must be at least 10 characters"
                        });

        // When
        ApiResponse<CreateProjectCommandResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertTrue(response.getMessage().contains("Project name is required"));
        assertTrue(response.getMessage().contains("Description must be at least 10 characters"));
        verify(userRepository, never()).findById(any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return not found error when owner does not exist")
    void shouldReturnError_whenOwnerNotFound() {
        // Given
        String nonExistentOwnerId = Ulid.fast().toString();
        CreateProjectCommand command =
                new CreateProjectCommand(
                        "Test Project",
                        "This is a test project description",
                        nonExistentOwnerId,
                        new String[] {},
                        new String[] {});

        when(validator.validate(command)).thenReturn(null);
        when(userRepository.findById(nonExistentOwnerId)).thenReturn(Optional.empty());

        // When
        ApiResponse<CreateProjectCommandResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        assertTrue(response.getMessage().contains("Owner with ID"));
        assertTrue(response.getMessage().contains("not found"));
        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should proceed when validator returns empty array")
    void shouldProceed_whenValidatorReturnsEmptyArray() {
        // Given
        Ulid ownerUlid = Ulid.fast();
        Ulid projectUlid = Ulid.fast();
        String ownerId = ownerUlid.toString();
        String projectId = projectUlid.toString();

        CreateProjectCommand command =
                new CreateProjectCommand(
                        "Valid Project",
                        "This is a valid project description",
                        ownerId,
                        new String[] {},
                        new String[] {});

        UserEntity ownerEntity = new UserEntity();
        ownerEntity.setId(ownerId);

        User ownerDomain = new User();
        ownerDomain.setId(ownerUlid);

        Project domainProject = new Project();
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);

        Project savedDomainProject = new Project();
        savedDomainProject.setId(projectUlid);

        CreateProjectCommandResult expectedResult =
                new CreateProjectCommandResult(
                        projectId,
                        command.projectName(),
                        command.description(),
                        ownerId,
                        new String[] {},
                        new String[] {});

        when(validator.validate(command)).thenReturn(new String[] {});
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(ownerEntity));
        when(createProjectMapper.commandToDomain(command)).thenReturn(domainProject);
        when(userMapper.entityToDomain(ownerEntity)).thenReturn(ownerDomain);
        when(projectMapper.domainToEntity(domainProject)).thenReturn(projectEntity);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectMapper.entityToDomain(projectEntity)).thenReturn(savedDomainProject);
        when(createProjectMapper.domainToResult(savedDomainProject)).thenReturn(expectedResult);

        // When
        ApiResponse<CreateProjectCommandResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.CREATED, response.getCode());
        verify(projectRepository).save(projectEntity);
    }
}
