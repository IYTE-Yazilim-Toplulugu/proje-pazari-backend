package com.iyte_yazilim.proje_pazari.application.queries.getAllProjects;

import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAllProjectsHandler
        implements IRequestHandler<GetAllProjectsQuery, ApiResponse<List<Project>>> {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    public ApiResponse<List<Project>> handle(GetAllProjectsQuery query) {
        List<Project> projects =
                projectRepository.findAll().stream().map(projectMapper::entityToDomain).toList();
        return ApiResponse.success(projects, "Projects retrieved successfully");
    }
}
