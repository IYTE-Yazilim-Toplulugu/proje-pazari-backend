package com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Command to update user profile")
public record UpdateUserProfileCommand(
        @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED)
        String userId,

        @Schema(description = "First name")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstName,

        @Schema(description = "Last name")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastName,

        @Schema(description = "User description/bio")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "LinkedIn profile URL")
        String linkedinUrl,

        @Schema(description = "GitHub profile URL")
        String githubUrl
) {
    public void validate() {
        if (linkedinUrl != null && !linkedinUrl.isBlank() && !linkedinUrl.matches("^https://(www\\.)?linkedin\\.com/.*")) {
            throw new IllegalArgumentException("Invalid LinkedIn URL format");
        }
        if (githubUrl != null && !githubUrl.isBlank()
                && !githubUrl.matches("^https://github\\.com/[a-zA-Z0-9_-]+(/[a-zA-Z0-9._-]+)?/?$")) {
            throw new IllegalArgumentException("Invalid GitHub URL format");
        }
    }
}
