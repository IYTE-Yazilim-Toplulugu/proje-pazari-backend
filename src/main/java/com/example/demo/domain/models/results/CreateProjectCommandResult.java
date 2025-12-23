package com.example.demo.domain.models.results;

public record CreateProjectCommandResult(
        String projectId,
        String projectName,
        String description,
        String ownerId,
        String[] teamMemberIds,
        String[] tags) {
}
