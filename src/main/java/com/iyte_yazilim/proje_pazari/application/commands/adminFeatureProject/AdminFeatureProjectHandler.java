package com.iyte_yazilim.proje_pazari.application.commands.adminFeatureProject;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminFeatureProjectHandler
        implements IRequestHandler<AdminFeatureProjectCommand, ApiResponse<Void>> {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(AdminFeatureProjectCommand command) {
        ProjectEntity project = projectRepository.findById(command.projectId()).orElse(null);

        if (project == null) {
            return ApiResponse.notFound("Project not found with id: " + command.projectId());
        }

        project.setFeatured(command.featured());
        projectRepository.save(project);

        String message =
                command.featured()
                        ? "Project featured successfully"
                        : "Project unfeatured successfully";
        return ApiResponse.success(null, message);
    }
}
