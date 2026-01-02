package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.elasticsearch.services.ProjectSearchService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search")
public class SearchController {

    private final ProjectSearchService searchService;

    @GetMapping("/projects")
    public ApiResponse<List<ProjectDocument>> searchProjects(
            @RequestParam String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectDocument> results = searchService.advancedSearch(q, status, tags, pageable);

        return ApiResponse.success(results.getContent());
    }

    @GetMapping("/projects/suggest")
    public ApiResponse<List<String>> suggestProjects(@RequestParam String q) {
        // Autocomplete/suggestion endpoint
        List<String> suggestions = searchService.getSuggestions(q);
        return ApiResponse.success(suggestions);
    }

    @GetMapping("/projects/statistics")
    public ApiResponse<Map<String, Long>> getStatistics() {
        Map<String, Long> stats = searchService.getProjectStatistics();
        return ApiResponse.success(stats);
    }
}
