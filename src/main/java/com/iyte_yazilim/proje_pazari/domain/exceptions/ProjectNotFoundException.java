package com.iyte_yazilim.proje_pazari.domain.exceptions;

/** ProjectNotFoundException */
public class ProjectNotFoundException extends RuntimeException {
    public ProjectNotFoundException(String projectId) {
        super("Project with ID " + projectId + " not found.");
    }
}
