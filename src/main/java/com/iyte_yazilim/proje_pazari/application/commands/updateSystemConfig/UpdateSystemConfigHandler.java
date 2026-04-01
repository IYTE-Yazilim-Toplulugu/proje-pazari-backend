package com.iyte_yazilim.proje_pazari.application.commands.updateSystemConfig;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.SystemConfigRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.SystemConfigEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSystemConfigHandler
        implements IRequestHandler<UpdateSystemConfigCommand, ApiResponse<Void>> {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(UpdateSystemConfigCommand command) {
        if (command.configs() == null || command.configs().isEmpty()) {
            return ApiResponse.validationError("Configuration entries cannot be empty");
        }

        for (Map.Entry<String, String> entry : command.configs().entrySet()) {
            Optional<SystemConfigEntity> existing =
                    systemConfigRepository.findByConfigKey(entry.getKey());

            if (existing.isPresent()) {
                SystemConfigEntity config = existing.get();
                config.setConfigValue(entry.getValue());
                config.setUpdatedAt(LocalDateTime.now());
                systemConfigRepository.save(config);
            } else {
                SystemConfigEntity newConfig =
                        SystemConfigEntity.builder()
                                .configKey(entry.getKey())
                                .configValue(entry.getValue())
                                .build();
                systemConfigRepository.save(newConfig);
            }
        }

        return ApiResponse.success(
                null, "System configuration updated (" + command.configs().size() + " entries)");
    }
}
