package com.iyte_yazilim.proje_pazari.application.queries.searchProjects;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.services.ProjectSearchService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
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
        implements IRequestHandler<SearchProjectsQuery, ApiResponse<List<ProjectDocument>>> {

    private final ProjectSearchService searchService;

    @Override
    public ApiResponse<List<ProjectDocument>> handle(SearchProjectsQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size());
        SearchPage<ProjectDocument> results =
                searchService.advancedSearch(query.q(), query.status(), query.tags(), pageable);

        List<ProjectDocument> documents =
                results.getContent().stream().map(SearchHit::getContent).toList();

        return ApiResponse.success(documents, "Projects retrieved successfully");
    }
}
