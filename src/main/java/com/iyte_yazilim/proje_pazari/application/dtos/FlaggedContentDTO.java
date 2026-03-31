package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Flagged content information for content moderation")
public record FlaggedContentDTO(
        @Schema(description = "Flag ID") String id,
        @Schema(description = "Content type (USER, PROJECT, APPLICATION)") String contentType,
        @Schema(description = "Content ID") String contentId,
        @Schema(description = "Reason for flagging") String reason,
        @Schema(description = "User who reported") String reportedBy,
        @Schema(description = "Flag status (PENDING, APPROVED, REMOVED)") String status,
        @Schema(description = "User who reviewed") String reviewedBy,
        @Schema(description = "Review note") String reviewNote,
        @Schema(description = "Flag creation date") LocalDateTime createdAt,
        @Schema(description = "Review date") LocalDateTime reviewedAt) {}
