package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.iyte_yazilim.proje_pazari.application.commands.adminDeleteProject.AdminDeleteProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.adminDeleteUser.AdminDeleteUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.adminFeatureProject.AdminFeatureProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.adminUpdateUser.AdminUpdateUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.banIp.BanIpCommand;
import com.iyte_yazilim.proje_pazari.application.commands.broadcastEmail.BroadcastEmailCommand;
import com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction.BulkApplicationActionCommand;
import com.iyte_yazilim.proje_pazari.application.commands.bulkProjectAction.BulkProjectActionCommand;
import com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction.BulkUserActionCommand;
import com.iyte_yazilim.proje_pazari.application.commands.cancelScheduledEmail.CancelScheduledEmailCommand;
import com.iyte_yazilim.proje_pazari.application.commands.flagContent.FlagContentCommand;
import com.iyte_yazilim.proje_pazari.application.commands.importProjectsFromCsv.ImportProjectsFromCsvCommand;
import com.iyte_yazilim.proje_pazari.application.commands.importUsersFromCsv.ImportUsersFromCsvCommand;
import com.iyte_yazilim.proje_pazari.application.commands.invalidateAllSessions.InvalidateAllSessionsCommand;
import com.iyte_yazilim.proje_pazari.application.commands.invalidateUserSessions.InvalidateUserSessionsCommand;
import com.iyte_yazilim.proje_pazari.application.commands.reviewApplication.ReviewApplicationCommand;
import com.iyte_yazilim.proje_pazari.application.commands.reviewFlaggedContent.ReviewFlaggedContentCommand;
import com.iyte_yazilim.proje_pazari.application.commands.scheduleEmail.ScheduleEmailCommand;
import com.iyte_yazilim.proje_pazari.application.commands.sendTargetedEmail.SendTargetedEmailCommand;
import com.iyte_yazilim.proje_pazari.application.commands.toggleMaintenanceMode.ToggleMaintenanceModeCommand;
import com.iyte_yazilim.proje_pazari.application.commands.unbanIp.UnbanIpCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateFeatureFlag.UpdateFeatureFlagCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateSystemConfig.UpdateSystemConfigCommand;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.Audited;
import com.iyte_yazilim.proje_pazari.application.common.IMediator;
import com.iyte_yazilim.proje_pazari.application.dtos.ActiveSessionDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.AnalyticsTrendsDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationAdminDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationStatsDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.AuditLogDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.BannedIpDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.application.dtos.FeatureFlagDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.FlaggedContentDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectAdminDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectStatsDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.ScheduledEmailDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.StorageHealthDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemConfigDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemHealthDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemOverviewDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.UserAdminDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.UserStatsDTO;
import com.iyte_yazilim.proje_pazari.application.queries.adminGetUser.AdminGetUserQuery;
import com.iyte_yazilim.proje_pazari.application.queries.adminListApplications.AdminListApplicationsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.adminListProjects.AdminListProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.adminListUsers.AdminListUsersQuery;
import com.iyte_yazilim.proje_pazari.application.queries.exportApplications.ExportApplicationsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.exportProjects.ExportProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.exportUsers.ExportUsersQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getActiveSessions.GetActiveSessionsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getAnalyticsTrends.GetAnalyticsTrendsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getApplicationStats.GetApplicationStatsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getAuditLogs.GetAuditLogsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getFeatureFlags.GetFeatureFlagsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getFlaggedContent.GetFlaggedContentQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getMaintenanceStatus.GetMaintenanceStatusQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getProjectStats.GetProjectStatsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getScheduledEmails.GetScheduledEmailsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemConfig.GetSystemConfigQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemHealth.GetSystemHealthQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemOverview.GetSystemOverviewQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getUserStats.GetUserStatsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.listBannedIps.ListBannedIpsQuery;
import com.iyte_yazilim.proje_pazari.application.services.StorageHealthService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.presentation.mappers.IRequestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(
        name = "Admin",
        description = "Admin-only endpoints for user and system management. Requires ADMIN role.")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController extends BaseController {

    private final StorageHealthService storageHealthService;

    public AdminController(
            IMediator mediator,
            IRequestMapper requestMapper,
            StorageHealthService storageHealthService) {
        this.mediator = mediator;
        this.requestMapper = requestMapper;
        this.storageHealthService = storageHealthService;
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    @Operation(
            summary = "List all users",
            description = "List all users with pagination and filters")
    public ResponseEntity<ApiResponse<PagedResponse<UserAdminDTO>>> listUsers(
            @Parameter(description = "Page number (zero-based)", example = "0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "Page size", example = "50") @RequestParam(defaultValue = "50")
                    int size,
            @Parameter(description = "Filter by role type", example = "STUDENT")
                    @RequestParam(required = false)
                    RoleType role,
            @Parameter(description = "Filter by active status") @RequestParam(required = false)
                    Boolean isActive,
            @Parameter(description = "Search by name or email", example = "john")
                    @RequestParam(required = false)
                    String search) {
        return send(new AdminListUsersQuery(page, size, role, isActive, search));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user details", description = "Get detailed user information by ID")
    public ResponseEntity<ApiResponse<UserAdminDTO>> getUser(
            @Parameter(
                            description = "User ID",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R4G")
                    @PathVariable
                    String userId) {
        return send(new AdminGetUserQuery(userId));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user", description = "Update user role, status, or profile")
    @Audited(action = "ADMIN_UPDATE_USER", entityType = "USER")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @Parameter(
                            description = "User ID",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R4G")
                    @PathVariable
                    String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return send(
                new AdminUpdateUserCommand(
                        userId,
                        request.role(),
                        request.isActive(),
                        request.firstName(),
                        request.lastName(),
                        request.description()));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user", description = "Soft delete user account (deactivate)")
    @Audited(action = "ADMIN_DELETE_USER", entityType = "USER")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(
                            description = "User ID",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R4G")
                    @PathVariable
                    String userId) {
        return send(new AdminDeleteUserCommand(userId));
    }

    @PostMapping("/users/bulk-action")
    @Operation(
            summary = "Bulk user action",
            description =
                    "Perform bulk operations on users (DELETE, SUSPEND, ACTIVATE, CHANGE_ROLE)")
    @Audited(action = "BULK_USER_ACTION", entityType = "USER")
    public ResponseEntity<ApiResponse<BulkActionResult>> bulkUserAction(
            @Valid @RequestBody BulkUserActionRequest request) {
        return send(new BulkUserActionCommand(request.action(), request.userIds()));
    }

    // ==================== PROJECT MANAGEMENT ====================

    @GetMapping("/projects")
    @Operation(
            summary = "List all projects",
            description = "List all projects with pagination and filters")
    public ResponseEntity<ApiResponse<PagedResponse<ProjectAdminDTO>>> listProjects(
            @Parameter(description = "Page number (zero-based)", example = "0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "Page size", example = "50") @RequestParam(defaultValue = "50")
                    int size,
            @Parameter(description = "Filter by project status", example = "OPEN")
                    @RequestParam(required = false)
                    ProjectStatus status,
            @Parameter(description = "Filter by owner ID", example = "01HQXV5KXBW9FYMN8CJZSP2R4G")
                    @RequestParam(required = false)
                    String ownerId,
            @Parameter(description = "Search by project name", example = "chatbot")
                    @RequestParam(required = false)
                    String search) {
        return send(new AdminListProjectsQuery(page, size, status, ownerId, search));
    }

    @DeleteMapping("/projects/{projectId}")
    @Operation(summary = "Delete project", description = "Delete any project with cascade")
    @Audited(action = "ADMIN_DELETE_PROJECT", entityType = "PROJECT")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @Parameter(
                            description = "Project ID",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R4H")
                    @PathVariable
                    String projectId) {
        return send(new AdminDeleteProjectCommand(projectId));
    }

    @PatchMapping("/projects/{projectId}/feature")
    @Operation(
            summary = "Feature/unfeature project",
            description = "Toggle featured status on a project")
    @Audited(action = "ADMIN_FEATURE_PROJECT", entityType = "PROJECT")
    public ResponseEntity<ApiResponse<Void>> featureProject(
            @Parameter(
                            description = "Project ID",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R4H")
                    @PathVariable
                    String projectId,
            @Parameter(description = "Whether to feature or unfeature the project")
                    @RequestParam(defaultValue = "true")
                    boolean featured) {
        return send(new AdminFeatureProjectCommand(projectId, featured));
    }

    @PostMapping("/projects/bulk-action")
    @Operation(
            summary = "Bulk project action",
            description =
                    "Perform bulk operations on projects (DELETE, FEATURE, UNFEATURE, CANCEL)")
    @Audited(action = "BULK_PROJECT_ACTION", entityType = "PROJECT")
    public ResponseEntity<ApiResponse<BulkActionResult>> bulkProjectAction(
            @Valid @RequestBody BulkProjectActionRequest request) {
        return send(new BulkProjectActionCommand(request.action(), request.projectIds()));
    }

    // ==================== APPLICATION MANAGEMENT ====================

    @GetMapping("/applications")
    @Operation(
            summary = "List all applications",
            description = "List all applications with pagination and filters")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationAdminDTO>>> listApplications(
            @Parameter(description = "Page number (zero-based)", example = "0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "Page size", example = "50") @RequestParam(defaultValue = "50")
                    int size,
            @Parameter(description = "Filter by application status", example = "PENDING")
                    @RequestParam(required = false)
                    ApplicationStatus status,
            @Parameter(description = "Filter by project ID", example = "01HQXV5KXBW9FYMN8CJZSP2R4H")
                    @RequestParam(required = false)
                    String projectId,
            @Parameter(
                            description = "Filter by applicant user ID",
                            example = "01HQXV5KXBW9FYMN8CJZSP2R4G")
                    @RequestParam(required = false)
                    String userId) {
        return send(new AdminListApplicationsQuery(page, size, status, projectId, userId));
    }

    @PutMapping("/applications/{applicationId}/review")
    @Operation(
            summary = "Review application",
            description =
                    "Admin review of any application. Uses the same approve/reject workflow and"
                            + " response contract as project-owner reviews.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Application reviewed successfully"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid review status or application state"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Application not found")
            })
    @Audited(action = "ADMIN_REVIEW_APPLICATION", entityType = "APPLICATION")
    public ResponseEntity<ApiResponse<ReviewApplicationCommandResult>> reviewApplication(
            @Parameter(
                            description = "Application ID",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R5A")
                    @PathVariable
                    String applicationId,
            @Valid @RequestBody ReviewApplicationRequest request) {
        return send(
                new ReviewApplicationCommand(
                        applicationId, request.status(), request.reviewMessage()));
    }

    @PostMapping("/applications/bulk-action")
    @Operation(
            summary = "Bulk application action",
            description = "Bulk approve/reject applications")
    @Audited(action = "BULK_APPLICATION_ACTION", entityType = "APPLICATION")
    public ResponseEntity<ApiResponse<BulkActionResult>> bulkApplicationAction(
            @Valid @RequestBody BulkApplicationActionRequest request) {
        return send(new BulkApplicationActionCommand(request.action(), request.applicationIds()));
    }

    // ==================== STATISTICS & ANALYTICS ====================

    @GetMapping("/stats/overview")
    @Operation(summary = "System overview", description = "Get system overview statistics")
    public ResponseEntity<ApiResponse<SystemOverviewDTO>> getOverview() {
        return send(new GetSystemOverviewQuery());
    }

    @GetMapping("/stats/users")
    @Operation(summary = "User statistics", description = "Get user statistics and distributions")
    public ResponseEntity<ApiResponse<UserStatsDTO>> getUserStats() {
        return send(new GetUserStatsQuery());
    }

    @GetMapping("/stats/projects")
    @Operation(
            summary = "Project statistics",
            description = "Get project statistics and distributions")
    public ResponseEntity<ApiResponse<ProjectStatsDTO>> getProjectStats() {
        return send(new GetProjectStatsQuery());
    }

    @GetMapping("/stats/applications")
    @Operation(
            summary = "Application statistics",
            description = "Get application statistics and acceptance rate")
    public ResponseEntity<ApiResponse<ApplicationStatsDTO>> getApplicationStats() {
        return send(new GetApplicationStatsQuery());
    }

    @GetMapping("/analytics/trends")
    @Operation(
            summary = "Analytics trends",
            description =
                    "Get time-series trend data for user growth, project creation, and application activity")
    public ResponseEntity<ApiResponse<AnalyticsTrendsDTO>> getAnalyticsTrends(
            @Parameter(description = "Number of days to look back", example = "30")
                    @RequestParam(defaultValue = "30")
                    int days) {
        return send(new GetAnalyticsTrendsQuery(days));
    }

    // ==================== AUDIT LOGS ====================

    @GetMapping("/audit-logs")
    @Operation(summary = "Query audit logs", description = "Query audit logs with filters")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogDTO>>> getAuditLogs(
            @Parameter(description = "Page number (zero-based)", example = "0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "Page size", example = "50") @RequestParam(defaultValue = "50")
                    int size,
            @Parameter(description = "Filter by action type", example = "ADMIN_UPDATE_USER")
                    @RequestParam(required = false)
                    String action,
            @Parameter(description = "Filter by user who performed the action")
                    @RequestParam(required = false)
                    String performedBy,
            @Parameter(description = "Filter by entity type", example = "USER")
                    @RequestParam(required = false)
                    String entityType,
            @Parameter(description = "Filter by entity ID") @RequestParam(required = false)
                    String entityId) {
        return send(new GetAuditLogsQuery(page, size, action, performedBy, entityType, entityId));
    }

    // ==================== CONTENT MODERATION ====================

    @PostMapping("/content/flag/{type}/{id}")
    @Operation(
            summary = "Flag content",
            description = "Flag inappropriate content (USER, PROJECT, APPLICATION)")
    @Audited(action = "FLAG_CONTENT", entityType = "CONTENT")
    public ResponseEntity<ApiResponse<Void>> flagContent(
            @Parameter(description = "Content type to flag", required = true, example = "PROJECT")
                    @PathVariable
                    String type,
            @Parameter(
                            description = "Content ID to flag",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R4H")
                    @PathVariable
                    String id,
            @Valid @RequestBody FlagContentRequest request) {
        return send(new FlagContentCommand(type, id, request.reason()));
    }

    @GetMapping("/content/flagged")
    @Operation(
            summary = "List flagged content",
            description = "List all flagged content with filters")
    public ResponseEntity<ApiResponse<PagedResponse<FlaggedContentDTO>>> getFlaggedContent(
            @Parameter(description = "Page number (zero-based)", example = "0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @Parameter(description = "Page size", example = "50") @RequestParam(defaultValue = "50")
                    int size,
            @Parameter(description = "Filter by flag status", example = "PENDING")
                    @RequestParam(required = false)
                    String status,
            @Parameter(description = "Filter by content type", example = "PROJECT")
                    @RequestParam(required = false)
                    String contentType) {
        return send(new GetFlaggedContentQuery(page, size, status, contentType));
    }

    @PostMapping("/content/review/{flagId}")
    @Operation(
            summary = "Review flagged content",
            description = "Review flagged content (APPROVE, REMOVE, BAN_USER)")
    @Audited(action = "REVIEW_FLAGGED_CONTENT", entityType = "CONTENT")
    public ResponseEntity<ApiResponse<Void>> reviewFlaggedContent(
            @Parameter(
                            description = "Flag ID to review",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R5B")
                    @PathVariable
                    String flagId,
            @Valid @RequestBody ReviewFlaggedContentRequest request) {
        return send(
                new ReviewFlaggedContentCommand(flagId, request.action(), request.reviewNote()));
    }

    // ==================== EMAIL & NOTIFICATIONS ====================

    @PostMapping("/email/broadcast")
    @Operation(
            summary = "Broadcast email",
            description = "Send email to all users or users with a specific role")
    @Audited(action = "BROADCAST_EMAIL", entityType = "EMAIL")
    public ResponseEntity<ApiResponse<Void>> broadcastEmail(
            @Valid @RequestBody BroadcastEmailRequest request) {
        return send(
                new BroadcastEmailCommand(request.subject(), request.body(), request.targetRole()));
    }

    @PostMapping("/email/targeted")
    @Operation(summary = "Send targeted email", description = "Send email to specific users by ID")
    @Audited(action = "TARGETED_EMAIL", entityType = "EMAIL")
    public ResponseEntity<ApiResponse<Void>> sendTargetedEmail(
            @Valid @RequestBody SendTargetedEmailRequest request) {
        return send(
                new SendTargetedEmailCommand(request.userIds(), request.subject(), request.body()));
    }

    // ==================== DATA EXPORT ====================

    @GetMapping("/export/users")
    @Operation(summary = "Export users to CSV", description = "Export all users as CSV file")
    @Audited(action = "EXPORT_USERS", entityType = "USER")
    public ResponseEntity<byte[]> exportUsers() {
        ApiResponse<String> result = mediator.send(new ExportUsersQuery());
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=users.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(result.getData().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @GetMapping("/export/projects")
    @Operation(summary = "Export projects to CSV", description = "Export all projects as CSV file")
    @Audited(action = "EXPORT_PROJECTS", entityType = "PROJECT")
    public ResponseEntity<byte[]> exportProjects() {
        ApiResponse<String> result = mediator.send(new ExportProjectsQuery());
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=projects.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(result.getData().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @GetMapping("/export/applications")
    @Operation(
            summary = "Export applications to CSV",
            description = "Export all applications as CSV file")
    @Audited(action = "EXPORT_APPLICATIONS", entityType = "APPLICATION")
    public ResponseEntity<byte[]> exportApplications() {
        ApiResponse<String> result = mediator.send(new ExportApplicationsQuery());
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=applications.csv")
                .header("Content-Type", "text/csv; charset=UTF-8")
                .body(result.getData().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // ==================== SYSTEM HEALTH ====================

    @GetMapping("/health/services")
    @Operation(
            summary = "System health check",
            description = "Check health status of all services (database, JVM memory, uptime)")
    public ResponseEntity<ApiResponse<SystemHealthDTO>> getSystemHealth() {
        return send(new GetSystemHealthQuery());
    }

    // ==================== CONFIGURATION MANAGEMENT ====================

    @GetMapping("/storage/health")
    @Operation(
            summary = "Storage health check",
            description = "Check file storage status, usage, and bucket visibility")
    public ResponseEntity<ApiResponse<StorageHealthDTO>> getStorageHealth() {
        StorageHealthDTO health = storageHealthService.getStorageHealth();
        return ResponseEntity.ok(
                ApiResponse.success(health, "Storage health retrieved successfully"));
    }

    // ==================== CONFIGURATION MANAGEMENT ====================

    @GetMapping("/config")
    @Operation(
            summary = "Get system configuration",
            description = "Get all system configuration entries")
    public ResponseEntity<ApiResponse<SystemConfigDTO>> getSystemConfig() {
        return send(new GetSystemConfigQuery());
    }

    @PutMapping("/config")
    @Operation(
            summary = "Update system configuration",
            description = "Update system configuration entries (key-value pairs)")
    @Audited(action = "UPDATE_SYSTEM_CONFIG", entityType = "SYSTEM")
    public ResponseEntity<ApiResponse<Void>> updateSystemConfig(
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        return send(new UpdateSystemConfigCommand(request.configs()));
    }

    // ==================== FEATURE FLAGS ====================

    @GetMapping("/feature-flags")
    @Operation(
            summary = "List feature flags",
            description = "Get all feature flags and their current status")
    public ResponseEntity<ApiResponse<List<FeatureFlagDTO>>> getFeatureFlags() {
        return send(new GetFeatureFlagsQuery());
    }

    @PutMapping("/feature-flags/{key}")
    @Operation(
            summary = "Update feature flag",
            description = "Create or update a feature flag by key")
    @Audited(action = "UPDATE_FEATURE_FLAG", entityType = "FEATURE_FLAG")
    public ResponseEntity<ApiResponse<Void>> updateFeatureFlag(
            @Parameter(
                            description = "Feature flag key",
                            required = true,
                            example = "ENABLE_NOTIFICATIONS")
                    @PathVariable
                    String key,
            @Valid @RequestBody UpdateFeatureFlagRequest request) {
        return send(new UpdateFeatureFlagCommand(key, request.enabled(), request.description()));
    }

    // ==================== MAINTENANCE MODE ====================

    @GetMapping("/maintenance")
    @Operation(
            summary = "Get maintenance status",
            description = "Get current maintenance mode status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMaintenanceStatus() {
        return send(new GetMaintenanceStatusQuery());
    }

    @PostMapping("/maintenance")
    @Operation(
            summary = "Toggle maintenance mode",
            description = "Enable or disable maintenance mode")
    @Audited(action = "TOGGLE_MAINTENANCE_MODE", entityType = "SYSTEM")
    public ResponseEntity<ApiResponse<Void>> toggleMaintenanceMode(
            @Valid @RequestBody ToggleMaintenanceModeRequest request) {
        return send(new ToggleMaintenanceModeCommand(request.enabled()));
    }

    // ==================== SESSION MANAGEMENT ====================

    @GetMapping("/sessions")
    @Operation(
            summary = "List active sessions",
            description = "Get all active user sessions grouped by user")
    public ResponseEntity<ApiResponse<List<ActiveSessionDTO>>> getActiveSessions() {
        return send(new GetActiveSessionsQuery());
    }

    @DeleteMapping("/sessions/{userId}")
    @Operation(
            summary = "Invalidate user sessions",
            description = "Revoke all sessions for a specific user")
    @Audited(action = "INVALIDATE_USER_SESSIONS", entityType = "SESSION")
    public ResponseEntity<ApiResponse<Void>> invalidateUserSessions(
            @Parameter(
                            description = "User ID whose sessions to invalidate",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R4G")
                    @PathVariable
                    String userId) {
        return send(new InvalidateUserSessionsCommand(userId));
    }

    @DeleteMapping("/sessions")
    @Operation(
            summary = "Invalidate all sessions",
            description = "Revoke all active sessions globally")
    @Audited(action = "INVALIDATE_ALL_SESSIONS", entityType = "SESSION")
    public ResponseEntity<ApiResponse<Void>> invalidateAllSessions() {
        return send(new InvalidateAllSessionsCommand());
    }

    // ==================== IP BAN MANAGEMENT ====================

    @PostMapping("/ip-bans")
    @Operation(
            summary = "Ban IP address",
            description = "Ban a specific IP address from accessing the platform")
    @Audited(action = "BAN_IP", entityType = "IP_BAN")
    public ResponseEntity<ApiResponse<Void>> banIp(@Valid @RequestBody BanIpRequest request) {
        return send(new BanIpCommand(request.ipAddress(), request.reason(), request.expiresAt()));
    }

    @DeleteMapping("/ip-bans/{ip}")
    @Operation(summary = "Unban IP address", description = "Remove an IP address from the ban list")
    @Audited(action = "UNBAN_IP", entityType = "IP_BAN")
    public ResponseEntity<ApiResponse<Void>> unbanIp(
            @Parameter(
                            description = "IP address to unban",
                            required = true,
                            example = "192.168.1.100")
                    @PathVariable
                    String ip) {
        return send(new UnbanIpCommand(ip));
    }

    @GetMapping("/ip-bans")
    @Operation(summary = "List banned IPs", description = "Get all currently banned IP addresses")
    public ResponseEntity<ApiResponse<List<BannedIpDTO>>> listBannedIps() {
        return send(new ListBannedIpsQuery());
    }

    // ==================== DATA IMPORT ====================

    @PostMapping(value = "/import/users", consumes = "multipart/form-data")
    @Operation(
            summary = "Import users from CSV",
            description =
                    "Import users from a CSV file. Expected columns: email,firstName,lastName,role,password")
    @Audited(action = "IMPORT_USERS_CSV", entityType = "USER")
    public ResponseEntity<ApiResponse<ImportResultDTO>> importUsers(
            @org.springframework.web.bind.annotation.RequestPart("file")
                    org.springframework.web.multipart.MultipartFile file) {
        return send(ImportUsersFromCsvCommand.class, null, null, null, null, Map.of("file", file));
    }

    @PostMapping(value = "/import/projects", consumes = "multipart/form-data")
    @Operation(
            summary = "Import projects from CSV",
            description =
                    "Import projects from a CSV file. Expected columns: title,description,ownerEmail,status,maxTeamSize,category,requiredSkills")
    @Audited(action = "IMPORT_PROJECTS_CSV", entityType = "PROJECT")
    public ResponseEntity<ApiResponse<ImportResultDTO>> importProjects(
            @org.springframework.web.bind.annotation.RequestPart("file")
                    org.springframework.web.multipart.MultipartFile file) {
        return send(
                ImportProjectsFromCsvCommand.class, null, null, null, null, Map.of("file", file));
    }

    // ==================== SCHEDULED EMAIL BROADCASTS ====================

    @PostMapping("/email/schedule")
    @Operation(
            summary = "Schedule email broadcast",
            description = "Schedule an email to be sent at a future date/time")
    @Audited(action = "SCHEDULE_EMAIL", entityType = "EMAIL")
    public ResponseEntity<ApiResponse<Void>> scheduleEmail(
            @Valid @RequestBody ScheduleEmailRequest request) {
        return send(
                new ScheduleEmailCommand(
                        request.subject(),
                        request.body(),
                        request.targetRole(),
                        request.scheduledAt()));
    }

    @GetMapping("/email/scheduled")
    @Operation(
            summary = "List scheduled emails",
            description = "Get all scheduled email broadcasts")
    public ResponseEntity<ApiResponse<List<ScheduledEmailDTO>>> getScheduledEmails() {
        return send(new GetScheduledEmailsQuery());
    }

    @DeleteMapping("/email/scheduled/{id}")
    @Operation(
            summary = "Cancel scheduled email",
            description = "Cancel a pending scheduled email broadcast")
    @Audited(action = "CANCEL_SCHEDULED_EMAIL", entityType = "EMAIL")
    public ResponseEntity<ApiResponse<Void>> cancelScheduledEmail(
            @Parameter(
                            description = "Scheduled email ID to cancel",
                            required = true,
                            example = "01HQXV5KXBW9FYMN8CJZSP2R5C")
                    @PathVariable
                    String id) {
        return send(new CancelScheduledEmailCommand(id));
    }

    public record UpdateUserRequest(
            RoleType role,
            Boolean isActive,
            String firstName,
            String lastName,
            String description) {}

    public record BulkUserActionRequest(String action, List<String> userIds) {}

    public record BulkProjectActionRequest(String action, List<String> projectIds) {}

    @Schema(
            name = "AdminReviewApplicationRequest",
            description = "Admin application review request")
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

    public record BulkApplicationActionRequest(String action, List<String> applicationIds) {}

    public record FlagContentRequest(String reason) {}

    public record ReviewFlaggedContentRequest(String action, String reviewNote) {}

    public record BroadcastEmailRequest(String subject, String body, String targetRole) {}

    public record SendTargetedEmailRequest(List<String> userIds, String subject, String body) {}

    public record UpdateSystemConfigRequest(Map<String, String> configs) {
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public UpdateSystemConfigRequest {}
    }

    public record UpdateFeatureFlagRequest(boolean enabled, String description) {}

    public record ToggleMaintenanceModeRequest(boolean enabled) {}

    public record BanIpRequest(String ipAddress, String reason, LocalDateTime expiresAt) {}

    public record ScheduleEmailRequest(
            String subject, String body, String targetRole, LocalDateTime scheduledAt) {}
}
