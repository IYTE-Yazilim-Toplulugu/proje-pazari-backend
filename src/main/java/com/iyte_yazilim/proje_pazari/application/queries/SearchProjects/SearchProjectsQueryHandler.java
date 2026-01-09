package com.iyte_yazilim.proje_pazari.application.queries.SearchProjects;

import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.mappers.ProjectDetailDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchProjectsQueryHandler
        implements IRequestHandler<SearchProjectsQuery, ApiResponse<PagedProjectsResult>> {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectDetailDtoMapper projectDtoMapper;
    private final MessageService messageService;

    @Override
    public ApiResponse<PagedProjectsResult> handle(SearchProjectsQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size());

        Page<ProjectEntity> projectEntityPage =
                projectRepository.searchProjects(
                        query.keyword(), query.status(), query.ownerId(), pageable);

        List<ProjectDetailDto> projectDtos =
                projectEntityPage.getContent().stream()
                        .map(projectMapper::entityToDomain)
                        .map(projectDtoMapper::domainToDto)
                        .toList();

        PagedProjectsResult pagedResult =
                new PagedProjectsResult(
                        projectDtos,
                        projectEntityPage.getNumber(),
                        projectEntityPage.getTotalPages(),
                        projectEntityPage.getTotalElements());

        return ApiResponse.success(
                pagedResult, messageService.getMessage("projects.search.success"));
    }
}
