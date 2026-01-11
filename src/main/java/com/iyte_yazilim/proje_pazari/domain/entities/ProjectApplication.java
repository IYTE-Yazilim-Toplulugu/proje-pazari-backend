package com.iyte_yazilim.proje_pazari.domain.entities;

import com.github.f4b6a3.ulid.Ulid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
 * @since 2024-01-01
 * @see Project
 * @see User
 * @see com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class ProjectApplication extends BaseEntity<Ulid> {

    /** The project this application is for. */
    private Project project;

    /** The user who submitted this application. */
    private User user;
}
