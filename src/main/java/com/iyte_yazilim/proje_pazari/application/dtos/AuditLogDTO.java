package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Audit log entry for admin operations tracking")
public record AuditLogDTO(
        @Schema(description = "Audit log ID") String id,
        @Schema(description = "Action performed") String action,
        @Schema(description = "Entity type affected") String entityType,
        @Schema(description = "Entity ID affected") String entityId,
        @Schema(description = "User who performed the action") String performedBy,
        @Schema(description = "IP address") String ipAddress,
        @Schema(description = "Timestamp of the action") LocalDateTime timestamp,
        @Schema(description = "Additional details (JSON)") String details,
        @Schema(description = "Action status (SUCCESS/FAILED)") String status) {}
