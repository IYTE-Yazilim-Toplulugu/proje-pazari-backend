package com.iyte_yazilim.proje_pazari.application.queries.getProject;

import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.mappers.ProjectDetailDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetProjectQueryHandler
        implements IRequestHandler<GetProjectQuery, ApiResponse<ProjectDetailDto>> {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectDetailDtoMapper projectDetailDtoMapper;
    private final MessageService messageService;

    @Override
    public ApiResponse<ProjectDetailDto> handle(GetProjectQuery query) {

        // --- 1. Find project by ID ---
        ProjectEntity projectEntity = projectRepository.findById(query.projectId()).orElse(null);
        if (projectEntity == null) {
            return ApiResponse.notFound(messageService.getMessage("project.not.found"));
        }

        // --- 2. Map to domain ---
        Project project = projectMapper.entityToDomain(projectEntity);

        // --- 3. Map to DTO ---
        ProjectDetailDto projectDetailDto = projectDetailDtoMapper.domainToDto(project);

        // --- 4. Response ---
        return ApiResponse.success(
                projectDetailDto, messageService.getMessage("project.retrieved.success"));
    }
}
