package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteProject;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ProjectNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDeleteProjectHandler
        implements IRequestHandler<AdminDeleteProjectCommand, ApiResponse<Void>> {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(AdminDeleteProjectCommand command) {
        ProjectEntity project =
                projectRepository
                        .findById(command.projectId())
                        .orElseThrow(() -> new ProjectNotFoundException(command.projectId()));

        projectRepository.delete(project);
        return ApiResponse.success(null, "Project deleted successfully");
    }
}
