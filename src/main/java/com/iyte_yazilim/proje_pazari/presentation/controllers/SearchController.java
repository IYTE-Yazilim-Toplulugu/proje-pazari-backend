package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.application.queries.getProjectStatistics.GetProjectStatisticsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.searchProjects.SearchProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.suggestProjects.SuggestProjectsQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
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
@RequiredArgsConstructor
public class SearchController extends BaseController {

    @GetMapping("/projects")
    @Operation(
            summary = "Search projects",
            description =
                    "Full-text search across projects using Elasticsearch. "
                            + "Supports filtering by status and tags with pagination.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Search results returned successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid search parameters")
            })
    public ResponseEntity<ApiResponse<PagedProjectsResult>> searchProjects(
            @Parameter(
                            description = "Search query string",
                            required = true,
                            example = "machine learning")
                    @RequestParam
                    @NotBlank
                    @Size(min = 2, max = 100)
                    String q,
            @Parameter(
                            description =
                                    "Filter by project status (e.g. OPEN, DRAFT, IN_PROGRESS)",
                            example = "OPEN")
                    @RequestParam(required = false)
                    String status,
            @Parameter(description = "Filter by tags (multiple allowed)", example = "python")
                    @RequestParam(required = false)
                    List<String> tags,
            @Parameter(description = "Page number (zero-based)", example = "0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10")
                    int size) {
        return send(new SearchProjectsQuery(q, status, tags, page, size));
    }

    @GetMapping("/projects/suggest")
    @Operation(
            summary = "Suggest projects",
            description =
                    "Returns autocomplete suggestions for project names based on the query prefix.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Suggestions returned successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid query parameter")
            })
    public ResponseEntity<ApiResponse<List<String>>> suggestProjects(
            @Parameter(
                            description = "Search query prefix for autocomplete",
                            required = true,
                            example = "mach")
                    @RequestParam
                    @NotBlank
                    @Size(min = 1, max = 100)
                    String q) {
        return send(new SuggestProjectsQuery(q));
    }

    @GetMapping("/projects/statistics")
    @Operation(
            summary = "Get project statistics",
            description =
                    "Returns aggregated statistics about projects (e.g. count by status, by category).")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Statistics retrieved successfully")
            })
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatistics() {
        return send(new GetProjectStatisticsQuery());
    }
}
