package com.iyte_yazilim.proje_pazari.application.queries.getApplicationStats;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationStatsDTO;

public record GetApplicationStatsQuery() implements IRequest<ApiResponse<ApplicationStatsDTO>> {}
