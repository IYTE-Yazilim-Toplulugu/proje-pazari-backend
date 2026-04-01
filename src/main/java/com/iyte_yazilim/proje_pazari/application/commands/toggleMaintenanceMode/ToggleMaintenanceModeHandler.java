package com.iyte_yazilim.proje_pazari.application.commands.toggleMaintenanceMode;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.SystemConfigRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.SystemConfigEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ToggleMaintenanceModeHandler
        implements IRequestHandler<ToggleMaintenanceModeCommand, ApiResponse<Void>> {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(ToggleMaintenanceModeCommand command) {
        Optional<SystemConfigEntity> existing =
                systemConfigRepository.findByConfigKey("maintenanceMode");

        if (existing.isPresent()) {
            SystemConfigEntity config = existing.get();
            config.setConfigValue(String.valueOf(command.enabled()));
            config.setUpdatedAt(LocalDateTime.now());
            systemConfigRepository.save(config);
        } else {
            SystemConfigEntity newConfig =
                    SystemConfigEntity.builder()
                            .configKey("maintenanceMode")
                            .configValue(String.valueOf(command.enabled()))
                            .description("Enable/disable maintenance mode")
                            .build();
            systemConfigRepository.save(newConfig);
        }

        String status = command.enabled() ? "enabled" : "disabled";
        return ApiResponse.success(null, "Maintenance mode " + status + " successfully");
    }
}
