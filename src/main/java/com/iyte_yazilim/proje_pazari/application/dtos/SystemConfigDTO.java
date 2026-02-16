package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "System configuration")
public record SystemConfigDTO(
        @Schema(description = "Configuration entries") Map<String, String> configs) {}
