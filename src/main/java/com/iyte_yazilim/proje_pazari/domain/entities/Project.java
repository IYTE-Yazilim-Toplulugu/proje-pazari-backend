package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import java.time.LocalDateTime;
import java.util.List;

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
 * project.setStatus(ProjectStatus.OPEN);
 * }</pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @see User
 * @see ProjectApplication
 * @see ProjectStatus
 * @since 2024-01-01
 */
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

    private Integer currentTeamSize;
    private List<String> requiredSkills;
    private String category;
    private LocalDateTime deadline;

    public Project(
            String title,
            String description,
            String summary,
            ProjectStatus status,
            User owner,
            List<ProjectApplication> applications,
            Integer maxTeamSize,
            Integer currentTeamSize,
            List<String> requiredSkills,
            String category,
            LocalDateTime deadline) {
        this.title = title;
        this.description = description;
        this.summary = summary;
        this.status = status;
        this.owner = owner;
        this.applications = applications;
        this.maxTeamSize = maxTeamSize;
        this.currentTeamSize = currentTeamSize;
        this.requiredSkills = requiredSkills;
        this.category = category;
        this.deadline = deadline;
    }

    public Project() {}

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getSummary() {
        return this.summary;
    }

    public ProjectStatus getStatus() {
        return this.status;
    }

    public User getOwner() {
        return this.owner;
    }

    public List<ProjectApplication> getApplications() {
        return this.applications;
    }

    public Integer getMaxTeamSize() {
        return this.maxTeamSize;
    }

    public Integer getCurrentTeamSize() {
        return this.currentTeamSize;
    }

    public List<String> getRequiredSkills() {
        return this.requiredSkills;
    }

    public String getCategory() {
        return this.category;
    }

    public LocalDateTime getDeadline() {
        return this.deadline;
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

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public void setApplications(List<ProjectApplication> applications) {
        this.applications = applications;
    }

    public void setMaxTeamSize(Integer maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public void setCurrentTeamSize(Integer currentTeamSize) {
        this.currentTeamSize = currentTeamSize;
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
