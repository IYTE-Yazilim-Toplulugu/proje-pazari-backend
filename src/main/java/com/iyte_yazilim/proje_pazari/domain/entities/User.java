package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.exceptions.IllegalUserStateException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;

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
 * <h2>Lifecycle Methods:</h2>
 *
 * <ul>
 *   <li>{@link #activate()} / {@link #deactivate()} — guarded account status transitions
 *   <li>{@link #assignRole(RoleType)} / {@link #removeRole(RoleType)} — guarded role management
 * </ul>
 *
 * <h2>Example Usage:</h2>
 *
 * <pre>{@code
 * User user = new User("student@std.iyte.edu.tr", "hashedPassword", "John", "Doe");
 * user.deactivate();   // sets isActive = false
 * user.activate();     // sets isActive = true
 * user.assignRole(RoleType.ADMIN);
 * }</pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 2.0
 * @since 2024-01-01
 * @see Project
 * @see BaseEntity
 */
@Getter
public class User extends BaseEntity<Ulid> {

    /** User's email address used for authentication. Must be unique across the system. */
    private String email;

    /** User's encrypted password. Never stored in plain text; stored as a BCrypt-style hash. */
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

    /** User's preferred language for UI/messages (e.g. "tr", "en"). */
    private String preferredLanguage;

    /** Indicates whether the user account is active. Inactive accounts cannot log in. */
    private boolean isActive;

    @Getter(AccessLevel.NONE)
    private Set<RoleType> roles = new HashSet<>();

    /** Default constructor. Initializes default values for isActive and roles. */
    public User() {
        this.isActive = true;
        this.roles = new HashSet<>();
    }

    /**
     * Constructor for creating a new user.
     *
     * @param email user's email address
     * @param password user's encrypted password
     * @param firstName user's first name
     * @param lastName user's last name
     */
    public User(String email, String password, String firstName, String lastName) {
        this();
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // ---------------------------------------------------------------------------
    //  Query Methods
    // ---------------------------------------------------------------------------

    /**
     * Returns the user's full name by combining first and last name.
     *
     * @return full name in format "FirstName LastName"
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns an unmodifiable view of this user's roles.
     *
     * <p>To mutate roles, use {@link #assignRole(RoleType)} and {@link #removeRole(RoleType)}.
     *
     * @return unmodifiable set of roles
     */
    public Set<RoleType> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    /**
     * Checks whether this user has the given role.
     *
     * @param role the role to check
     * @return {@code true} if the user has the role
     */
    public boolean hasRole(RoleType role) {
        return roles.contains(role);
    }

    // ---------------------------------------------------------------------------
    //  Lifecycle Methods — guarded account status transitions
    // ---------------------------------------------------------------------------

    /**
     * Activates an inactive user account.
     *
     * @throws IllegalUserStateException if the account is already active
     */
    public void activate() {
        if (this.isActive) {
            throw new IllegalUserStateException("User account is already active");
        }
        this.isActive = true;
    }

    /**
     * Deactivates an active user account (soft-delete).
     *
     * @throws IllegalUserStateException if the account is already inactive
     */
    public void deactivate() {
        if (!this.isActive) {
            throw new IllegalUserStateException("User account is already inactive");
        }
        this.isActive = false;
    }

    // ---------------------------------------------------------------------------
    //  Role Management — guarded role mutations
    // ---------------------------------------------------------------------------

    /**
     * Assigns a role to this user. If the user already has the role, this is a no-op.
     *
     * <p>When assigning a new primary role (e.g. promoting to ADMIN), existing roles are replaced
     * to match the single-role-per-user convention used by this system.
     *
     * @param role the role to assign
     * @throws IllegalArgumentException if role is null
     */
    public void assignRole(RoleType role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.roles.clear();
        this.roles.add(role);
    }

    /**
     * Removes a role from this user.
     *
     * @param role the role to remove
     * @throws IllegalArgumentException if role is null
     * @throws IllegalStateException if removing the role would leave the user with no roles
     */
    public void removeRole(RoleType role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        if (!this.roles.contains(role)) {
            return; // no-op if role not present
        }
        if (this.roles.size() <= 1) {
            throw new IllegalStateException("Cannot remove the last role from a user");
        }
        this.roles.remove(role);
    }

    /**
     * Adds a role to this user without clearing existing roles.
     *
     * @param role the role to add
     * @throws IllegalArgumentException if role is null
     */
    public void addRole(RoleType role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        this.roles.add(role);
    }

    // ---------------------------------------------------------------------------
    //  Profile Setters — simple data fields, no invariants
    // ---------------------------------------------------------------------------

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    // ---------------------------------------------------------------------------
    //  Infrastructure / Mapping Support
    // ---------------------------------------------------------------------------

    /**
     * Reconstitutes the active state from persistence without triggering lifecycle guards.
     *
     * <p><strong>Infrastructure-only</strong> — must not be called from domain or application code.
     * Use {@link #activate()} or {@link #deactivate()} in all application-layer code.
     *
     * @param active the persisted active flag
     */
    public void reconstituteActive(boolean active) {
        this.isActive = active;
    }

    /**
     * Reconstitutes roles from persistence without triggering role management guards.
     *
     * <p><strong>Infrastructure-only</strong> — must not be called from domain or application code.
     * Use {@link #addRole(RoleType)}, {@link #assignRole(RoleType)}, or {@link
     * #removeRole(RoleType)} in all application-layer code.
     *
     * @param roles the persisted roles
     */
    public void reconstituteRoles(Set<RoleType> roles) {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }
}
