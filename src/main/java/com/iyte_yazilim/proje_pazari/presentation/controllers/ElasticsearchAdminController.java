package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.deleteAllIndexes.DeleteAllIndexesCommand;
import com.iyte_yazilim.proje_pazari.application.commands.reindexProjects.ReindexProjectsCommand;
import com.iyte_yazilim.proje_pazari.application.commands.reindexUsers.ReindexUsersCommand;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/elasticsearch")
@PreAuthorize("hasRole('ADMIN')")
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
@Tag(
        name = "Elasticsearch Admin",
        description =
                "Admin endpoints for Elasticsearch index management. "
                        + "Allows reindexing and deleting search indexes. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
public class ElasticsearchAdminController extends BaseController {

    @PostMapping("/reindex/projects")
    @Operation(
            summary = "Reindex all projects",
            description =
                    "Rebuilds the Elasticsearch project index from the database. "
                            + "Use after data migrations or index corruption.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Projects reindexed successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "500",
                        description = "Reindex failed")
            })
    public ResponseEntity<ApiResponse<Void>> reindexProjects() {
        return send(new ReindexProjectsCommand());
    }

    @PostMapping("/reindex/users")
    @Operation(
            summary = "Reindex all users",
            description =
                    "Rebuilds the Elasticsearch user index from the database. "
                            + "Use after data migrations or index corruption.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Users reindexed successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "500",
                        description = "Reindex failed")
            })
    public ResponseEntity<ApiResponse<Void>> reindexUsers() {
        return send(new ReindexUsersCommand());
    }

    @DeleteMapping("/indexes")
    @Operation(
            summary = "Delete all Elasticsearch indexes",
            description =
                    "Deletes all Elasticsearch indexes. Use with caution — "
                            + "a reindex operation will be required afterwards to restore search functionality.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "All indexes deleted successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "500",
                        description = "Delete operation failed")
            })
    public ResponseEntity<ApiResponse<Void>> deleteAllIndexes() {
        return send(new DeleteAllIndexesCommand());
    }
}
