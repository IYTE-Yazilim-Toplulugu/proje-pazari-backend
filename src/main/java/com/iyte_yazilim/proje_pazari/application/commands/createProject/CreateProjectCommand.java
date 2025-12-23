package com.iyte_yazilim.proje_pazari.application.commands.createProject;

public record CreateProjectCommand(
        String projectName,
        String description,
        String ownerId,
        String[] teamMemberIds,
        String[] tags) {
}
