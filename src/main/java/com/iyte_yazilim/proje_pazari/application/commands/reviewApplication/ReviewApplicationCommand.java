package com.iyte_yazilim.proje_pazari.application.commands.reviewApplication;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Command to review (approve or reject) a project application.
 *
 * <p>This command initiates the application review process which includes:
 *
 * <ul>
 *   <li>Validation of application existence
 *   <li>Status update (APPROVED or REJECTED)
 *   <li>Event publishing for email notifications
 * </ul>
 *
 * @param applicationId ULID of the application to review
 * @param status The new status (APPROVED or REJECTED)
 * @param reviewMessage Optional message to include in the review
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-02-01
 * @see ReviewApplicationHandler
 * @see com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult
 */
@Schema(description = "Command to review a project application")
public record ReviewApplicationCommand(
        @Schema(hidden = true) String applicationId,
        @Schema(description = "New status for the application (APPROVED or REJECTED)")
                @NotNull(message = "Status is required")
                ApplicationStatus status,
        @Schema(description = "Optional review message", example = "Great experience!")
                String reviewMessage)
        implements ICommand<ApiResponse<ReviewApplicationCommandResult>> {}
