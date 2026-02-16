package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "System health status information")
public record SystemHealthDTO(
        @Schema(description = "Overall system status") String status,
        @Schema(description = "Individual service statuses") Map<String, String> services,
        @Schema(description = "JVM memory usage in MB") Map<String, Long> memory,
        @Schema(description = "System uptime") String uptime) {}
