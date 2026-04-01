package com.iyte_yazilim.proje_pazari.application.queries.getSystemOverview;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemOverviewDTO;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record GetSystemOverviewQuery() implements IRequest<ApiResponse<SystemOverviewDTO>> {}
