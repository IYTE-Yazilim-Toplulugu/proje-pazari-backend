package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Storage health status information")
public record StorageHealthDTO(
        @Schema(description = "Storage adapter/provider") String provider,
        @Schema(description = "Whether storage backend is reachable") boolean available,
        @Schema(description = "Total storage capacity in bytes when known") Long totalSpaceBytes,
        @Schema(description = "Used storage in bytes when known") Long usedSpaceBytes,
        @Schema(description = "Available buckets/containers") List<String> buckets,
        @Schema(description = "Health check timestamp") LocalDateTime checkedAt) {}
