package com.iyte_yazilim.proje_pazari.application.commands.updateFeatureFlag;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record UpdateFeatureFlagCommand(String flagKey, boolean enabled, String description)
        implements IRequest<ApiResponse<Void>> {}
