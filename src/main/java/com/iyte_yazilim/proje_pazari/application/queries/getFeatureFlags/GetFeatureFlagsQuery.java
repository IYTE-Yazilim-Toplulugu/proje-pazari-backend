package com.iyte_yazilim.proje_pazari.application.queries.getFeatureFlags;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.FeatureFlagDTO;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.List;

public record GetFeatureFlagsQuery() implements IRequest<ApiResponse<List<FeatureFlagDTO>>> {}
