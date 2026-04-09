package com.iyte_yazilim.proje_pazari.application.commands.deleteAllIndexes;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.ElasticsearchSyncService;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DeleteAllIndexesHandler
        implements IRequestHandler<DeleteAllIndexesCommand, ApiResponse<Void>> {

    private final ElasticsearchSyncService syncService;

    @Override
    public ApiResponse<Void> handle(DeleteAllIndexesCommand command) {
        syncService.deleteAllIndexes();
        return ApiResponse.success(null, "All indexes deleted");
    }
}
