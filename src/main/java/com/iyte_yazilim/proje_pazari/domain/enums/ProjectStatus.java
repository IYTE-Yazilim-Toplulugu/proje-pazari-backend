package com.iyte_yazilim.proje_pazari.domain.enums;

/**
 * Represents the lifecycle status of a project in the Proje Pazarı system.
 *
 * <p>Projects transition through these states during their lifecycle:
 *
 * <pre>
 * DRAFT → OPEN → IN_PROGRESS → COMPLETED
 *              ↘           ↗
 *               CANCELLED
 * </pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see com.iyte_yazilim.proje_pazari.domain.entities.Project
 */
public enum ProjectStatus {
    /** Project is being drafted and not yet visible to others. */
    DRAFT,

    /** Project is open and accepting applications from users. */
    OPEN,

    /** Project has team members and work is actively ongoing. */
    IN_PROGRESS,

    /** Project has been successfully completed. */
    COMPLETED,

    /** Project has been cancelled or abandoned. */
    CANCELLED;

    /**
     * Safely parses a status string into a {@link ProjectStatus}, returning {@code null} instead of
     * throwing when the value is {@code null} or does not match any known status.
     *
     * <p>Useful when reading status values from external/denormalized sources (e.g. Elasticsearch
     * documents) where a stale or unexpected value should degrade gracefully rather than fail the
     * whole request.
     *
     * @param value the status string to parse, may be {@code null}
     * @return the matching {@link ProjectStatus}, or {@code null} if unknown or {@code null}
     */
    public static ProjectStatus fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
