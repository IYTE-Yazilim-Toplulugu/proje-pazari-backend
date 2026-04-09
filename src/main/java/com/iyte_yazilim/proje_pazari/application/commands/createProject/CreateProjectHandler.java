package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.mappers.CreateProjectMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectCreatedEvent;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.metrics.BusinessMetricsService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class CreateProjectHandler
        implements IRequestHandler<CreateProjectCommand, ApiResponse<CreateProjectCommandResult>> {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CreateProjectMapper createProjectMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BusinessMetricsService metricsService;

    /**
     * Handles project creation command.
     *
     * <p>Creates a new project in DRAFT status with the specified owner.
     *
     * @param command the project creation command
     * @return API response with project result or error message
     */
    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<CreateProjectCommandResult> handle(CreateProjectCommand command) {

        // --- 1. Verify Owner Exists ---
        UserEntity ownerEntity = userRepository.findById(command.ownerId()).orElse(null);
        if (ownerEntity == null) {
            metricsService.incrementProjectCreationFailure();
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "project.owner.not.found", new Object[] {command.ownerId()}));
        }

        // --- 2. Mapping (Command -> Domain Entity) ---
        Project domainProject = createProjectMapper.commandToDomain(command);

        // --- 4. Set Owner (map from persistence to domain) ---
        User owner = userMapper.entityToDomain(ownerEntity);
        domainProject.setOwner(owner);

        // --- 5. Mapping (Domain -> Persistence) ---
        ProjectEntity persistenceProject = projectMapper.domainToEntity(domainProject);

        // --- 6. Persistence ---
        ProjectEntity savedProject = projectRepository.save(persistenceProject);

        // --- 7. Mapping (Persistence -> Domain) ---
        Project savedDomainProject = projectMapper.entityToDomain(savedProject);

        // --- 8. Result Mapping (Domain Entity -> Result DTO) ---
        var result = createProjectMapper.domainToResult(savedDomainProject);

        // --- 9. Publish event for side effects (email sending handled by event listener) ---
        applicationEventPublisher.publishEvent(
                new ProjectCreatedEvent(
                        savedDomainProject.getId().toString(),
                        savedDomainProject.getTitle(),
                        savedDomainProject.getOwner().getId().toString(),
                        savedDomainProject.getOwner().getEmail(),
                        savedDomainProject.getOwner().getFirstName(),
                        LocalDateTime.now()));

        // --- 10. Response ---
        metricsService.incrementProjectCreationSuccess();
        return ApiResponse.created(result, messageService.getMessage("project.created.success"));
    }
}
