package com.iyte_yazilim.proje_pazari.application.commands.reindexProjects;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.ElasticsearchSyncService;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ReindexProjectsHandler
        implements IRequestHandler<ReindexProjectsCommand, ApiResponse<Void>> {

    private final ElasticsearchSyncService syncService;

    @Override
    public ApiResponse<Void> handle(ReindexProjectsCommand command) {
        syncService.reindexAllProjects();
        return ApiResponse.success(null, "Reindexing started");
    }
}
