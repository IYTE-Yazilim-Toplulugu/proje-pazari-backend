package com.iyte_yazilim.proje_pazari.application.commands.updateSystemConfig;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import java.util.Map;

public record UpdateSystemConfigCommand(Map<String, String> configs)
        implements ICommand<ApiResponse<Void>> {}
