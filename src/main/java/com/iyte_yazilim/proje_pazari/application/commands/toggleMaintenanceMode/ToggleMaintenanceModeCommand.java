package com.iyte_yazilim.proje_pazari.application.commands.toggleMaintenanceMode;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record ToggleMaintenanceModeCommand(boolean enabled)
        implements ICommand<ApiResponse<Void>> {}
