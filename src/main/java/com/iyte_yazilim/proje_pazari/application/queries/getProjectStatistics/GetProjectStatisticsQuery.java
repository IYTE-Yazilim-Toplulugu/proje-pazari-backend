package com.iyte_yazilim.proje_pazari.application.queries.getProjectStatistics;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.Map;

public record GetProjectStatisticsQuery()
        implements IRequest<ApiResponse<Map<String, Long>>> {}
