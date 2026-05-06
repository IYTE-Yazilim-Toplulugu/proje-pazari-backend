package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import java.util.HashSet;
import java.util.Set;

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
 * User user = new User("student@std.iyte.edu.tr", "hashedPassword", "John", "Doe");
 * }</pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @see Project
 * @see BaseEntity
 * @since 2024-01-01
 */
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

    /** Indicates whether the user account is active. Inactive accounts cannot log in. */
    private boolean isActive;

    private Set<RoleType> roles = new HashSet<>();

    /** Default constructor. Initializes default values for emailVerified, isActive, and roles. */
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

    /**
     * Returns the user's full name by combining first and last name.
     *
     * @return full name in format "FirstName LastName"
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getProfilePictureUrl() {
        return this.profilePictureUrl;
    }

    public String getLinkedinUrl() {
        return this.linkedinUrl;
    }

    public String getGithubUrl() {
        return this.githubUrl;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public Set<RoleType> getRoles() {
        return this.roles;
    }

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

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setRoles(Set<RoleType> roles) {
        this.roles = roles;
    }
}
