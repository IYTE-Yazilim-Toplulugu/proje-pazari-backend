package com.iyte_yazilim.proje_pazari.application.queries.getProjectById;

import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProjectByIdHandler
        implements IRequestHandler<GetProjectByIdQuery, ApiResponse<Project>> {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    public ApiResponse<Project> handle(GetProjectByIdQuery query) {
        return projectRepository
                .findById(query.projectId())
                .map(projectMapper::entityToDomain)
                .map(project -> ApiResponse.success(project, "Project retrieved successfully"))
                .orElseGet(() -> ApiResponse.notFound("Project not found"));
    }
}
