package com.iyte_yazilim.proje_pazari.application.commands.updateProject;

import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.mappers.ProjectDetailDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateProjectHandler
        implements IRequestHandler<UpdateProjectCommand, ApiResponse<ProjectDetailDto>> {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectDetailDtoMapper projectDetailDtoMapper;
    private final MessageService messageService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<ProjectDetailDto> handle(UpdateProjectCommand command) {

        // --- 1. Verify Project Exists ---
        ProjectEntity projectEntity = projectRepository.findById(command.projectId()).orElse(null);
        if (projectEntity == null) {
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "project.not.found", new Object[] {command.projectId()}));
        }

        // --- 2. Verify Ownership ---
        if (!projectEntity.getOwner().getId().equals(command.ownerId())) {
            return ApiResponse.forbidden(messageService.getMessage("project.owner.mismatch"));
        }

        // --- 3. Apply Updates ---
        projectEntity.setTitle(command.projectName());
        projectEntity.setDescription(command.description());
        if (command.summary() != null) {
            projectEntity.setSummary(command.summary());
        }
        if (command.maxTeamSize() != null) {
            projectEntity.setMaxTeamSize(command.maxTeamSize());
        }
        if (command.requiredSkills() != null) {
            projectEntity.setRequiredSkills(Arrays.asList(command.requiredSkills()));
        }
        if (command.category() != null) {
            projectEntity.setCategory(command.category());
        }
        if (command.deadline() != null) {
            projectEntity.setDeadline(command.deadline());
        }

        // --- 4. Persist ---
        ProjectEntity saved = projectRepository.save(projectEntity);

        // --- 5. Map to DTO ---
        Project domain = projectMapper.entityToDomain(saved);
        ProjectDetailDto dto = projectDetailDtoMapper.domainToDto(domain);

        // --- 6. Response ---
        return ApiResponse.success(dto, messageService.getMessage("project.updated.success"));
    }
}
