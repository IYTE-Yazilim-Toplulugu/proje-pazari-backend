# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- **Authentication System**
  - User registration with email and password
  - JWT-based authentication
  - Login and token generation
  - Password hashing with BCrypt

- **User Management**
  - User profile CRUD operations
  - Profile picture upload
  - Password change functionality
  - Account deactivation

- **Project Management**
  - Project creation with details (title, description, skills, etc.)
  - Project status management (DRAFT, OPEN, IN_PROGRESS, COMPLETED, CANCELLED)
  - Project owner assignment

- **Application System**
  - Apply to projects
  - Application status tracking (PENDING, APPROVED, REJECTED, WITHDRAWN)
  - Application review by project owner

- **Infrastructure**
  - PostgreSQL 16 database integration
  - Docker and Docker Compose support
  - Swagger/OpenAPI documentation
  - Health check endpoints

- **Code Quality**
  - JaCoCo test coverage (70% threshold)
  - Spotless code formatting (Google Java Format)
  - Clean Architecture implementation
  - CQRS pattern for commands and queries

- **Documentation**
  - Comprehensive README.md
  - Contributing guidelines
  - API documentation
  - Architecture documentation
  - Deployment guide
  - Security documentation
  - Troubleshooting guide

### Changed

- Upgraded to Spring Boot 4.0.0
- Using Java 21 features

### Fixed

- ProjectStatus default initialization issue

### Security

- JWT token validation on protected endpoints
- BCrypt password encryption
- Input validation with Jakarta Bean Validation

---

## [0.1.0] - 2024-12-01

### Added

- Initial project setup
- Spring Boot 4.0.0 configuration
- Basic project structure following Clean Architecture
- Gradle build configuration
- Docker support

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| Unreleased | - | Current development |
| 0.1.0 | 2024-12-01 | Initial release |

---

## Upgrade Notes

### From 0.1.0 to Unreleased

No breaking changes. New features are additive.

---

## Links

- [GitHub Repository](https://github.com/IYTE-Yazilim-Toplulugu/proje-pazari-backend)
- [Project Board](https://github.com/orgs/IYTE-Yazilim-Toplulugu/projects/23)
- [Issue Tracker](https://github.com/IYTE-Yazilim-Toplulugu/proje-pazari-backend/issues)
