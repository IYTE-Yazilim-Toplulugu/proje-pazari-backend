package com.iyte_yazilim.proje_pazari.application.queries.getFeatureFlags;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.FeatureFlagDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.FeatureFlagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetFeatureFlagsHandler
        implements IRequestHandler<GetFeatureFlagsQuery, ApiResponse<List<FeatureFlagDTO>>> {

    private final FeatureFlagRepository featureFlagRepository;

    @Override
    public ApiResponse<List<FeatureFlagDTO>> handle(GetFeatureFlagsQuery query) {
        List<FeatureFlagDTO> flags =
                featureFlagRepository.findAll().stream()
                        .map(
                                entity ->
                                        new FeatureFlagDTO(
                                                entity.getId(),
                                                entity.getFlagKey(),
                                                entity.isEnabled(),
                                                entity.getDescription(),
                                                entity.getCreatedAt(),
                                                entity.getUpdatedAt()))
                        .toList();

        return ApiResponse.success(flags, "Feature flags retrieved successfully");
    }
}
