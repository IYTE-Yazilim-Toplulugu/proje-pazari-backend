package com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Command to update user profile information.
 *
 * <p>This command allows users to update their profile details including
 * personal information, social links, and language preference.
 *
 * <p><strong>Fields:</strong>
 * <ul>
 *   <li>firstName, lastName - Basic personal information</li>
 *   <li>description - User bio/description</li>
 *   <li>linkedinUrl, githubUrl - Social profile links</li>
 *   <li>preferredLanguage - Language preference for API responses (tr, en)</li>
 * </ul>
 *
 * @param userId the ID of the user to update
 * @param firstName user's first name (optional, max 50 chars)
 * @param lastName user's last name (optional, max 50 chars)
 * @param description user's bio/description (optional, max 1000 chars)
 * @param linkedinUrl LinkedIn profile URL (optional, must be valid LinkedIn URL)
 * @param githubUrl GitHub profile URL (optional, must be valid GitHub URL)
 * @param preferredLanguage preferred language for API responses (optional, tr or en)
 */
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
        String githubUrl,

        @Schema(
                description = "Preferred language (tr, en)",
                example = "en",
                allowableValues = {"tr", "en"})
        @Pattern(regexp = "^(tr|en)$", message = "Language must be either 'tr' or 'en'")
        String preferredLanguage) {

    /**
     * Validates the command.
     *
     * <p>Validates:
     * <ul>
     *   <li>LinkedIn URL format (if provided)</li>
     *   <li>GitHub URL format (if provided)</li>
     * </ul>
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (linkedinUrl != null
                && !linkedinUrl.isBlank()
                && !linkedinUrl.matches("^https://(www\\.)?linkedin\\.com/.*")) {
            throw new IllegalArgumentException("Invalid LinkedIn URL format");
        }
        if (githubUrl != null
                && !githubUrl.isBlank()
                && !githubUrl.matches("^https://github\\.com/[a-zA-Z0-9_-]+(/.*)?$")) {
            throw new IllegalArgumentException("Invalid GitHub URL format");
        }
    }
}