package com.iyte_yazilim.proje_pazari.application.commands.updateProject;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.UpdateProjectCommandResult;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UpdateProjectCommand(
        @NotBlank(message = "Project ID is required") String projectId,
        @NotBlank(message = "Owner ID is required") String ownerId,
        @Size(min = 3, max = 100, message = "Project name must be between 3 and 100 characters")
                String projectName,
        @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
                String description,
        String summary,
        @Min(value = 1, message = "Maximum team size must be at least 1") Integer maxTeamSize,
        String[] requiredSkills,
        String category,
        LocalDateTime deadline)
        implements ICommand<ApiResponse<UpdateProjectCommandResult>> {}
