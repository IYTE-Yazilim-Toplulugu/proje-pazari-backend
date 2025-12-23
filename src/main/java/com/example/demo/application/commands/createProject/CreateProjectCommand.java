package com.example.demo.application.commands.createProject;

public record CreateProjectCommand(
        String projectName,
        String description,
        String ownerId,
        String[] teamMemberIds,
        String[] tags) {
}
