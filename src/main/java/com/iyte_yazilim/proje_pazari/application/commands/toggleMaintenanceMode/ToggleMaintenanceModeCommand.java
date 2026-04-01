package com.iyte_yazilim.proje_pazari.application.commands.toggleMaintenanceMode;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record ToggleMaintenanceModeCommand(boolean enabled)
        implements IRequest<ApiResponse<Void>> {}
