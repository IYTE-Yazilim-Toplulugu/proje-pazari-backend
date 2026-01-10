package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
 * @since 2024-01-01
 * @see User
 * @see ProjectApplication
 * @see ProjectStatus
 */
@Getter
@Setter
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
    public void setOwner(User owner) {
        this.owner = owner;
    }
}
