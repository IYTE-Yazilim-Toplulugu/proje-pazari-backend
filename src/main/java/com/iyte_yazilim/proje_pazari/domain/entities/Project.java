package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a project in the Proje Pazarı marketplace.
 *
 * <p>Projects are the central entity in the system where users can:
 *
 * <ul>
 *   <li>Post projects seeking collaborators
 *   <li>Receive applications from interested users
 *   <li>Manage project status through its lifecycle
 * </ul>
 *
 * <h2>Project Lifecycle:</h2>
 *
 * <ol>
 *   <li>{@link ProjectStatus#DRAFT} - Initial state when created
 *   <li>{@link ProjectStatus#OPEN} - Accepting applications
 *   <li>{@link ProjectStatus#IN_PROGRESS} - Actively being worked on
 *   <li>{@link ProjectStatus#COMPLETED} - Successfully finished
 *   <li>{@link ProjectStatus#CANCELLED} - Abandoned or cancelled
 * </ol>
 *
 * <h2>Example Usage:</h2>
 *
 * <pre>{@code
 * Project project = new Project();
 * project.setTitle("Mobile App Development");
 * project.setDescription("Looking for Flutter developers");
 * project.setOwner(currentUser);
 * }</pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @see User
 * @see ProjectApplication
 * @see ProjectStatus
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("unused")
public class Project extends BaseEntity<Ulid> {

    /** The title of the project. Should be concise and descriptive. */
    private String title;

    /** Detailed description of the project, requirements, and expectations. */
    private String description;

    /** A brief summary of the project for listing displays. */
    private String summary;

    /**
     * Current status of the project in its lifecycle. Defaults to {@link ProjectStatus#DRAFT} when
     * created.
     *
     * @see ProjectStatus
     */
    private ProjectStatus status = ProjectStatus.DRAFT;

    /**
     * The user who created and owns this project. Only the owner can manage the project and review
     * applications.
     */
    private User owner;

    /**
     * List of applications submitted to this project by interested users.
     *
     * @see ProjectApplication
     */
    private List<ProjectApplication> applications;

    /**
     * Sets the owner of this project.
     *
     * @param owner the user to set as project owner
     */
    private Integer maxTeamSize;

    private Integer currentTeamSize = 0;
    private List<String> requiredSkills;
    private String category;
    private LocalDateTime deadline;

    // --- 1. STATE MACHINE ---
    public void transitionTo(ProjectStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null.");
        }

        if (this.status == newStatus) {
            return; // Ignore redundant transitions
        }

        boolean isValidTransition =
                switch (this.status) {
                    case DRAFT ->
                            newStatus == ProjectStatus.OPEN || newStatus == ProjectStatus.CANCELLED;
                    case OPEN ->
                            newStatus == ProjectStatus.IN_PROGRESS
                                    || newStatus == ProjectStatus.CANCELLED;
                    case IN_PROGRESS ->
                            newStatus == ProjectStatus.COMPLETED
                                    || newStatus == ProjectStatus.CANCELLED;
                    case COMPLETED, CANCELLED ->
                            false; // Terminal states cannot transition to anything
                };

        if (!isValidTransition) {
            throw new IllegalStateException(
                    String.format(
                            "Invalid state transition from %s to %s", this.status, newStatus));
        }

        this.status = newStatus;

        // Note: If you are using Spring Data's @DomainEvents, you would register the event here:
        // registerEvent(new ProjectStatusChangedEvent(this.getId(), this.status));
    }

    // --- 2. CAPACITY ENCAPSULATION ---
    public void incrementTeamSize() {
        if (isFull()) {
            throw new IllegalStateException(
                    String.format(
                            "Project is at maximum capacity. Cannot exceed %d members.",
                            this.maxTeamSize));
        }
        this.currentTeamSize++;
    }

    public boolean isFull() {
        if (this.maxTeamSize == null) {
            return false; // Assuming null means unlimited, or handle according to your domain rules
        }
        return this.currentTeamSize >= this.maxTeamSize;
    }

    public boolean canAcceptApplications() {
        return this.status == ProjectStatus.OPEN && !isFull();
    }

    // --- 3. LIFECYCLE GUARDS ---
    public boolean canBeDeleted() {
        // Blocked if IN_PROGRESS
        return this.status != ProjectStatus.IN_PROGRESS;
    }

    public boolean canBeUpdated() {
        // Blocked if COMPLETED or CANCELLED
        return this.status != ProjectStatus.COMPLETED && this.status != ProjectStatus.CANCELLED;
    }

    // --- INFRASTRUCTURE/MAPPING ONLY ---
    // Safely reconstitutes the domain object from the database without firing state machine rules
    public void reconstitute(ProjectStatus status, Integer currentTeamSize) {
        this.status = status;
        this.currentTeamSize = currentTeamSize != null ? currentTeamSize : 0;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setApplications(List<ProjectApplication> applications) {
        this.applications = applications;
    }

    public void setMaxTeamSize(Integer maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
