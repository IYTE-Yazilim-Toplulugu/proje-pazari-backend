package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.queries.getProjectStatistics.GetProjectStatisticsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.searchProjects.SearchProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.suggestProjects.SuggestProjectsQuery;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
@Tag(
        name = "Search",
        description = "Elasticsearch-powered search endpoints for projects. Public access.")
public class SearchController {

    private final ProjectSearchService searchService;

    @GetMapping("/projects")
    public ApiResponse<List<ProjectDocument>> searchProjects(
            @RequestParam @NotBlank @Size(min = 2, max = 100) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return send(new SearchProjectsQuery(q, status, tags, page, size));
    }

    @GetMapping("/projects/suggest")
    public ApiResponse<List<String>> suggestProjects(
            @RequestParam @NotBlank @Size(min = 1, max = 100) String q) {
        List<String> suggestions = searchService.getSuggestions(q);
        return ApiResponse.success(suggestions, "Suggestions retrieved successfully");
    }

    @GetMapping("/projects/statistics")
    public ApiResponse<Map<String, Long>> getStatistics() {
        Map<String, Long> stats = searchService.getProjectStatistics();
        return ApiResponse.success(stats, "Statistics retrieved successfully");
    }
}
