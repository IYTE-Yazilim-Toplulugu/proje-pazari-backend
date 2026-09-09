package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

/**
 * Exception thrown when a project with the specified identifier cannot be found in the system.
 *
 * <p>This unchecked exception is typically used in service or repository layers when a lookup by
 * project ID fails. {@code GlobalExceptionHandler} translates it into an HTTP 404 response with the
 * {@code PROJECT_NOT_FOUND} error code; the technical message below is for server logs only.
 */
public class ProjectNotFoundException extends DomainException {
    public ProjectNotFoundException(String projectId) {
        super(ErrorCode.PROJECT_NOT_FOUND, "Project with ID " + projectId + " not found.");
    }
}
