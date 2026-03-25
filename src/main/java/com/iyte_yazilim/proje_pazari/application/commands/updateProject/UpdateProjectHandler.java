package com.iyte_yazilim.proje_pazari.application.commands.updateProject;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.UpdateProjectCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UpdateProjectHandler
        implements IRequestHandler<UpdateProjectCommand, ApiResponse<UpdateProjectCommandResult>> {

    private final ProjectRepository projectRepository;
    private final MessageService messageService;

    @Override
    @Transactional
    public ApiResponse<UpdateProjectCommandResult> handle(UpdateProjectCommand command) {

        ProjectEntity project = projectRepository.findById(command.projectId()).orElse(null);
        if (project == null) {
            return ApiResponse.notFound(messageService.getMessage("project.not.found"));
        }

        if (!project.getOwner().getId().equals(command.ownerId())) {
            return ApiResponse.forbidden(messageService.getMessage("error.forbidden"));
        }

        if (command.projectName() != null) {
            project.setTitle(command.projectName());
        }
        if (command.description() != null) {
            project.setDescription(command.description());
        }
        if (command.summary() != null) {
            project.setSummary(command.summary());
        }
        if (command.maxTeamSize() != null) {
            project.setMaxTeamSize(command.maxTeamSize());
        }
        if (command.requiredSkills() != null) {
            project.setRequiredSkills(Arrays.asList(command.requiredSkills()));
        }
        if (command.category() != null) {
            project.setCategory(command.category());
        }
        if (command.deadline() != null) {
            project.setDeadline(command.deadline());
        }

        ProjectEntity saved = projectRepository.save(project);

        return ApiResponse.success(
                new UpdateProjectCommandResult(
                        saved.getId(),
                        saved.getTitle(),
                        saved.getDescription(),
                        saved.getStatus().toString()),
                messageService.getMessage("project.updated"));
    }
}
