package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import com.iyte_yazilim.proje_pazari.application.mappers.CreateProjectMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class CreateProjectHandler
        implements IRequestHandler<CreateProjectCommand, ApiResponse<CreateProjectCommandResult>> {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final IValidator<CreateProjectCommand> validator;
    private final CreateProjectMapper createProjectMapper;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final MessageService messageService; // EKLENMELI

    @Override
    public ApiResponse<CreateProjectCommandResult> handle(CreateProjectCommand command) {

        // --- 1. Validation ---
        var errors = validator.validate(command);
        if (errors != null && errors.length > 0) {
            String errorMessage = String.join(", ", errors);
            return ApiResponse.badRequest(errorMessage);
        }

        // --- 2. Verify Owner Exists ---
        UserEntity ownerEntity = userRepository.findById(command.ownerId()).orElse(null);
        if (ownerEntity == null) {
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "project.owner.not.found", new Object[] {command.ownerId()}));
        }

        // --- 3. Mapping (Command -> Domain Entity) ---
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

        // --- 9. Response ---
        return ApiResponse.created(result, messageService.getMessage("project.created.success"));
    }
}
