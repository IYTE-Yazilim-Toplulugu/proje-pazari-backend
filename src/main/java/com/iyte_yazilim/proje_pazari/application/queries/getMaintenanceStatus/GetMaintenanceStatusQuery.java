package com.iyte_yazilim.proje_pazari.application.queries.getMaintenanceStatus;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.Map;

public record GetMaintenanceStatusQuery() implements IRequest<ApiResponse<Map<String, Object>>> {}
