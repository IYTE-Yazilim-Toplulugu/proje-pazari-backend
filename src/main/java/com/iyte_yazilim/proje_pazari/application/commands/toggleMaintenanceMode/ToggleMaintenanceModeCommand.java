package com.iyte_yazilim.proje_pazari.application.commands.toggleMaintenanceMode;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record ToggleMaintenanceModeCommand(boolean enabled)
        implements ICommand<ApiResponse<Void>> {}
