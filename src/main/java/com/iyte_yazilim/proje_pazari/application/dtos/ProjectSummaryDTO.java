package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Project summary for user profile")
public record ProjectSummaryDTO(
        @Schema(description = "Project ID")
        String id,

        @Schema(description = "Project title")
        String title,

        @Schema(description = "Project description")
        String description,

        @Schema(description = "Project status")
        String status,

        @Schema(description = "Project creation date")
        java.time.LocalDateTime createdAt
) {
}
