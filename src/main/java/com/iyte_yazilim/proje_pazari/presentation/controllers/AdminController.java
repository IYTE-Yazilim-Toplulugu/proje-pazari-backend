package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.adminDeleteProject.AdminDeleteProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.adminDeleteUser.AdminDeleteUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.adminFeatureProject.AdminFeatureProjectCommand;
import com.iyte_yazilim.proje_pazari.application.commands.adminReviewApplication.AdminReviewApplicationCommand;
import com.iyte_yazilim.proje_pazari.application.commands.adminUpdateUser.AdminUpdateUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.broadcastEmail.BroadcastEmailCommand;
import com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction.BulkApplicationActionCommand;
import com.iyte_yazilim.proje_pazari.application.commands.bulkProjectAction.BulkProjectActionCommand;
import com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction.BulkUserActionCommand;
import com.iyte_yazilim.proje_pazari.application.commands.flagContent.FlagContentCommand;
import com.iyte_yazilim.proje_pazari.application.commands.promoteToProjectOwner.PromoteToProjectOwnerCommand;
import com.iyte_yazilim.proje_pazari.application.commands.reviewFlaggedContent.ReviewFlaggedContentCommand;
import com.iyte_yazilim.proje_pazari.application.commands.sendTargetedEmail.SendTargetedEmailCommand;
import com.iyte_yazilim.proje_pazari.application.commands.toggleMaintenanceMode.ToggleMaintenanceModeCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateFeatureFlag.UpdateFeatureFlagCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateSystemConfig.UpdateSystemConfigCommand;
import com.iyte_yazilim.proje_pazari.application.common.Audited;
import com.iyte_yazilim.proje_pazari.application.dtos.ActiveSessionDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationAdminDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationStatsDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.AuditLogDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.application.dtos.FeatureFlagDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.FlaggedContentDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectAdminDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectStatsDTO;
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
import com.iyte_yazilim.proje_pazari.application.queries.getApplicationStats.GetApplicationStatsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getAuditLogs.GetAuditLogsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getFeatureFlags.GetFeatureFlagsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getFlaggedContent.GetFlaggedContentQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getMaintenanceStatus.GetMaintenanceStatusQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getProjectStats.GetProjectStatsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemConfig.GetSystemConfigQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemHealth.GetSystemHealthQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemOverview.GetSystemOverviewQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getUserStats.GetUserStatsQuery;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    @Operation(
            summary = "List all users",
            description = "List all users with pagination and filters")
    public ResponseEntity<ApiResponse<PagedResponse<UserAdminDTO>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) RoleType role,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search) {
        return send(new AdminListUsersQuery(page, size, role, isActive, search));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user details", description = "Get detailed user information by ID")
    public ResponseEntity<ApiResponse<UserAdminDTO>> getUser(@PathVariable String userId) {
        return send(new AdminGetUserQuery(userId));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "Update user", description = "Update user role, status, or profile")
    @Audited(action = "ADMIN_UPDATE_USER", entityType = "USER")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @PathVariable String userId, @RequestBody Map<String, Object> updates) {
        RoleType role =
                updates.containsKey("role") ? RoleType.valueOf((String) updates.get("role")) : null;
        Boolean isActive =
                updates.containsKey("isActive") ? (Boolean) updates.get("isActive") : null;
        String firstName = (String) updates.get("firstName");
        String lastName = (String) updates.get("lastName");
        String description = (String) updates.get("description");

        return send(
                new AdminUpdateUserCommand(
                        userId, role, isActive, firstName, lastName, description));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user", description = "Soft delete user account (deactivate)")
    @Audited(action = "ADMIN_DELETE_USER", entityType = "USER")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        return send(new AdminDeleteUserCommand(userId));
    }

    @PostMapping("/users/bulk-action")
    @Operation(
            summary = "Bulk user action",
            description =
                    "Perform bulk operations on users (DELETE, SUSPEND, ACTIVATE, CHANGE_ROLE)")
    @Audited(action = "BULK_USER_ACTION", entityType = "USER")
    public ResponseEntity<ApiResponse<BulkActionResult>> bulkUserAction(
            @RequestBody Map<String, Object> request) {
        String action = (String) request.get("action");
        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) request.get("userIds");
        return send(new BulkUserActionCommand(action, userIds));
    }

    @PostMapping("/users/{userId}/promote-to-project-owner")
    @Operation(
            summary = "Promote user to PROJECT_OWNER",
            description = "Promotes a user to the PROJECT_OWNER role. Requires ADMIN role.")
    @Audited(action = "PROMOTE_TO_PROJECT_OWNER", entityType = "USER")
    public ResponseEntity<ApiResponse<Void>> promoteToProjectOwner(@PathVariable String userId) {
        return send(new PromoteToProjectOwnerCommand(userId));
    }

    // ==================== PROJECT MANAGEMENT ====================

    @GetMapping("/projects")
    @Operation(
            summary = "List all projects",
            description = "List all projects with pagination and filters")
    public ResponseEntity<ApiResponse<PagedResponse<ProjectAdminDTO>>> listProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) String search) {
        return send(new AdminListProjectsQuery(page, size, status, ownerId, search));
    }

    @DeleteMapping("/projects/{projectId}")
    @Operation(summary = "Delete project", description = "Delete any project with cascade")
    @Audited(action = "ADMIN_DELETE_PROJECT", entityType = "PROJECT")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable String projectId) {
        return send(new AdminDeleteProjectCommand(projectId));
    }

    @PatchMapping("/projects/{projectId}/feature")
    @Operation(
            summary = "Feature/unfeature project",
            description = "Toggle featured status on a project")
    @Audited(action = "ADMIN_FEATURE_PROJECT", entityType = "PROJECT")
    public ResponseEntity<ApiResponse<Void>> featureProject(
            @PathVariable String projectId, @RequestParam(defaultValue = "true") boolean featured) {
        return send(new AdminFeatureProjectCommand(projectId, featured));
    }

    @PostMapping("/projects/bulk-action")
    @Operation(
            summary = "Bulk project action",
            description =
                    "Perform bulk operations on projects (DELETE, FEATURE, UNFEATURE, CANCEL)")
    @Audited(action = "BULK_PROJECT_ACTION", entityType = "PROJECT")
    public ResponseEntity<ApiResponse<BulkActionResult>> bulkProjectAction(
            @RequestBody Map<String, Object> request) {
        String action = (String) request.get("action");
        @SuppressWarnings("unchecked")
        List<String> projectIds = (List<String>) request.get("projectIds");
        return send(new BulkProjectActionCommand(action, projectIds));
    }

    // ==================== APPLICATION MANAGEMENT ====================

    @GetMapping("/applications")
    @Operation(
            summary = "List all applications",
            description = "List all applications with pagination and filters")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationAdminDTO>>> listApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) String userId) {
        return send(new AdminListApplicationsQuery(page, size, status, projectId, userId));
    }

    @PutMapping("/applications/{applicationId}/review")
    @Operation(
            summary = "Review application",
            description = "Admin review of any application (approve/reject)")
    @Audited(action = "ADMIN_REVIEW_APPLICATION", entityType = "APPLICATION")
    public ResponseEntity<ApiResponse<Void>> reviewApplication(
            @PathVariable String applicationId, @RequestBody Map<String, String> request) {
        ApplicationStatus applicationStatus =
                ApplicationStatus.valueOf(request.get("status").toUpperCase());
        return send(new AdminReviewApplicationCommand(applicationId, applicationStatus));
    }

    @PostMapping("/applications/bulk-action")
    @Operation(
            summary = "Bulk application action",
            description = "Bulk approve/reject applications")
    @Audited(action = "BULK_APPLICATION_ACTION", entityType = "APPLICATION")
    public ResponseEntity<ApiResponse<BulkActionResult>> bulkApplicationAction(
            @RequestBody Map<String, Object> request) {
        String action = (String) request.get("action");
        @SuppressWarnings("unchecked")
        List<String> applicationIds = (List<String>) request.get("applicationIds");
        return send(new BulkApplicationActionCommand(action, applicationIds));
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

    // ==================== AUDIT LOGS ====================

    @GetMapping("/audit-logs")
    @Operation(summary = "Query audit logs", description = "Query audit logs with filters")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogDTO>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String entityId) {
        return send(new GetAuditLogsQuery(page, size, action, performedBy, entityType, entityId));
    }

    // ==================== CONTENT MODERATION ====================

    @PostMapping("/content/flag/{type}/{id}")
    @Operation(
            summary = "Flag content",
            description = "Flag inappropriate content (USER, PROJECT, APPLICATION)")
    @Audited(action = "FLAG_CONTENT", entityType = "CONTENT")
    public ResponseEntity<ApiResponse<Void>> flagContent(
            @PathVariable String type,
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "INAPPROPRIATE");
        return send(new FlagContentCommand(type, id, reason));
    }

    @GetMapping("/content/flagged")
    @Operation(
            summary = "List flagged content",
            description = "List all flagged content with filters")
    public ResponseEntity<ApiResponse<PagedResponse<FlaggedContentDTO>>> getFlaggedContent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String contentType) {
        return send(new GetFlaggedContentQuery(page, size, status, contentType));
    }

    @PostMapping("/content/review/{flagId}")
    @Operation(
            summary = "Review flagged content",
            description = "Review flagged content (APPROVE, REMOVE, BAN_USER)")
    @Audited(action = "REVIEW_FLAGGED_CONTENT", entityType = "CONTENT")
    public ResponseEntity<ApiResponse<Void>> reviewFlaggedContent(
            @PathVariable String flagId, @RequestBody Map<String, String> request) {
        String reviewAction = request.getOrDefault("action", "APPROVE");
        String reviewNote = request.get("reviewNote");
        return send(new ReviewFlaggedContentCommand(flagId, reviewAction, reviewNote));
    }

    // ==================== EMAIL & NOTIFICATIONS ====================

    @PostMapping("/email/broadcast")
    @Operation(
            summary = "Broadcast email",
            description = "Send email to all users or users with a specific role")
    @Audited(action = "BROADCAST_EMAIL", entityType = "EMAIL")
    public ResponseEntity<ApiResponse<Void>> broadcastEmail(
            @RequestBody Map<String, String> request) {
        String subject = request.get("subject");
        String body = request.get("body");
        String targetRole = request.getOrDefault("targetRole", "ALL");
        return send(new BroadcastEmailCommand(subject, body, targetRole));
    }

    @PostMapping("/email/targeted")
    @Operation(summary = "Send targeted email", description = "Send email to specific users by ID")
    @Audited(action = "TARGETED_EMAIL", entityType = "EMAIL")
    public ResponseEntity<ApiResponse<Void>> sendTargetedEmail(
            @RequestBody Map<String, Object> request) {
        String subject = (String) request.get("subject");
        String body = (String) request.get("body");
        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) request.get("userIds");
        return send(new SendTargetedEmailCommand(userIds, subject, body));
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
            @RequestBody Map<String, String> configs) {
        return send(new UpdateSystemConfigCommand(configs));
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
            @PathVariable String key, @RequestBody Map<String, Object> request) {
        boolean enabled = request.containsKey("enabled") ? (Boolean) request.get("enabled") : false;
        String description = (String) request.get("description");
        return send(new UpdateFeatureFlagCommand(key, enabled, description));
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
            @RequestBody Map<String, Boolean> request) {
        boolean enabled = request.getOrDefault("enabled", false);
        return send(new ToggleMaintenanceModeCommand(enabled));
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
    public ResponseEntity<ApiResponse<Void>> invalidateUserSessions(@PathVariable String userId) {
        return send(
                new com.iyte_yazilim
                        .proje_pazari
                        .application
                        .commands
                        .invalidateUserSessions
                        .InvalidateUserSessionsCommand(userId));
    }

    @DeleteMapping("/sessions")
    @Operation(
            summary = "Invalidate all sessions",
            description = "Revoke all active sessions globally")
    @Audited(action = "INVALIDATE_ALL_SESSIONS", entityType = "SESSION")
    public ResponseEntity<ApiResponse<Void>> invalidateAllSessions() {
        return send(
                new com.iyte_yazilim
                        .proje_pazari
                        .application
                        .commands
                        .invalidateAllSessions
                        .InvalidateAllSessionsCommand());
    }
}
