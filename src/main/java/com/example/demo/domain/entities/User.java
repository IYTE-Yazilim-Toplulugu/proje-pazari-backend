package com.example.demo.domain.entities;

import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Projects owned/created by this user.
 * CascadeType.ALL: When user is deleted, their projects are also deleted.
 * orphanRemoval: Projects removed from this list are deleted from DB.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(length = 1000)
    private String bio;

    /**
     * Comma-separated list of skills (e.g., "Java, Spring Boot, React")
     * Consider normalizing to a separate table if complex querying is needed.
     */
    @Column(length = 500)
    private String skills;

    @Column
    private String profilePictureUrl;

    @Column
    private String linkedinUrl;

    @Column
    private String githubUrl;

    /**
     * Projects owned/created by this user.
     * CascadeType.ALL: When user is deleted, their projects are also deleted.
     * orphanRemoval: Projects removed from this list are deleted from DB.
     */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // so that lombok won't set it to null
    private List<Project> ownedProjects = new ArrayList<>();

    /**
     * Applications this user has made to other projects.
     */
    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // so that lombok won't set it to null
    private List<ProjectApplication> applications = new ArrayList<>();

    public boolean addOwnedProject(Project project) {
        if (project == null || ownedProjects.contains(project)) {
            return false;
        }
        project.setOwner(this);
        return ownedProjects.add(project);
    }

    public boolean removeOwnedProject(Project project) {
        if (project == null || !ownedProjects.contains(project)) {
            return false;
        }
        boolean removed = ownedProjects.remove(project);
        if (removed) {
            project.setOwner(null);
        }
        return removed;
    }

    public boolean addApplication(ProjectApplication application) {
        if (application == null || applications.contains(application)) {
            return false;
        }
        application.setApplicant(this);
        return applications.add(application);
    }

    public boolean removeApplication(ProjectApplication application) {
        if (application == null || !applications.contains(application)) {
            return false;
        }
        boolean removed = applications.remove(application);
        if (removed) {
            application.setApplicant(null);
        }
        return removed;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
