package com.iyte_yazilim.proje_pazari.application.commands.createProject;

public record CreateProjectCommandResult(
        String projectId,
        String projectName,
        String description,
        String ownerId,
        String[] teamMemberIds,
        String[] tags) {}
