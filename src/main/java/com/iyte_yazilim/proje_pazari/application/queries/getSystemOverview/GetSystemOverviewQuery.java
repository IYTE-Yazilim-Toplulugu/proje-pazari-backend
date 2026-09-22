package com.iyte_yazilim.proje_pazari.application.queries.getSystemOverview;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemOverviewDTO;

public record GetSystemOverviewQuery() implements IRequest<ApiResponse<SystemOverviewDTO>> {}
