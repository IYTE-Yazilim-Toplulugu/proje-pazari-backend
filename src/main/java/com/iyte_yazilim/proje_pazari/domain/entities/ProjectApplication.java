package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;

/**
 * Represents an application submitted by a user to join a project.
 *
 * <p>When a user is interested in a project, they submit an application which links the user to the
 * target project. Project owners can then review and approve or reject these applications.
 *
 * <h2>Application Flow:</h2>
 *
 * <ol>
 *   <li>User finds an interesting project
 *   <li>User submits an application to the project
 *   <li>Project owner reviews the application
 *   <li>Application is approved or rejected
 * </ol>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @see Project
 * @see User
 * @see com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus
 * @since 2024-01-01
 */
@SuppressWarnings("unused")
public class ProjectApplication extends BaseEntity<Ulid> {

    /** The project this application is for. */
    private Project project;

    /** The user who submitted this application. */
    private User user;

    /** The current status of the application. Defaults to PENDING. */
    private ApplicationStatus status = ApplicationStatus.PENDING;

    public ProjectApplication(Project project, User user, ApplicationStatus status) {
        this.project = project;
        this.user = user;
        this.status = status;
    }

    public ProjectApplication() {}

    public Project getProject() {
        return this.project;
    }

    public User getUser() {
        return this.user;
    }

    public ApplicationStatus getStatus() {
        return this.status;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}
