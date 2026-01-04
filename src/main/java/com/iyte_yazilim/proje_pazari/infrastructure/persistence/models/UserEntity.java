package com.iyte_yazilim.proje_pazari.infrastructure.persistence.models;

import com.github.f4b6a3.ulid.Ulid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity representing a user in the database.
 *
 * <p>This entity maps to the "users" table and contains all user-related
 * information including authentication credentials, profile data, and preferences.
 *
 * <p><strong>Language Preference:</strong>
 * The preferredLanguage field stores the user's language preference for API responses.
 * It's used by the internationalization system to deliver localized messages.
 *
 * @see com.iyte_yazilim.proje_pazari.domain.entities.User
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * Unique identifier for the user (ULID format).
     *
     * <p>ULID provides:
     * <ul>
     *   <li>Lexicographically sortable</li>
     *   <li>URL-safe</li>
     *   <li>128-bit compatibility with UUID</li>
     *   <li>Auto-generated on creation</li>
     * </ul>
     */
    @Id
    @Column(length = 26)
    private String id;

    /**
     * User's email address (unique, used for authentication).
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * User's hashed password (BCrypt).
     */
    @Column(nullable = false)
    private String password;

    /**
     * User's first name.
     */
    @Column(name = "first_name")
    private String firstName;

    /**
     * User's last name.
     */
    @Column(name = "last_name")
    private String lastName;

    /**
     * User's profile description/bio.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * URL to user's profile picture.
     */
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    /**
     * User's LinkedIn profile URL.
     */
    @Column(name = "linkedin_url")
    private String linkedinUrl;

    /**
     * User's GitHub profile URL.
     */
    @Column(name = "github_url")
    private String githubUrl;

    /**
     * User's preferred language for API responses (e.g., "tr", "en").
     *
     * <p><strong>Database Configuration:</strong>
     * <ul>
     *   <li>Column name: preferred_language</li>
     *   <li>Length: 5 characters (sufficient for language codes like "en-US")</li>
     *   <li>Default: "tr" (Turkish)</li>
     *   <li>Nullable: No (has default value)</li>
     * </ul>
     *
     * <p><strong>Supported Values:</strong>
     * <ul>
     *   <li>"tr" - Turkish (default)</li>
     *   <li>"en" - English</li>
     * </ul>
     *
     * <p><strong>Usage by Interceptor:</strong>
     * This field is read by {@link com.iyte_yazilim.proje_pazari.presentation.config.LocaleInterceptor}
     * to determine the user's preferred language when the Accept-Language header is not present.
     *
     * <p><strong>Database Migration Note:</strong>
     * If you're adding this field to an existing database, ensure your migration script
     * sets the default value for existing rows:
     * <pre>
     * ALTER TABLE users ADD COLUMN preferred_language VARCHAR(5) DEFAULT 'tr';
     * UPDATE users SET preferred_language = 'tr' WHERE preferred_language IS NULL;
     * </pre>
     *
     * @see com.iyte_yazilim.proje_pazari.presentation.config.LocaleInterceptor
     */
    @Column(name = "preferred_language", length = 5, nullable = false)
    private String preferredLanguage = "tr";

    /**
     * Timestamp of when the user account was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of when the user account was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Indicates whether the user account is active.
     *
     * <p>Inactive accounts:
     * <ul>
     *   <li>Cannot log in</li>
     *   <li>Cannot access protected resources</li>
     *   <li>Are soft-deleted (not removed from database)</li>
     * </ul>
     *
     * <p>Default: true (active)
     */
    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = true;

    /**
     * JPA lifecycle callback - executed before persisting a new entity.
     *
     * <p>Initializes:
     * <ul>
     *   <li>id (ULID) if not set</li>
     *   <li>isActive to true if not set</li>
     *   <li>preferredLanguage to "tr" if not set</li>
     *   <li>createdAt timestamp</li>
     * </ul>
     */
    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = Ulid.fast().toString();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (preferredLanguage == null || preferredLanguage.isBlank()) {
            preferredLanguage = "tr";
        }
        createdAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback - executed before updating an existing entity.
     *
     * <p>Updates:
     * <ul>
     *   <li>updatedAt timestamp</li>
     * </ul>
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}