package com.iyte_yazilim.proje_pazari.application.commands.submitApplication;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.SubmitApplicationCommandResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Command to submit an application to join a project.
 *
 * <p>This command initiates the application submission process which includes:
 *
 * <ul>
 *   <li>Validation of project and user existence
 *   <li>Check for duplicate applications
 *   <li>Application entity creation with PENDING status
 *   <li>Event publishing for email notifications
 * </ul>
 *
 * @param projectId ULID of the project to apply for
 * @param userId ULID of the user submitting the application
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 * @see SubmitApplicationHandler
 * @see com.iyte_yazilim.proje_pazari.domain.models.results.SubmitApplicationCommandResult
 */
@Schema(description = "Command to submit an application to a project")
public record SubmitApplicationCommand(
        @Schema(description = "ID of the project to apply for", example = "01HQZX...")
                @NotBlank(message = "Project ID is required")
                String projectId,
        @Schema(description = "ID of the user submitting the application", example = "01HQZY...")
                @NotBlank(message = "User ID is required")
                String userId)
        implements ICommand<ApiResponse<SubmitApplicationCommandResult>> {}
