package com.iyte_yazilim.proje_pazari.application.queries.getSystemHealth;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemHealthDTO;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record GetSystemHealthQuery() implements IRequest<ApiResponse<SystemHealthDTO>> {}
