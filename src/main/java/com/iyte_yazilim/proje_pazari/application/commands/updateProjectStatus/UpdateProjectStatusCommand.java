package com.iyte_yazilim.proje_pazari.application.commands.updateProjectStatus;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.models.results.UpdateProjectStatusCommandResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Command to update a project's status.
 *
 * <p>This command initiates the project status update process which includes:
 *
 * <ul>
 *   <li>Validation of project existence
 *   <li>Status transition validation
 *   <li>Event publishing for email notifications to owner and team members
 * </ul>
 *
 * @param projectId ULID of the project to update
 * @param newStatus The new status for the project
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 * @see UpdateProjectStatusHandler
 * @see com.iyte_yazilim.proje_pazari.domain.models.results.UpdateProjectStatusCommandResult
 */
@Schema(description = "Command to update a project's status")
public record UpdateProjectStatusCommand(
        @Schema(description = "ID of the project to update", example = "01HQZX...")
                @NotBlank(message = "Project ID is required")
                String projectId,
        @Schema(description = "New status for the project") @NotNull(message = "Status is required")
                ProjectStatus newStatus)
        implements ICommand<ApiResponse<UpdateProjectStatusCommandResult>> {}
