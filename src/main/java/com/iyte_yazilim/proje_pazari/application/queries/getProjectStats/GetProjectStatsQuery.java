package com.iyte_yazilim.proje_pazari.application.queries.getProjectStats;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectStatsDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record GetProjectStatsQuery() implements IRequest<ApiResponse<ProjectStatsDTO>> {}
