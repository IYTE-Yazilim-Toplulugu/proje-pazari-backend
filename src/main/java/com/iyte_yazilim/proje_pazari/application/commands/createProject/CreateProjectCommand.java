package com.iyte_yazilim.proje_pazari.application.commands.createProject;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
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
 *                 "Flutter app for navigating the IYTE campus with real-time updates",
 *                 "01HQXYZ123",
 *                 5,
 *                 new String[] { "Flutter", "Dart", "Firebase" },
 *                 "Mobile Development",
 *                 LocalDateTime.of(2025, 6, 1, 0, 0));
 * ApiResponse<CreateProjectCommandResult> response = handler.handle(command);
 * }</pre>
 *
 * @param projectName the title of the project (3–100 chars)
 * @param description detailed project description (10–2000 chars)
 * @param summary brief summary of the project for listing displays (3–250 chars)
 * @param ownerId ULID of the project owner (resolved from authentication, not sent by client)
 * @param maxTeamSize maximum number of team members allowed (minimum 1)
 * @param requiredSkills optional array of skills required for the project
 * @param category optional category that the project belongs to
 * @param deadline optional deadline by which the project should be completed
 * @author IYTE Yazılım Topluluğu
 * @version 1.1
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
                example =
                        "A platform for collaborative ML research enabling teams to share"
                                + " datasets, run experiments, and publish results.")
        @NotBlank(message = "Description is required")
        @Size(
                min = 10,
                max = 2000,
                message = "Description must be between 10 and 2000 characters")
        String description,
        @Schema(
                description = "Brief summary of the project for listing displays",
                example = "Collaborative ML research platform for dataset sharing and experimentation")
        @NotBlank(message = "Summary is required")
        @Size(
                min = 3,
                max = 250,
                message = "Summary must be between 3 and 250 characters")
        String summary,
        @Schema(
                description =
                        "ID of the project owner (resolved from authentication, do not"
                                + " send)",
                accessMode = Schema.AccessMode.READ_ONLY,
                example = "01HQZX...")
        @NotBlank(message = "Owner ID is required")
        String ownerId,
        @Schema(description = "Maximum team size for the project", example = "5")
        @Min(value = 1, message = "Maximum team size must be at least 1")
        Integer maxTeamSize,
        @Schema(description = "Array of required skills", example = "[\"Java\", \"Spring Boot\"]")
        String[] requiredSkills,
        @Schema(description = "Project category", example = "Software Development") String category,
        @Schema(description = "Project deadline") LocalDateTime deadline)
        implements ICommand<ApiResponse<CreateProjectCommandResult>> {}