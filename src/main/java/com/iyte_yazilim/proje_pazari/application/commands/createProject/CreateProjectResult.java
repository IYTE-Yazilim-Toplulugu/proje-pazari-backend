package com.iyte_yazilim.proje_pazari.application.commands.createProject;

public record CreateProjectResult(
        String projectId,
        String projectName,
        String description,
        String ownerId,
        String[] teamMemberIds,
        String[] tags) {}
