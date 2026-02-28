package com.iyte_yazilim.proje_pazari.domain.models.results;

/**
 * Result object returned after successful project status update.
 *
 * @param projectId The ULID of the project
 * @param projectTitle The title of the project
 * @param oldStatus The previous status
 * @param newStatus The new status
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 */
public record UpdateProjectStatusCommandResult(
        String projectId, String projectTitle, String oldStatus, String newStatus) {}
