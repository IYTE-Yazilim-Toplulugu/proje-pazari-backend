package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Plain-text message in a project application thread")
public record ApplicationMessageDto(
        @Schema(description = "Message ULID") String id,
        @Schema(description = "Parent application ULID") String applicationId,
        @Schema(description = "Authenticated sender ULID") String senderId,
        @Schema(description = "Plain-text body", maxLength = 2000) String body,
        @Schema(description = "Creation timestamp") LocalDateTime createdAt) {}
