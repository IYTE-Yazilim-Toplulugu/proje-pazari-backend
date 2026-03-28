package com.iyte_yazilim.proje_pazari.application.commands.reindexUsers;

import com.iyte_yazilim.proje_pazari.application.services.ElasticsearchSyncService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
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
public class ReindexUsersHandler
        implements IRequestHandler<ReindexUsersCommand, ApiResponse<Void>> {

    private final ElasticsearchSyncService syncService;

    @Override
    public ApiResponse<Void> handle(ReindexUsersCommand command) {
        syncService.reindexAllUsers();
        return ApiResponse.success(null, "Reindexing started");
    }
}
