package com.iyte_yazilim.proje_pazari.domain.models.results;

public record UpdateProjectCommandResult(
        String projectId, String projectName, String description, String status) {}
