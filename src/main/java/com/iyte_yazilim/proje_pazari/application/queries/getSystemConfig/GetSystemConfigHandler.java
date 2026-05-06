package com.iyte_yazilim.proje_pazari.application.queries.getSystemConfig;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemConfigDTO;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.SystemConfigRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.SystemConfigEntity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSystemConfigHandler
        implements IRequestHandler<GetSystemConfigQuery, ApiResponse<SystemConfigDTO>> {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    public ApiResponse<SystemConfigDTO> handle(GetSystemConfigQuery query) {
        List<SystemConfigEntity> configs = systemConfigRepository.findAll();

        Map<String, String> configMap = new LinkedHashMap<>();
        for (SystemConfigEntity config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }

        SystemConfigDTO dto = new SystemConfigDTO(configMap);
        return ApiResponse.success(dto, "System configuration retrieved successfully");
    }
}
