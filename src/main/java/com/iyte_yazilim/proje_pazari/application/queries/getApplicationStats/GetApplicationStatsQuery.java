package com.iyte_yazilim.proje_pazari.application.queries.getApplicationStats;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationStatsDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record GetApplicationStatsQuery() implements IRequest<ApiResponse<ApplicationStatsDTO>> {}
