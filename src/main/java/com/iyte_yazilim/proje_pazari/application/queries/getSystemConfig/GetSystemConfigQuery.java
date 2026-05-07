package com.iyte_yazilim.proje_pazari.application.queries.getSystemConfig;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemConfigDTO;

public record GetSystemConfigQuery() implements IRequest<ApiResponse<SystemConfigDTO>> {}
