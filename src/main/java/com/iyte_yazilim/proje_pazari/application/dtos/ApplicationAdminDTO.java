package com.iyte_yazilim.proje_pazari.application.dtos;

import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Detailed application information for admin operations")
public record ApplicationAdminDTO(
        @Schema(description = "Application ID") String applicationId,
        @Schema(description = "Project ID") String projectId,
        @Schema(description = "Project title") String projectTitle,
        @Schema(description = "Applicant user ID") String applicantId,
        @Schema(description = "Applicant name") String applicantName,
        @Schema(description = "Applicant email") String applicantEmail,
        @Schema(description = "Application status") ApplicationStatus status,
        @Schema(description = "Application date") LocalDateTime createdAt,
        @Schema(description = "Last update date") LocalDateTime updatedAt) {}
