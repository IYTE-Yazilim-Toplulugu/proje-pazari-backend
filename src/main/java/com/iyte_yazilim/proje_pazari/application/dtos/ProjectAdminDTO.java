package com.iyte_yazilim.proje_pazari.application.dtos;

import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Detailed project information for admin operations")
public record ProjectAdminDTO(
        @Schema(description = "Project ID") String projectId,
        @Schema(description = "Project title") String title,
        @Schema(description = "Project description") String description,
        @Schema(description = "Project summary") String summary,
        @Schema(description = "Project status") ProjectStatus status,
        @Schema(description = "Owner user ID") String ownerId,
        @Schema(description = "Owner name") String ownerName,
        @Schema(description = "Owner email") String ownerEmail,
        @Schema(description = "Maximum team size") Integer maxTeamSize,
        @Schema(description = "Current team size") Integer currentTeamSize,
        @Schema(description = "Required skills") List<String> requiredSkills,
        @Schema(description = "Category") String category,
        @Schema(description = "Deadline") LocalDateTime deadline,
        @Schema(description = "Whether the project is featured") boolean featured,
        @Schema(description = "Number of applications") int applicationCount,
        @Schema(description = "Creation date") LocalDateTime createdAt,
        @Schema(description = "Last update date") LocalDateTime updatedAt) {}
