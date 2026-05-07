package com.iyte_yazilim.proje_pazari.application.commands.updateFeatureFlag;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record UpdateFeatureFlagCommand(String flagKey, boolean enabled, String description)
        implements ICommand<ApiResponse<Void>> {}
