package com.iyte_yazilim.proje_pazari.application.queries.getProjectStats;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectStatsDTO;

public record GetProjectStatsQuery() implements IRequest<ApiResponse<ProjectStatsDTO>> {}
