package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain entity representing a user in the system.
 *
 * <p>This entity represents a user with their profile information,
 * authentication credentials, and preferences.
 *
 * <p><strong>Language Preference:</strong>
 * Users can specify their preferred language for API responses.
 * Supported languages: Turkish (tr), English (en).
 * Default: Turkish (tr).
 *
 * @see com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("unused")
public class User extends BaseEntity<Ulid> {

    /**
     * User's email address (unique identifier for authentication).
     */
    private String email;

    /**
     * User's hashed password (BCrypt).
     */
    private String password;

    /**
     * User's first name.
     */
    private String firstName;

    /**
     * User's last name.
     */
    private String lastName;

    /**
     * User's profile description/bio.
     */
    private String description;

    /**
     * URL to user's profile picture.
     */
    private String profilePictureUrl;

    /**
     * User's LinkedIn profile URL.
     */
    private String linkedinUrl;

    /**
     * User's GitHub profile URL.
     */
    private String githubUrl;

    /**
     * User's preferred language for API responses.
     *
     * <p>Supported values:
     * <ul>
     *   <li>"tr" - Turkish (default)</li>
     *   <li>"en" - English</li>
     * </ul>
     *
     * <p>This preference is used by {@link com.iyte_yazilim.proje_pazari.presentation.config.LocaleInterceptor}
     * to determine the language for localized messages when Accept-Language header is not present.
     *
     * <p><strong>Default:</strong> "tr" (Turkish)
     *
     * @see #getPreferredLocale()
     */
    private String preferredLanguage = "tr";

    /**
     * Indicates whether the user account is active.
     *
     * <p>Inactive accounts cannot log in or access protected resources.
     */
    private boolean isActive = true;

    /**
     * Gets the user's full name by concatenating first and last name.
     *
     * @return the full name in format "FirstName LastName"
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Gets the user's preferred language as a {@link Locale} object.
     *
     * <p>This helper method converts the string language preference
     * to a proper Locale object that can be used with Spring's
     * internationalization framework.
     *
     * <p><strong>Usage:</strong>
     * <pre>
     * {@code
     * User user = userRepository.findById(userId);
     * Locale locale = user.getPreferredLocale();
     * String message = messageSource.getMessage("key", null, locale);
     * }
     * </pre>
     *
     * <p><strong>Behavior:</strong>
     * <ul>
     *   <li>If preferredLanguage is null or blank → returns Turkish (tr)</li>
     *   <li>If preferredLanguage is "tr" → returns Turkish locale</li>
     *   <li>If preferredLanguage is "en" → returns English locale</li>
     *   <li>If preferredLanguage is unsupported → returns Turkish (tr)</li>
     * </ul>
     *
     * @return the user's preferred locale, or Turkish locale if not set/invalid
     *
     * @see Locale
     * @see #preferredLanguage
     */
    public Locale getPreferredLocale() {
        if (preferredLanguage == null || preferredLanguage.isBlank()) {
            return Locale.forLanguageTag("tr");
        }

        // Normalize to lowercase for comparison
        String normalizedLang = preferredLanguage.toLowerCase().trim();

        // Validate against supported languages
        return switch (normalizedLang) {
            case "tr" -> Locale.forLanguageTag("tr");
            case "en" -> Locale.forLanguageTag("en");
            default -> {
                // Unsupported language - fallback to Turkish
                yield Locale.forLanguageTag("tr");
            }
        };
    }

    /**
     * Sets the user's preferred language with validation.
     *
     * <p>This method validates the input and normalizes it before setting.
     * Only supported languages (tr, en) are accepted.
     *
     * <p><strong>Behavior:</strong>
     * <ul>
     *   <li>Null or blank → sets to "tr" (default)</li>
     *   <li>"tr" or "TR" → sets to "tr"</li>
     *   <li>"en" or "EN" → sets to "en"</li>
     *   <li>Unsupported value → sets to "tr" (default)</li>
     * </ul>
     *
     * @param preferredLanguage the language code to set ("tr" or "en")
     */
    public void setPreferredLanguage(String preferredLanguage) {
        if (preferredLanguage == null || preferredLanguage.isBlank()) {
            this.preferredLanguage = "tr";
            return;
        }

        String normalizedLang = preferredLanguage.toLowerCase().trim();

        // Validate and set
        switch (normalizedLang) {
            case "tr", "en" -> this.preferredLanguage = normalizedLang;
            default -> this.preferredLanguage = "tr"; // Fallback to default
        }
    }

    /**
     * Checks if the user has a valid preferred language set.
     *
     * @return true if preferredLanguage is set to a supported value, false otherwise
     */
    public boolean hasPreferredLanguage() {
        return preferredLanguage != null
                && !preferredLanguage.isBlank()
                && (preferredLanguage.equalsIgnoreCase("tr")
                || preferredLanguage.equalsIgnoreCase("en"));
    }

    /**
     * Checks if the user's preferred language is Turkish.
     *
     * @return true if preferred language is Turkish, false otherwise
     */
    public boolean isTurkishPreferred() {
        return getPreferredLocale().getLanguage().equals("tr");
    }

    /**
     * Checks if the user's preferred language is English.
     *
     * @return true if preferred language is English, false otherwise
     */
    public boolean isEnglishPreferred() {
        return getPreferredLocale().getLanguage().equals("en");
    }
}