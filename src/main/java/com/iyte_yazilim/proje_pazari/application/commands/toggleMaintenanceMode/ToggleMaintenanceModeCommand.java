package com.iyte_yazilim.proje_pazari.application.commands.toggleMaintenanceMode;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record ToggleMaintenanceModeCommand(boolean enabled)
        implements IRequest<ApiResponse<Void>> {}
