package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.mappers.CreateProjectMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.metrics.BusinessMetricsService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ResponseCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CreateProjectHandlerTest {

    @Mock private ProjectRepository projectRepository;

    @Mock private UserRepository userRepository;

    @Mock private CreateProjectMapper createProjectMapper;

    @Mock private ProjectMapper projectMapper;

    @Mock private UserMapper userMapper;

    @Mock private MessageService messageService;

    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @Mock private BusinessMetricsService metricsService;

    @InjectMocks private CreateProjectHandler handler;

    @BeforeEach
    void setUp() {
        lenient()
                .when(messageService.getMessage("project.created.success"))
                .thenReturn("Project created successfully");
        lenient()
                .when(messageService.getMessage(eq("project.owner.not.found"), any(Object[].class)))
                .thenAnswer(
                        invocation -> {
                            Object[] args = invocation.getArgument(1);
                            return "Owner with ID " + args[0] + " not found";
                        });
    }

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
                        new String[] {"java", "spring"},
                        5,
                        new String[] {"Java", "Spring Boot"},
                        "Software Development",
                        LocalDateTime.now().plusDays(30));

        UserEntity ownerEntity = new UserEntity();
        ownerEntity.setId(ownerId);
        ownerEntity.setEmail("owner@std.iyte.edu.tr");

        User ownerDomain = new User();
        ownerDomain.setId(ownerUlid);
        ownerDomain.setEmail("owner@std.iyte.edu.tr");
        ownerDomain.setFirstName("Owner");

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
                        new String[] {},
                        5,
                        0,
                        new String[] {"Java", "Spring Boot"},
                        "Software Development",
                        LocalDateTime.now().plusDays(30));

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
        verify(metricsService).incrementProjectCreationSuccess();
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
                        new String[] {},
                        5,
                        new String[] {},
                        null,
                        null);

        when(userRepository.findById(nonExistentOwnerId)).thenReturn(Optional.empty());

        // When
        ApiResponse<CreateProjectCommandResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        assertTrue(response.getMessage().contains("Owner with ID"));
        assertTrue(response.getMessage().contains("not found"));
        verify(projectRepository, never()).save(any());
        verify(metricsService).incrementProjectCreationFailure();
    }
}
