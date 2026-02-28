package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.deleteAllIndexes.DeleteAllIndexesCommand;
import com.iyte_yazilim.proje_pazari.application.commands.reindexProjects.ReindexProjectsCommand;
import com.iyte_yazilim.proje_pazari.application.commands.reindexUsers.ReindexUsersCommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
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
public class ElasticsearchAdminController extends BaseController {

    @PostMapping("/reindex/projects")
    public ResponseEntity<ApiResponse<Void>> reindexProjects() {
        return send(new ReindexProjectsCommand());
    }

    @PostMapping("/reindex/users")
    public ResponseEntity<ApiResponse<Void>> reindexUsers() {
        return send(new ReindexUsersCommand());
    }

    @DeleteMapping("/indexes")
    public ResponseEntity<ApiResponse<Void>> deleteAllIndexes() {
        return send(new DeleteAllIndexesCommand());
    }
}
