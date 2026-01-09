package com.iyte_yazilim.proje_pazari.application.queries.SearchProjects;

import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;

public record SearchProjectsQuery(
        String keyword, // Search in title and description
        ProjectStatus status, // Filter by status
        String ownerId, // Filter by owner
        int page,
        int size) {}
