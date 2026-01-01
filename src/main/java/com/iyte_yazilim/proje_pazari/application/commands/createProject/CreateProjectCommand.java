package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Command to create a new project")
public record CreateProjectCommand(
        @Schema(description = "Name of the project", example = "Machine Learning Research Platform")
                @NotBlank(message = "Project name is required")
                @Size(
                        min = 3,
                        max = 100,
                        message = "Project name must be between 3 and 100 characters")
                String projectName,
        @Schema(
                        description = "Detailed description of the project",
                        example = "A platform for collaborative ML research")
                @NotBlank(message = "Description is required")
                @Size(
                        min = 10,
                        max = 2000,
                        message = "Description must be between 10 and 2000 characters")
                String description,
        @Schema(description = "ID of the project owner", example = "01HQZX...")
                @NotBlank(message = "Owner ID is required")
                String ownerId,
        @Schema(
                        description = "Array of team member IDs",
                        example = "[\"01HQZX...\", \"01HQZY...\"]")
                String[] teamMemberIds,
        @Schema(
                        description = "Array of project tags",
                        example = "[\"machine-learning\", \"python\", \"research\"]")
                String[] tags) {}
