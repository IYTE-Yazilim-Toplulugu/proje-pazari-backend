package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.reviewApplication.ReviewApplicationCommand;
import com.iyte_yazilim.proje_pazari.application.commands.submitApplication.SubmitApplicationCommand;
import com.iyte_yazilim.proje_pazari.application.commands.withdrawApplication.WithdrawApplicationCommand;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationDto;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedApplicationsResult;
import com.iyte_yazilim.proje_pazari.application.queries.getProjectApplications.GetProjectApplicationsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getUserApplications.GetUserApplicationsQuery;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.domain.models.results.SubmitApplicationCommandResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(
        name = "Applications",
        description =
                "Project application endpoints for submitting, reviewing, and withdrawing applications.")
public class ApplicationController extends BaseController {

    @PostMapping("/api/v1/projects/{projectId}/applications")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Apply to a project")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        description = "Application submitted successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description =
                                "The project owner cannot apply to their own project"
                                        + " (SELF_APPLICATION_NOT_ALLOWED)"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "409",
                        description =
                                "Submission is ineligible: APPLICATION_ALREADY_EXISTS,"
                                        + " PROJECT_NOT_OPEN, PROJECT_APPLICATION_DEADLINE_PASSED,"
                                        + " or PROJECT_FULL"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Project not found")
            })
    public ResponseEntity<ApiResponse<SubmitApplicationCommandResult>> applyToProject(
            @PathVariable String projectId, Authentication auth) {
        return send(
                SubmitApplicationCommand.class, Map.of("projectId", projectId), null, null, auth);
    }

    @GetMapping("/api/v1/projects/{projectId}/applications")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get applications for a project (owner only)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Applications retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Not the project owner"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Project not found")
            })
    public ResponseEntity<ApiResponse<List<ApplicationDto>>> getProjectApplications(
            @PathVariable String projectId,
            @RequestParam(required = false) ApplicationStatus status,
            Authentication auth) {
        String requesterId = getCurrentUserId(auth);
        return send(new GetProjectApplicationsQuery(projectId, requesterId, status));
    }

    @PutMapping("/api/v1/applications/{applicationId}/review")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Approve or reject an application")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Application reviewed successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Authenticated user does not own the application's project"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Application not found")
            })
    public ResponseEntity<ApiResponse<ReviewApplicationCommandResult>> reviewApplication(
            @PathVariable String applicationId,
            @RequestBody @Valid ReviewApplicationRequest request,
            Authentication auth) {
        return send(
                new ReviewApplicationCommand(
                        applicationId,
                        getCurrentUserId(auth),
                        RoleType.valueOf(getCurrentUserRole(auth)),
                        request.status(),
                        request.reviewMessage()));
    }

    @PatchMapping("/api/v1/applications/{applicationId}/withdraw")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Withdraw a pending application (soft status change to WITHDRAWN)")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Application withdrawn successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Application is not in PENDING status"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Not the application owner"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Application not found")
            })
    public ResponseEntity<ApiResponse<Void>> withdrawApplication(
            @PathVariable String applicationId, Authentication auth) {
        return send(
                WithdrawApplicationCommand.class,
                Map.of("applicationId", applicationId),
                null,
                null,
                auth);
    }

    @GetMapping("/api/v1/users/me/applications")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all applications submitted by the authenticated user")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Applications retrieved successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized")
            })
    public ResponseEntity<ApiResponse<PagedApplicationsResult>> getMyApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ApplicationStatus status,
            Authentication auth) {
        String userId = getCurrentUserId(auth);
        return send(new GetUserApplicationsQuery(userId, page, size, status));
    }

    @Schema(
            name = "ReviewApplicationRequest",
            description = "Project-owner application review request")
    public record ReviewApplicationRequest(
            @Schema(
                            description = "Review decision",
                            allowableValues = {"APPROVED", "REJECTED"},
                            example = "APPROVED")
                    @NotNull(message = "Status is required")
                    ApplicationStatus status,
            @Schema(
                            description = "Optional message persisted with the review",
                            example = "Great experience!")
                    String reviewMessage) {}
}
