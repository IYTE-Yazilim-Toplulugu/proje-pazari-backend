# API Documentation

> [!NOTE]
> Full interactive API documentation is available at `/swagger-ui.html` when running the application.

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

---

## Authentication Endpoints

### Register User

Creates a new user account.

```http
POST /auth/register
```

**Request Body:**
```json
{
  "email": "student@std.iyte.edu.tr",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Success Response (201 Created):**
```json
{
  "code": "CREATED",
  "message": "User registered successfully",
  "data": {
    "userId": "01HQXV5KXBW9FYMN8CJZSP2R4G",
    "email": "student@std.iyte.edu.tr",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "code": "BAD_REQUEST",
  "message": "Email already exists",
  "data": null
}
```

---

### Login

Authenticates a user and returns a JWT token.

```http
POST /auth/login
```

**Request Body:**
```json
{
  "email": "student@std.iyte.edu.tr",
  "password": "SecurePass123!"
}
```

**Success Response (200 OK):**
```json
{
  "code": "SUCCESS",
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "code": "BAD_REQUEST",
  "message": "Invalid credentials",
  "data": null
}
```

---

## User Endpoints

### Get All Users

Returns a list of all users. Requires authentication.

```http
GET /users
```

**Success Response (200 OK):**
```json
{
  "code": "SUCCESS",
  "message": "Users retrieved successfully",
  "data": [
    {
      "userId": "01HQXV5KXBW9FYMN8CJZSP2R4G",
      "email": "student@std.iyte.edu.tr",
      "firstName": "John",
      "lastName": "Doe",
      "description": "Computer Science student",
      "profilePictureUrl": "/uploads/profile-picture.jpg",
      "linkedinUrl": "https://linkedin.com/in/johndoe",
      "githubUrl": "https://github.com/johndoe"
    }
  ]
}
```

---

### Get Current User Profile

Returns the authenticated user's profile.

```http
GET /users/me
```

**Headers:**
```http
Authorization: Bearer <token>
```

**Success Response (200 OK):**
```json
{
  "code": "SUCCESS",
  "message": "Profile retrieved successfully",
  "data": {
    "userId": "01HQXV5KXBW9FYMN8CJZSP2R4G",
    "email": "student@std.iyte.edu.tr",
    "firstName": "John",
    "lastName": "Doe",
    "description": "Computer Science student",
    "profilePictureUrl": "/uploads/profile-picture.jpg",
    "linkedinUrl": "https://linkedin.com/in/johndoe",
    "githubUrl": "https://github.com/johndoe"
  }
}
```

---

### Get User by ID

Returns a specific user's public profile.

```http
GET /users/{userId}
```

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `userId` | string | ULID of the user |

---

### Update Profile

Updates the authenticated user's profile.

```http
PUT /users/me
```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "description": "Updated description",
  "linkedinUrl": "https://linkedin.com/in/johndoe",
  "githubUrl": "https://github.com/johndoe"
}
```

---

### Upload Profile Picture

Uploads a profile picture for the authenticated user.

```http
POST /users/me/profile-picture
```

**Request:**
- Content-Type: `multipart/form-data`
- Max file size: 5MB

---

### Change Password

Changes the authenticated user's password.

```http
PUT /users/me/password
```

**Request Body:**
```json
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewPass456!",
  "confirmPassword": "NewPass456!"
}
```

---

### Deactivate Account

Deactivates the authenticated user's account.

```http
DELETE /users/me
```

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `reason` | string | No | Reason for deactivation |

---

## Project Endpoints

### Create Project

Creates a new project.

```http
POST /projects
```

**Request Body:**
```json
{
  "title": "AI Chatbot Project",
  "description": "Building an AI-powered chatbot for customer support",
  "summary": "AI chatbot using NLP",
  "maxTeamSize": 5,
  "requiredSkills": ["Python", "NLP", "Machine Learning"],
  "category": "Artificial Intelligence",
  "deadline": "2025-06-15T23:59:59"
}
```

**Success Response (201 Created):**
```json
{
  "code": "CREATED",
  "message": "Project created successfully",
  "data": {
    "projectId": "01HQXV5KXBW9FYMN8CJZSP2R4H",
    "title": "AI Chatbot Project",
    "status": "DRAFT"
  }
}
```

---

### Get All Projects

Returns a list of all projects.

```http
GET /projects
```

---

### Get Project by ID

Returns a specific project.

```http
GET /projects/{id}
```

---

### Update Project

Updates a project. Only the owner can update.

```http
PUT /projects/{id}
```

---

### Delete Project

Deletes a project. Only the owner can delete.

```http
DELETE /projects/{id}
```

---

### Update Project Status

Updates the status of a project.

```http
PATCH /projects/{id}/status
```

**Request Body:**
```json
{
  "status": "OPEN"
}
```

**Project Status Values:**
| Status | Description |
|--------|-------------|
| `DRAFT` | Project is not yet published |
| `OPEN` | Project is open for applications |
| `IN_PROGRESS` | Project is actively being worked on |
| `COMPLETED` | Project has been completed |
| `CANCELLED` | Project has been cancelled |

---

### Search Projects

Searches for projects by keyword.

```http
GET /search/projects?q={keyword}
```

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | string | Yes | Search keyword |

---

## Application Endpoints

### Apply to Project

Submits an application to a project.

```http
POST /projects/{id}/applications
```

---

### Get Project Applications

Returns all applications for a project. Only the project owner can view.

```http
GET /projects/{id}/applications
```

---

### Review Application

Reviews (approve/reject) an application.

```http
PUT /applications/{id}/review
```

**Request Body:**
```json
{
  "status": "APPROVED",
  "message": "Welcome to the team!"
}
```

**Application Status Values:**
| Status | Description |
|--------|-------------|
| `PENDING` | Application is waiting for review |
| `APPROVED` | Application has been approved |
| `REJECTED` | Application has been rejected |
| `WITHDRAWN` | Applicant withdrew the application |

---

### Get My Applications

Returns all applications submitted by the authenticated user.

```http
GET /users/me/applications
```

---

### Withdraw Application

Withdraws an application.

```http
DELETE /applications/{id}
```

---

## Health Endpoints

### Health Check

```http
GET /health
```

**Success Response (200 OK):**
```json
{
  "status": "UP"
}
```

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `SUCCESS` | 200 | Request completed successfully |
| `CREATED` | 201 | Resource created successfully |
| `BAD_REQUEST` | 400 | Invalid request data |
| `UNAUTHORIZED` | 401 | Authentication required |
| `FORBIDDEN` | 403 | Insufficient permissions |
| `NOT_FOUND` | 404 | Resource not found |
| `INTERNAL_SERVER_ERROR` | 500 | Server error |

---

## Rate Limiting

Currently, there is no rate limiting implemented. This may be added in future versions.

---

## Pagination

Pagination support will be added in future versions. Currently, all results are returned in a single response.

---

## Additional Resources

- [Swagger UI](http://localhost:8080/swagger-ui.html) - Interactive API documentation
- [OpenAPI JSON](http://localhost:8080/v3/api-docs) - OpenAPI specification
