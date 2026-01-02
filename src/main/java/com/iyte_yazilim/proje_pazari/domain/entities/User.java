package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.validation.IyteEmail;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("unused")
@Entity
@Table(name = "users")
public class User extends BaseEntity<Ulid> {

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column private String verificationToken;

    @Column private LocalDateTime verificationTokenExpiresAt;

    @Column private LocalDateTime emailVerifiedAt;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @IyteEmail
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    private String description;

    private String profilePictureUrl;

    private String linkedinUrl;
    private String githubUrl;

    private boolean isActive;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
