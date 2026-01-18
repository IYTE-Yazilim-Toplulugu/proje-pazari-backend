package com.iyte_yazilim.proje_pazari.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the status of a project application in the review process.
 *
 * <p>Application status flow:
 *
 * <pre>
 * PENDING → APPROVED
 *        ↘ REJECTED
 * </pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication
 */
@Getter
@AllArgsConstructor
public enum ApplicationStatus {
    /** Application is awaiting review by project owner. */
    PENDING("pending"),

    /** Application has been approved; user is now part of the project. */
    APPROVED("approved"),

    /** Application has been rejected by the project owner. */
    REJECTED("rejected");

    /** String representation of the status for serialization. */
    private final String status;
}
