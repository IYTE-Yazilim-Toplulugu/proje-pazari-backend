package com.iyte_yazilim.proje_pazari.application.commands.updateProject;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "Command to update an existing project")
public record UpdateProjectCommand(
        @Schema(hidden = true) String projectId,
        @Schema(hidden = true) String ownerId,
        @Schema(description = "Updated project title", example = "Improved AI Chatbot")
        @NotBlank(message = "Project name is required")
        @Size(
                min = 3,
                max = 100,
                message = "Project name must be between 3 and 100 characters")
        String projectName,
        @Schema(
                description = "Updated project description",
                example =
                        "An improved AI chatbot leveraging the latest LLM models for"
                                + " campus support and FAQ automation.")
        @Size(
                min = 10,
                max = 2000,
                message = "Description must be between 10 and 2000 characters")
        String description,
        @Schema(
                description = "Updated brief summary of the project for listing displays",
                example = "AI chatbot for campus support using state-of-the-art LLMs")
        @NotBlank(message = "Summary is required")
        @Size(
                min = 3,
                max = 250,
                message = "Summary must be between 3 and 250 characters")
        String summary,
        @Schema(description = "Updated maximum team size", example = "8") Integer maxTeamSize,
        @Schema(
                description = "Updated required skills",
                example = "[\"Python\", \"LangChain\", \"FastAPI\"]")
        String[] requiredSkills,
        @Schema(description = "Updated category", example = "Machine Learning") String category,
        @Schema(description = "Updated deadline") LocalDateTime deadline)
        implements ICommand<ApiResponse<ProjectDetailDto>> {}