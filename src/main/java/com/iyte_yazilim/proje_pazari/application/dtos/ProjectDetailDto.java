package com.iyte_yazilim.proje_pazari.application.dtos;

import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Project detail data transfer object")
public record ProjectDetailDto(
        @Schema(description = "Owner name", example = "Jane Smith")
        String ownerName,
        @Schema(description = "Owner email", example = "janesmith@iyte.edu.tr")
        String ownerEmail,
        @Schema(description = "Project title", example = "AI-Powered Chatbot")
        String title,
        @Schema(description = "Project description", example = "A chatbot that uses AI to provide customer support.")
        String description,
        @Schema(description = "Number of applications received", example = "42")
        int applicationCount,
        @Schema(description = "Current project status", example = "IN_PROGRESS")
        ProjectStatus status) {
}
