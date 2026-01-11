package com.iyte_yazilim.proje_pazari.domain.exceptions;

/**
 * Exception thrown when a project with the specified identifier cannot be found in the system.
 *
 * <p>This unchecked exception is typically used in service or repository layers when a lookup by
 * project ID fails. Callers are expected to catch this exception where appropriate and translate it
 * into a user-facing response, such as an HTTP 404 (Not Found) status or a "project not found"
 * message.
 */
public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String projectId) {
        super("Project with ID " + projectId + " not found.");
    }
}
