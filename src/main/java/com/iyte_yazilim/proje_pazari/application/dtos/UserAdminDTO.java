package com.iyte_yazilim.proje_pazari.application.dtos;

import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Detailed user information for admin operations")
public record UserAdminDTO(
        @Schema(description = "User ID") String userId,
        @Schema(description = "Email address") String email,
        @Schema(description = "First name") String firstName,
        @Schema(description = "Last name") String lastName,
        @Schema(description = "User description/bio") String description,
        @Schema(description = "Profile picture URL") String profilePictureUrl,
        @Schema(description = "LinkedIn profile URL") String linkedinUrl,
        @Schema(description = "GitHub profile URL") String githubUrl,
        @Schema(description = "User role") RoleType role,
        @Schema(description = "Whether the account is active") Boolean isActive,
        @Schema(description = "Account creation date") LocalDateTime createdAt,
        @Schema(description = "Last update date") LocalDateTime updatedAt,
        @Schema(description = "Number of owned projects") int projectCount,
        @Schema(description = "Number of submitted applications") int applicationCount) {}
