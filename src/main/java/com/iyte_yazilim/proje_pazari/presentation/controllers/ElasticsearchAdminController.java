package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.services.ElasticsearchSyncService;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/elasticsearch")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ElasticsearchAdminController {

    private final ElasticsearchSyncService syncService;

    @PostMapping("/reindex/projects")
    public ApiResponse<Void> reindexProjects() {
        syncService.reindexAllProjects();
        return ApiResponse.success(null, "Reindexing started");
    }

    @PostMapping("/reindex/users")
    public ApiResponse<Void> reindexUsers() {
        syncService.reindexAllUsers();
        return ApiResponse.success(null, "Reindexing started");
    }

    @DeleteMapping("/indexes")
    public ApiResponse<Void> deleteAllIndexes() {
        syncService.deleteAllIndexes();
        return ApiResponse.success(null, "All indexes deleted");
    }
}
