package com.iyte_yazilim.proje_pazari.application.queries.getAnalyticsTrends;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.AnalyticsTrendsDTO;

public record GetAnalyticsTrendsQuery(int days)
        implements IRequest<ApiResponse<AnalyticsTrendsDTO>> {}
