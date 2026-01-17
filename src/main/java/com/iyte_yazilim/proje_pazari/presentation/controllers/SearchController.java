package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.services.ProjectSearchService;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
@Validated
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class SearchController {

    private final ProjectSearchService searchService;

    @GetMapping("/projects")
    public ApiResponse<List<ProjectDocument>> searchProjects(
            @RequestParam @NotBlank @Size(min = 2, max = 100) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        SearchPage<ProjectDocument> results =
                searchService.advancedSearch(q, status, tags, pageable);

        List<ProjectDocument> documents =
                results.getContent().stream().map(SearchHit::getContent).toList();

        return ApiResponse.success(documents, "Projects retrieved successfully");
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
