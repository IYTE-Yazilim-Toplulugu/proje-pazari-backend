package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Command to create a new project in the marketplace.
 *
 * <p>This command initiates project creation which includes:
 *
 * <ul>
 *   <li>Validation of project details
 *   <li>Owner verification
 *   <li>Project entity creation
 *   <li>Initial status set to DRAFT
 * </ul>
 *
 * <h2>Example:</h2>
 *
 * <pre>{@code
 * CreateProjectCommand command = new CreateProjectCommand(
 *                 "Mobile App Development",
 *                 "Building a Flutter mobile app for campus navigation",
 *                 "01HQXYZ123",
 *                 new String[] {},
 *                 new String[] { "flutter", "mobile", "navigation" });
 * ApiResponse<CreateProjectCommandResult> response = handler.handle(command);
 * }</pre>
 *
 * @param projectName the title of the project (3-100 chars)
 * @param description detailed project description (10-2000 chars)
 * @param ownerId ULID of the project owner
 * @param teamMemberIds optional array of initial team member ULIDs
 * @param tags optional array of project tags for categorization
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see CreateProjectHandler
 * @see com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult
 */
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
                String[] tags,
        @Schema(description = "Maximum team size", example = "5")
                @Min(value = 1, message = "Maximum team size must be at least 1")
                Integer maxTeamSize,
        @Schema(
                        description = "Array of required skills",
                        example = "[\"Python\", \"Machine Learning\", \"TensorFlow\"]")
                String[] requiredSkills,
        @Schema(description = "Project category", example = "Machine Learning") String category,
        @Schema(description = "Project deadline", example = "2025-12-31T23:59:59")
                LocalDateTime deadline) {}
