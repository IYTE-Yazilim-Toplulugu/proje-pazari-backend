package com.iyte_yazilim.proje_pazari.domain.models.results;

/**
 * Result object returned after successful application review.
 *
 * @param applicationId The ULID of the reviewed application
 * @param projectId The ULID of the project
 * @param projectTitle The title of the project
 * @param status The new status of the application (APPROVED or REJECTED)
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 */
public record ReviewApplicationCommandResult(
        String applicationId, String projectId, String projectTitle, String status) {}
