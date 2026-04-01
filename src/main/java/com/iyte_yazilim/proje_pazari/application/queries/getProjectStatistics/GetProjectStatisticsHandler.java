package com.iyte_yazilim.proje_pazari.application.queries.getProjectStatistics;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.services.ProjectSearchService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class GetProjectStatisticsHandler
        implements IRequestHandler<GetProjectStatisticsQuery, ApiResponse<Map<String, Long>>> {

    private final ProjectSearchService searchService;

    @Override
    public ApiResponse<Map<String, Long>> handle(GetProjectStatisticsQuery query) {
        Map<String, Long> stats = searchService.getProjectStatistics();
        return ApiResponse.success(stats, "Statistics retrieved successfully");
    }
}
