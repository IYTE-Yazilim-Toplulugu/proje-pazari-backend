package com.iyte_yazilim.proje_pazari.application.queries.getAllProjects;

import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.mappers.ProjectDetailDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAllProjectsQueryHandler
        implements IRequestHandler<GetAllProjectsQuery, ApiResponse<PagedProjectsResult>> {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectDetailDtoMapper projectDtoMapper;
    private final MessageService messageService;

    @Override
    public ApiResponse<PagedProjectsResult> handle(GetAllProjectsQuery query) {
        // --- 1. Construct Pageable Object ---
        Sort.Direction direction =
                Sort.Direction.DESC.name().equalsIgnoreCase(query.sortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        String sortBy =
                (query.sortBy() != null && !query.sortBy().isEmpty()) ? query.sortBy() : "id";

        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by(direction, sortBy));

        // --- 2. Fetch Paged Data from Repository ---
        Page<ProjectEntity> projectEntityPage = projectRepository.findAll(pageable);

        // --- 3. Map Entity -> Domain -> DTO ---
        List<ProjectDetailDto> projectDtos =
                projectEntityPage.getContent().stream()
                        .map(projectMapper::entityToDomain)
                        .map(projectDtoMapper::domainToDto)
                        .toList();

        // --- 4. Construct the Paged Result ---
        PagedProjectsResult pagedResult =
                new PagedProjectsResult(
                        projectDtos,
                        projectEntityPage.getNumber(),
                        projectEntityPage.getTotalPages(),
                        projectEntityPage.getTotalElements());

        // --- 5. Return Response ---
        return ApiResponse.success(
                pagedResult, messageService.getMessage("projects.listed.success"));
    }
}
