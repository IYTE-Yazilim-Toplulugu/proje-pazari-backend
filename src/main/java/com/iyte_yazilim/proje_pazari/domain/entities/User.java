package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user in the Proje Pazarı system.
 *
 * <p>Users are the primary actors in the system who can:
 *
 * <ul>
 *   <li>Create and manage projects
 *   <li>Apply to other users' projects
 *   <li>Update their profile information
 * </ul>
 *
 * <p>Each user has a unique ULID identifier and must register with an email address. Users can
 * optionally add social links and profile pictures.
 *
 * <h2>Example Usage:</h2>
 *
 * <pre>{@code
 * User user = new User();
 * user.setEmail("student@std.iyte.edu.tr");
 * user.setFirstName("John");
 * user.setLastName("Doe");
 * }</pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see Project
 * @see BaseEntity
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("unused")
public class User extends BaseEntity<Ulid> {

    /** User's email address used for authentication. Must be unique across the system. */
    private String email;

    /**
     * User's encrypted password. Never stored in plain text; encrypted using BCrypt algorithm.
     *
     * @see org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
     */
    private String password;

    /** User's first name. Required field. */
    private String firstName;

    /** User's last name. Required field. */
    private String lastName;

    /** Optional biography or description about the user. */
    private String description;

    /** URL to the user's profile picture. Can be null if not set. */
    private String profilePictureUrl;

    /** User's LinkedIn profile URL. Optional social link. */
    private String linkedinUrl;

    /** User's GitHub profile URL. Optional social link. */
    private String githubUrl;

    /** Indicates whether the user account is active. Inactive accounts cannot login. */
    private boolean isActive;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    /**
     * Returns the user's full name by combining first and last name.
     *
     * @return full name in format "FirstName LastName"
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
