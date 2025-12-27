package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Comprehensive user profile data transfer object")
public record UserProfileDTO(
        @Schema(description = "User ID", example = "01HQZX9K2M3N4P5Q6R7S8T9V0W")
        String id,

        @Schema(description = "Email address", example = "user@example.com")
        String email,

        @Schema(description = "First name", example = "John")
        String firstName,

        @Schema(description = "Last name", example = "Doe")
        String lastName,

        @Schema(description = "Full name", example = "John Doe")
        String fullName,

        @Schema(description = "User description/bio")
        String description,

        @Schema(description = "Profile picture URL")
        String profilePictureUrl,

        @Schema(description = "LinkedIn profile URL")
        String linkedinUrl,

        @Schema(description = "GitHub profile URL")
        String githubUrl,

        @Schema(description = "Account creation timestamp")
        LocalDateTime joinedAt,

        @Schema(description = "Number of projects created by user")
        int projectsCreated,

        @Schema(description = "Number of applications submitted by user")
        int applicationsSubmitted,

        @Schema(description = "List of user's projects")
        List<ProjectSummaryDTO> projects
) {
}
