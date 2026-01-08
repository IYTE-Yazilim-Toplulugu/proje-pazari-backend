package com.iyte_yazilim.proje_pazari.application.dtos;

import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Project detail data transfer object")
public record ProjectDetailDto(
        @Schema(description = "Project ID", example = "01ARZ3NDEKTSV4RRFFQ69G5FAV") String id,
        @Schema(description = "Owner name", example = "Jane Smith") String ownerName,
        @Schema(description = "Owner email", example = "janesmith@iyte.edu.tr") String ownerEmail,
        @Schema(description = "Project title", example = "AI-Powered Chatbot") String title,
        @Schema(description = "Project description", example = "A chatbot that uses AI...")
                String description,
        @Schema(description = "Project summary", example = "Brief summary...") String summary,
        @Schema(description = "Number of applications received", example = "42")
                int applicationCount,
        @Schema(description = "Current project status", example = "IN_PROGRESS")
                ProjectStatus status,
        @Schema(description = "Creation timestamp") LocalDateTime createdAt) {}
