package com.iyte_yazilim.proje_pazari.application.queries.getSystemHealth;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemHealthDTO;

public record GetSystemHealthQuery() implements IRequest<ApiResponse<SystemHealthDTO>> {}
