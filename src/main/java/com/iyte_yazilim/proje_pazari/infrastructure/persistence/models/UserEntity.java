package com.iyte_yazilim.proje_pazari.infrastructure.persistence.models;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(length = 26)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<RoleType> roles = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "github_url")
    private String githubUrl;

    /**
     * User's preferred language for API responses (e.g., "tr", "en") Default: "tr" (Turkish) If
     * set, overrides Accept-Language header
     */
    @Column(name = "preferred_language", length = 5)
    private String preferredLanguage = "tr";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(
            name = "two_factor_enabled",
            nullable = false,
            columnDefinition = "boolean default false")
    private boolean isTwoFactorEnabled = false;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isBlank()) {
            id = Ulid.fast().toString();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add(RoleType.USER);
        }
        if (preferredLanguage == null || preferredLanguage.isBlank()) {
            preferredLanguage = "tr";
        }
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
