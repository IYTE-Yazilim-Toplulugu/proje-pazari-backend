package com.iyte_yazilim.proje_pazari.application.commands.adminFeatureProject;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ProjectNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminFeatureProjectHandler
        implements IRequestHandler<AdminFeatureProjectCommand, ApiResponse<Void>> {

    private final ProjectRepository projectRepository;
    private final MessageService messageService;

    @Override
    @Transactional
    public ApiResponse<Void> handle(AdminFeatureProjectCommand command) {
        ProjectEntity project = projectRepository.findById(command.projectId()).orElse(null);

        if (project == null) {
            throw new ProjectNotFoundException(command.projectId());
        }

        project.setFeatured(command.featured());
        projectRepository.save(project);

        String messageKey =
                command.featured() ? "project.featured.success" : "project.unfeatured.success";
        return ApiResponse.success(null, messageService.getMessage(messageKey));
    }
}
