package com.iyte_yazilim.proje_pazari.application.queries.searchProjects;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.services.ProjectSearchService;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class SearchProjectsHandler
        implements IRequestHandler<SearchProjectsQuery, ApiResponse<PagedProjectsResult>> {

    private final ProjectSearchService searchService;

    @Override
    public ApiResponse<PagedProjectsResult> handle(SearchProjectsQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size());
        SearchPage<ProjectDocument> results =
                searchService.advancedSearch(query.q(), query.status(), pageable);

        List<ProjectDetailDto> projects =
                results.getContent().stream()
                        .map(SearchHit::getContent)
                        .map(this::toProjectDetailDto)
                        .toList();

        PagedProjectsResult response =
                new PagedProjectsResult(
                        projects,
                        results.getNumber(),
                        results.getTotalPages(),
                        results.getTotalElements());

        return ApiResponse.success(response, "Projects retrieved successfully");
    }

    private ProjectDetailDto toProjectDetailDto(ProjectDocument document) {
        return new ProjectDetailDto(
                document.getId(),
                document.getOwnerId(),
                document.getOwnerName(),
                document.getOwnerEmail(),
                document.getTitle(),
                document.getDescription(),
                document.getSummary(),
                document.getApplicationCount(),
                ProjectStatus.fromString(document.getStatus()),
                document.getMaxTeamSize(),
                document.getRequiredSkills() != null ? document.getRequiredSkills() : List.of(),
                document.getCategory(),
                document.getDeadline(),
                document.getCreatedAt());
    }
}
