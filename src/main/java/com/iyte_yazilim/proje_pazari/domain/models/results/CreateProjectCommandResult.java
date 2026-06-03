package com.iyte_yazilim.proje_pazari.domain.models.results;

import java.time.LocalDateTime;

/**
 * Result returned after successful project creation.
 *
 * <p>Contains all project information including owner and initial configuration.
 *
 * @param projectId the unique ULID identifier assigned to the new project
 * @param projectName the title of the created project
 * @param description the project's detailed description
 * @param summary the brief summary of the project for listing displays
 * @param ownerId the unique identifier of the project owner
 * @param maxTeamSize the maximum number of team members allowed
 * @param currentTeamSize the current number of team members (1 at creation — the owner)
 * @param requiredSkills array of skills required for the project
 * @param category the category the project belongs to
 * @param deadline the deadline by which the project should be completed
 * @author IYTE Yazılım Topluluğu
 * @version 1.1
 * @since 2024-01-01
 * @see com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectCommand
 */
public record CreateProjectCommandResult(
        String projectId,
        String projectName,
        String description,
        String summary,
        String ownerId,
        Integer maxTeamSize,
        Integer currentTeamSize,
        String[] requiredSkills,
        String category,
        LocalDateTime deadline) {}