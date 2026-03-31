package com.iyte_yazilim.proje_pazari.application.commands.updateSystemConfig;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.Map;

public record UpdateSystemConfigCommand(Map<String, String> configs)
        implements IRequest<ApiResponse<Void>> {}
