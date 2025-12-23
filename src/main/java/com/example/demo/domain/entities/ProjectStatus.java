package com.example.demo.domain.entities;

/**
 * Represents the lifecycle status of a project.
 * 
 * DRAFT - Project is being prepared, not visible to others yet
 * OPEN - Project is accepting applications from interested users
 * IN_PROGRESS - Team has been formed, project work has started
 * COMPLETED - Project has been finished
 * CANCELLED - Project was abandoned or cancelled
 */
public enum ProjectStatus {
    DRAFT,
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
