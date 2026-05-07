package com.iyte_yazilim.proje_pazari.domain.enums;

/**
 * Represents the status of a project application in the review process.
 *
 * <p>Application status flow:
 *
 * <pre>
 * PENDING → APPROVED
 *        ↘ REJECTED
 *        ↘ WITHDRAWN
 * </pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.1
 * @see com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication
 * @since 2024-01-01
 */
public enum ApplicationStatus {
    /** Application is awaiting review by project owner. */
    PENDING("pending"),

    /** Application has been approved; user is now part of the project. */
    APPROVED("approved"),

    /** Application has been rejected by the project owner. */
    REJECTED("rejected"),

    /** Application has been withdrawn by the applicant. */
    WITHDRAWN("withdrawn");

    /** String representation of the status for serialization. */
    private final String status;

    private ApplicationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }
}
