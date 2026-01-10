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
 * @param ownerId the unique identifier of the project owner
 * @param teamMemberIds array of user IDs for initial team members (may be empty)
 * @param tags array of tags/categories for the project
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectCommand
 */
public record CreateProjectCommandResult(
        String projectId,
        String projectName,
        String description,
        String ownerId,
        String[] teamMemberIds,
        String[] tags,
        Integer maxTeamSize,
        Integer currentTeamSize,
        String[] requiredSkills,
        String category,
        LocalDateTime deadline) {}
