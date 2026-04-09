package com.iyte_yazilim.proje_pazari.application.queries.getMaintenanceStatus;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.SystemConfigRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.SystemConfigEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMaintenanceStatusHandler
        implements IRequestHandler<GetMaintenanceStatusQuery, ApiResponse<Map<String, Object>>> {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    public ApiResponse<Map<String, Object>> handle(GetMaintenanceStatusQuery query) {
        Optional<SystemConfigEntity> config =
                systemConfigRepository.findByConfigKey("maintenanceMode");

        Map<String, Object> status = new HashMap<>();
        boolean enabled =
                config.isPresent() && "true".equalsIgnoreCase(config.get().getConfigValue());
        status.put("enabled", enabled);
        status.put("updatedAt", config.map(SystemConfigEntity::getUpdatedAt).orElse(null));

        return ApiResponse.success(status, "Maintenance status retrieved");
    }
}
