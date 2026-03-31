# Admin Dashboard API Reference

Complete API reference for the Admin Dashboard endpoints. All endpoints require the `ADMIN` role and Bearer token authentication.

## Table of Contents

- [User Management](#user-management)
- [Project Management](#project-management)
- [Application Management](#application-management)
- [Statistics & Analytics](#statistics--analytics)
- [Audit Logging](#audit-logging)
- [Content Moderation](#content-moderation)
- [Email & Notifications](#email--notifications)
- [Data Export](#data-export)
- [System Health](#system-health)
- [Configuration Management](#configuration-management)
- [Security Considerations](#security-considerations)

---

## User Management

### List Users
```
GET /api/v1/admin/users?page=0&size=50&role=APPLICANT&isActive=true&search=john
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| page | int | No (default: 0) | Page number |
| size | int | No (default: 50) | Page size |
| role | RoleType | No | Filter by role (APPLICANT, PROJECT_OWNER, ADMIN) |
| isActive | Boolean | No | Filter by active status |
| search | String | No | Search by name or email |

### Get User Details
```
GET /api/v1/admin/users/{userId}
```
Returns detailed user information including project count and application count.

### Update User
```
PUT /api/v1/admin/users/{userId}
```
```json
{
  "role": "PROJECT_OWNER",
  "isActive": true,
  "firstName": "John",
  "lastName": "Doe",
  "description": "Updated bio"
}
```
All fields are optional — only provided fields will be updated.

### Delete User (Soft Delete)
```
DELETE /api/v1/admin/users/{userId}
```
Deactivates the user (soft delete). Sets `isActive` to `false`.

### Bulk User Action
```
POST /api/v1/admin/users/bulk-action
```
```json
{
  "action": "SUSPEND",
  "userIds": ["user-1", "user-2", "user-3"]
}
```
**Supported actions:** `DELETE`, `SUSPEND`, `ACTIVATE`, `CHANGE_ROLE`

### Promote to Project Owner
```
POST /api/v1/admin/users/{userId}/promote-to-project-owner
```

---

## Project Management

### List Projects
```
GET /api/v1/admin/projects?page=0&size=50&status=OPEN&ownerId=xxx&search=flutter
```

### Delete Project
```
DELETE /api/v1/admin/projects/{projectId}
```
Cascades delete to related data.

### Feature/Unfeature Project
```
PATCH /api/v1/admin/projects/{projectId}/feature?featured=true
```

### Bulk Project Action
```
POST /api/v1/admin/projects/bulk-action
```
```json
{
  "action": "DELETE",
  "projectIds": ["proj-1", "proj-2"]
}
```
**Supported actions:** `DELETE`, `FEATURE`, `UNFEATURE`, `CANCEL`

---

## Application Management

### List Applications
```
GET /api/v1/admin/applications?page=0&size=50&status=PENDING&projectId=xxx&userId=yyy
```

### Review Application
```
PUT /api/v1/admin/applications/{applicationId}/review
```
```json
{
  "status": "ACCEPTED"
}
```

### Bulk Application Action
```
POST /api/v1/admin/applications/bulk-action
```
```json
{
  "action": "APPROVE",
  "applicationIds": ["app-1", "app-2"]
}
```

---

## Statistics & Analytics

### System Overview
```
GET /api/v1/admin/stats/overview
```
Returns: `totalUsers`, `activeProjects`, `pendingApplications`, `newUsersToday`, `newUsersThisWeek`, `newUsersThisMonth`, `totalProjects`, `totalApplications`, `usersByRole`

### User Statistics
```
GET /api/v1/admin/stats/users
```
Returns user growth trends, role distribution, email domain breakdown, and verification rates.

### Project Statistics
```
GET /api/v1/admin/stats/projects
```
Returns projects by status, creation trends, and average applications per project.

### Application Statistics
```
GET /api/v1/admin/stats/applications
```
Returns application status breakdown, acceptance rates, and trends.

---

## Audit Logging

All admin actions annotated with `@Audited` are automatically logged via the `AuditAspect`.

### Query Audit Logs
```
GET /api/v1/admin/audit-logs?page=0&size=50&action=ADMIN_DELETE_USER&performedBy=xxx&entityType=USER&entityId=yyy
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| action | String | No | Filter by action type |
| performedBy | String | No | Filter by admin user ID |
| entityType | String | No | Filter by entity type (USER, PROJECT, etc.) |
| entityId | String | No | Filter by entity ID |

---

## Content Moderation

### Flag Content
```
POST /api/v1/admin/content/flag/{type}/{id}
```
```json
{
  "reason": "SPAM"
}
```
**Types:** `USER`, `PROJECT`, `APPLICATION`
**Reasons:** `SPAM`, `INAPPROPRIATE`, `OFFENSIVE`

### List Flagged Content
```
GET /api/v1/admin/content/flagged?page=0&size=50&status=PENDING&contentType=PROJECT
```

### Review Flagged Content
```
POST /api/v1/admin/content/review/{flagId}
```
```json
{
  "action": "REMOVE",
  "reviewNote": "Contains inappropriate content"
}
```
**Actions:** `APPROVE`, `REMOVE`, `BAN_USER`

---

## Email & Notifications

### Broadcast Email
```
POST /api/v1/admin/email/broadcast
```
```json
{
  "subject": "Platform Update",
  "body": "Hello! We have exciting news...",
  "targetRole": "ALL"
}
```
**Target roles:** `ALL`, `APPLICANT`, `PROJECT_OWNER`, `ADMIN`

### Send Targeted Email
```
POST /api/v1/admin/email/targeted
```
```json
{
  "subject": "Account Notice",
  "body": "Dear user, ...",
  "userIds": ["user-1", "user-2"]
}
```

---

## Data Export

### Export Users to CSV
```
GET /api/v1/admin/export/users
```
Returns: CSV file download with columns `id, email, firstName, lastName, role, isActive, createdAt, updatedAt`

### Export Projects to CSV
```
GET /api/v1/admin/export/projects
```

### Export Applications to CSV
```
GET /api/v1/admin/export/applications
```

---

## System Health

### Service Health Check
```
GET /api/v1/admin/health/services
```
```json
{
  "status": "HEALTHY",
  "services": {
    "database": "UP"
  },
  "memory": {
    "totalMB": 512,
    "freeMB": 256,
    "usedMB": 256,
    "maxMB": 1024
  },
  "uptime": "2d 5h 30m 15s"
}
```

---

## Configuration Management

### Get System Configuration
```
GET /api/v1/admin/config
```
Returns all key-value configuration entries.

### Update System Configuration
```
PUT /api/v1/admin/config
```
```json
{
  "maintenanceMode": "false",
  "registrationEnabled": "true",
  "emailVerificationRequired": "true",
  "maxProjectsPerUser": "10"
}
```

---

## Security Considerations

> **⚠️ Important:** All admin endpoints are protected by `@PreAuthorize("hasRole('ADMIN')")` at the controller class level.

- **Authentication:** All requests require a valid JWT Bearer token in the `Authorization` header
- **Authorization:** Only users with the `ADMIN` role can access these endpoints. Non-admin users receive a `403 Forbidden` response.
- **Audit Trail:** All destructive operations (delete, update, bulk actions) are automatically logged via the `@Audited` annotation and `AuditAspect`
- **Soft Delete:** User deletion performs a soft delete (deactivation) rather than hard delete, preserving data integrity
- **Input Validation:** All inputs are validated before processing. Invalid inputs return `400 Bad Request` with descriptive error messages
- **Rate Limiting:** Auth endpoints have rate limiting configured via `RateLimitConfig`

## Best Practices

1. **Use bulk operations** for batch updates instead of calling individual endpoints repeatedly
2. **Check audit logs** regularly to monitor admin actions
3. **Export data** periodically for backup purposes
4. **Monitor system health** to ensure all services are running
5. **Flag and review** inappropriate content promptly
6. **Use targeted emails** for specific user communication rather than broadcasting
