package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.queries.getProjectStatistics.GetProjectStatisticsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.searchProjects.SearchProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.suggestProjects.SuggestProjectsQuery;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Search", description = "Elasticsearch-powered search endpoints for projects")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("isAuthenticated()")
public class SearchController extends BaseController {

    @GetMapping("/projects")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<ProjectDocument>>> searchProjects(
            @RequestParam String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return send(new SearchProjectsQuery(q, status, tags, page, size));
    }

    @GetMapping("/projects/suggest")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<List<String>>> suggestProjects(@RequestParam String q) {
        return send(new SuggestProjectsQuery(q));
    }

    @GetMapping("/projects/statistics")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatistics() {
        return send(new GetProjectStatisticsQuery());
    }
}
