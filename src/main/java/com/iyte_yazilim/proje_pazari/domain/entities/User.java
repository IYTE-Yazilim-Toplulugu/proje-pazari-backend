package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User domain entity (Clean Architecture - Domain Layer).
 *
 * <p>Pure domain model without persistence annotations. Represents a user in the business domain.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity<Ulid> {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String description;
    private String profilePictureUrl;
    private String linkedinUrl;
    private String githubUrl;
    private boolean isActive;

    // Email verification fields
    private boolean emailVerified = false;
    private String verificationToken;
    private LocalDateTime verificationTokenExpiresAt;
    private LocalDateTime emailVerifiedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
