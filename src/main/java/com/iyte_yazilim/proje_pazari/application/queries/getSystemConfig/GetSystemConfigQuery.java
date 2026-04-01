package com.iyte_yazilim.proje_pazari.application.queries.getSystemConfig;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemConfigDTO;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record GetSystemConfigQuery() implements IRequest<ApiResponse<SystemConfigDTO>> {}
