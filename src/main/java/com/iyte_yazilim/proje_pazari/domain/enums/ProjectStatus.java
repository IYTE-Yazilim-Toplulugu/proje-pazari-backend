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
    CANCELLED
}
