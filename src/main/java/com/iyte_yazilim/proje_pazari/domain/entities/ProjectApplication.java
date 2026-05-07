package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.exceptions.IllegalApplicationStateException;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
 * <h2>State Transitions:</h2>
 *
 * <pre>
 * PENDING → APPROVED   (via {@link #approve()})
 *        → REJECTED   (via {@link #reject()})
 *        → WITHDRAWN  (via {@link #withdraw()})
 * </pre>
 *
 * <p>All terminal transitions are guarded: calling any workflow method on a non-PENDING application
 * throws {@link IllegalApplicationStateException}.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 2.0
 * @since 2024-01-01
 * @see Project
 * @see User
 * @see com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus
 */
@Getter
@NoArgsConstructor
@SuppressWarnings("unused")
public class ProjectApplication extends BaseEntity<Ulid> {

    /** The project this application is for. */
    private Project project;

    /** The user who submitted this application. */
    private User user;

    /** The current status of the application. Defaults to PENDING. */
    private ApplicationStatus status = ApplicationStatus.PENDING;

    /**
     * Creates a new application for the given project and user.
     *
     * <p>The initial status is always {@link ApplicationStatus#PENDING}.
     *
     * @param project the target project
     * @param user the applicant
     */
    public ProjectApplication(Project project, User user) {
        this.project = project;
        this.user = user;
        this.status = ApplicationStatus.PENDING;
    }

    // ---------------------------------------------------------------------------
    //  Workflow Methods — enforce state-machine invariants
    // ---------------------------------------------------------------------------

    /**
     * Approves this application.
     *
     * @throws IllegalApplicationStateException if the current status is not {@link
     *     ApplicationStatus#PENDING}
     */
    public void approve() {
        requirePending("approve");
        this.status = ApplicationStatus.APPROVED;
    }

    /**
     * Rejects this application.
     *
     * @throws IllegalApplicationStateException if the current status is not {@link
     *     ApplicationStatus#PENDING}
     */
    public void reject() {
        requirePending("reject");
        this.status = ApplicationStatus.REJECTED;
    }

    /**
     * Withdraws this application (by the applicant).
     *
     * @throws IllegalApplicationStateException if the current status is not {@link
     *     ApplicationStatus#PENDING}
     */
    public void withdraw() {
        requirePending("withdraw");
        this.status = ApplicationStatus.WITHDRAWN;
    }

    // ---------------------------------------------------------------------------
    //  Infrastructure / Mapping Support
    // ---------------------------------------------------------------------------

    /**
     * Reconstitutes the application state from persistence without triggering guards.
     *
     * <p><strong>Infrastructure-only</strong> — must not be called from domain or application code.
     *
     * @param project the project
     * @param user the applicant
     * @param status the persisted status
     */
    public void reconstitute(Project project, User user, ApplicationStatus status) {
        this.project = project;
        this.user = user;
        this.status = status;
    }

    // ---------------------------------------------------------------------------
    //  Internal helpers
    // ---------------------------------------------------------------------------

    private void requirePending(String action) {
        if (this.status != ApplicationStatus.PENDING) {
            throw new IllegalApplicationStateException(action, this.status);
        }
    }
}
