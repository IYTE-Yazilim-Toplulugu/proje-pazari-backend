package com.iyte_yazilim.proje_pazari.application.commands.updateLanguage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Command to update user's preferred language setting.
 *
 * <p>This command is used to change the user's language preference for API responses.
 * The language preference affects all localized messages returned by the API.
 *
 * <p><strong>Supported Languages:</strong>
 * <ul>
 *   <li>"tr" - Turkish</li>
 *   <li>"en" - English</li>
 * </ul>
 *
 * <p><strong>Validation:</strong>
 * The language field is validated to ensure only supported language codes are accepted.
 *
 * <p><strong>Usage Example:</strong>
 * <pre>
 * PUT /api/v1/users/me/language?language=en
 * Authorization: Bearer {token}
 * </pre>
 *
 * @param userId the ID of the user (typically from authentication context)
 * @param language the language code ("tr" or "en")
 */
@Schema(description = "Command to update user's language preference")
public record UpdateLanguageCommand(

        @Schema(
                description = "User ID",
                example = "01HQZX9K2M3N4P5Q6R7S8T9V0W",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "User ID is required")
        String userId,

        @Schema(
                description = "Preferred language code (tr or en)",
                example = "en",
                allowableValues = {"tr", "en"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Language is required")
        @Pattern(
                regexp = "^(tr|en)$",
                message = "Language must be either 'tr' or 'en'"
        )
        String language
) {

    /**
     * Validates the command.
     *
     * <p>Additional validation beyond Jakarta Bean Validation annotations.
     * Currently, all validation is handled by annotations, but this method
     * can be extended for more complex validation logic if needed.
     *
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        // All validation is currently handled by Jakarta Bean Validation annotations
        // This method can be extended for additional business rule validation if needed

        // Normalize and validate language (defensive programming)
        if (language != null) {
            String normalizedLang = language.trim().toLowerCase();
            if (!normalizedLang.equals("tr") && !normalizedLang.equals("en")) {
                throw new IllegalArgumentException(
                        "Invalid language code. Supported languages: tr, en"
                );
            }
        }
    }

    /**
     * Gets the normalized language code (lowercase, trimmed).
     *
     * @return normalized language code
     */
    public String getNormalizedLanguage() {
        if (language == null) {
            return "tr"; // Default fallback
        }
        return language.trim().toLowerCase();
    }
}