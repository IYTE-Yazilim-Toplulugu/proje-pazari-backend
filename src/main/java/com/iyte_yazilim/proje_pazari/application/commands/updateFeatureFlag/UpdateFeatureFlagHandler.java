package com.iyte_yazilim.proje_pazari.application.commands.updateFeatureFlag;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.FeatureFlagRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.FeatureFlagEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateFeatureFlagHandler
        implements IRequestHandler<UpdateFeatureFlagCommand, ApiResponse<Void>> {

    private final FeatureFlagRepository featureFlagRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(UpdateFeatureFlagCommand command) {
        if (command.flagKey() == null || command.flagKey().isBlank()) {
            return ApiResponse.validationError("Flag key is required");
        }

        Optional<FeatureFlagEntity> existing =
                featureFlagRepository.findByFlagKey(command.flagKey());

        if (existing.isPresent()) {
            FeatureFlagEntity flag = existing.get();
            flag.setEnabled(command.enabled());
            if (command.description() != null) {
                flag.setDescription(command.description());
            }
            featureFlagRepository.save(flag);
        } else {
            FeatureFlagEntity newFlag =
                    FeatureFlagEntity.builder()
                            .flagKey(command.flagKey())
                            .enabled(command.enabled())
                            .description(command.description())
                            .build();
            featureFlagRepository.save(newFlag);
        }

        String action = command.enabled() ? "enabled" : "disabled";
        return ApiResponse.success(
                null, "Feature flag '" + command.flagKey() + "' " + action + " successfully");
    }
}
