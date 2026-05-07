package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;

/**
 * Thrown when a {@link com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication} workflow
 * method is invoked from an invalid current status.
 *
 * <p>For example, calling {@code approve()} on an application whose status is {@link
 * ApplicationStatus#REJECTED}.
 *
 * @author IYTE Yazılım Topluluğu
 * @since 2026-04-04
 */
public class IllegalApplicationStateException extends IllegalStateException {

    public IllegalApplicationStateException(String action, ApplicationStatus currentStatus) {
        super(
                String.format(
                        "Cannot %s application: current status is %s (expected PENDING)",
                        action, currentStatus));
    }
}
