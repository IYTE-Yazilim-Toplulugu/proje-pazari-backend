package com.iyte_yazilim.proje_pazari.application.queries.getUserStats;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.UserStatsDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record GetUserStatsQuery() implements IRequest<ApiResponse<UserStatsDTO>> {}
