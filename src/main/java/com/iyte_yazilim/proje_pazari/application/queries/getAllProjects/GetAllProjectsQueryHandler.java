package com.iyte_yazilim.proje_pazari.application.queries.getAllProjects;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.mappers.ProjectDetailDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAllProjectsQueryHandler
        implements IRequestHandler<GetAllProjectsQuery, ApiResponse<PagedProjectsResult>> {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "title", "status", "createdAt", "deadline");

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectDetailDtoMapper projectDtoMapper;
    private final MessageService messageService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedProjectsResult> handle(GetAllProjectsQuery query) {
        // --- 1. Construct Pageable Object ---
        Sort.Direction direction =
                Sort.Direction.DESC.name().equalsIgnoreCase(query.sortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        String sortBy = SORTABLE_FIELDS.contains(query.sortBy()) ? query.sortBy() : "id";

        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by(direction, sortBy));

        // --- 2. Fetch Paged Data from Repository ---
        Page<ProjectEntity> projectEntityPage =
                query.status() == null
                        ? projectRepository.findAllWithApplications(pageable)
                        : projectRepository.findAllByStatusWithApplications(
                                query.status(), pageable);

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
