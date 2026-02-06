package com.iyte_yazilim.proje_pazari.domain.models.results;

/**
 * Result object returned after successful application submission.
 *
 * @param applicationId The ULID of the created application
 * @param projectId The ULID of the project
 * @param projectTitle The title of the project
 * @param status The status of the application (will be PENDING)
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 */
public record SubmitApplicationCommandResult(
        String applicationId, String projectId, String projectTitle, String status) {}
