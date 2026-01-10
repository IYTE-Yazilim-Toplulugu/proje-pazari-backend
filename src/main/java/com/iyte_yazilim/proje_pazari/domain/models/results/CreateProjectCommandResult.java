package com.iyte_yazilim.proje_pazari.domain.models.results;

import java.time.LocalDateTime;

public record CreateProjectCommandResult(
        String projectId,
        String projectName,
        String description,
        String ownerId,
        String[] teamMemberIds,
        String[] tags,
        Integer maxTeamSize,
        Integer currentTeamSize,
        String[] requiredSkills,
        String category,
        LocalDateTime deadline) {}
