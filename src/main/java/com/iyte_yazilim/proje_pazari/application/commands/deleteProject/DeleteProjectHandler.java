package com.iyte_yazilim.proje_pazari.application.commands.deleteProject;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteProjectHandler
        implements IRequestHandler<DeleteProjectCommand, ApiResponse<Void>> {

    private final ProjectRepository projectRepository;
    private final MessageService messageService;

    @Override
    @Transactional
    public ApiResponse<Void> handle(DeleteProjectCommand command) {

        ProjectEntity project = projectRepository.findById(command.projectId()).orElse(null);
        if (project == null) {
            return ApiResponse.notFound(messageService.getMessage("project.not.found"));
        }

        if (!project.getOwner().getId().equals(command.ownerId())) {
            return ApiResponse.forbidden(messageService.getMessage("error.forbidden"));
        }

        projectRepository.delete(project);

        return ApiResponse.success(null, messageService.getMessage("project.deleted"));
    }
}
