# 🎓 IYTE Proje Pazarı - Backend

A Spring Boot backend application for IYTE Project Marketplace, where students collaborate on projects.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![CI](https://github.com/IYTE-Yazilim-Toplulugu/proje-pazari-backend/actions/workflows/dev-ci-cd.yml/badge.svg)](https://github.com/IYTE-Yazilim-Toplulugu/proje-pazari-backend/actions/workflows/dev-ci-cd.yml)

## 📋 Table of Contents

- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Development Workflow](#development-workflow)
- [API Documentation](#api-documentation)
- [Monitoring](#monitoring)
- [Testing](#testing)
- [Contributing](#contributing)

## 🛠 Tech Stack

- **Framework**: Spring Boot 4.0.0
- **Language**: Java 21
- **Database**: PostgreSQL 16
- **Build Tool**: Gradle
- **Authentication**: JWT (JJWT 0.12.6)
- **Validation**: Jakarta Bean Validation
- **ORM**: Spring Data JPA / Hibernate
- **Documentation**: Swagger/OpenAPI 3.0
- **ID Generation**: ULID
- **Mapping**: MapStruct 1.5.5
- **Code Quality**: Spotless (Google Java Format), JaCoCo (Coverage)

## ⚙️ Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21** or higher ([Download](https://adoptium.net/))
- **Docker & Docker Compose** ([Download](https://www.docker.com/get-started))
- **Gradle 8.x** (or use the wrapper `./gradlew`)
- **Git** ([Download](https://git-scm.com/))

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/IYTE-Yazilim-Toplulugu/proje-pazari-backend.git
cd proje-pazari-backend
```

### 2. Set Up Environment Variables

Create a `.env` file in the root directory (optional, for production):

```env
# Application
JWT_SECRET=your-secret-key-minimum-256-bits
JWT_EXPIRATION=86400000
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/proje_pazari_db
SPRING_DATASOURCE_USERNAME=yazilim
SPRING_DATASOURCE_PASSWORD=yazilim123
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# Monitoring — Grafana alerts (optional)
GF_SMTP_ENABLED=true
GF_SMTP_HOST=smtp.gmail.com:587
GF_SMTP_USER=your-email@gmail.com
GF_SMTP_PASSWORD=your-app-password
GF_SMTP_FROM_ADDRESS=grafana@proje-pazari.com
GF_ALERT_EMAIL_TO=admin@proje-pazari.com
GF_SLACK_WEBHOOK_URL=https://hooks.slack.com/services/xxx/yyy/zzz
GF_DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/xxx/yyy
GF_SERVER_ROOT_URL=http://localhost:3030
```

### 3. Start PostgreSQL with Docker

```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- Redis on port 6379
- MinIO API on port 9002 / Console on port 9003
- Elasticsearch on port 9200
- Application API on port 8080
- Prometheus on port 9090, Grafana on port 3030, Alertmanager on port 9093 (use `--profile monitoring` flag)
- PgAdmin on port 5050 (use `--profile tools` flag)

### 4. Run the Application

Using Gradle wrapper:

```bash
./gradlew bootRun
```

Or if you have Gradle installed:

```bash
gradle bootRun
```

The application will start on **http://localhost:8080**

### 5. File Storage (MinIO)

The application uses MinIO for file storage (profile pictures, project attachments). MinIO is S3-compatible and runs automatically with docker-compose.

**Local Development:**
- MinIO API: http://localhost:9002
- MinIO Console: http://localhost:9003
- Default credentials: `minioadmin` / `minioadmin123`
- Credentials can be overridden with:
  - `MINIO_ADMIN_USER`
  - `MINIO_ADMIN_PASSWORD`

**Auto-created Buckets (via `minio-setup`):**
- `proje-pazari-files`
- `proje-pazari-avatars`
- `proje-pazari-documents`
- `proje-pazari-backups`

**Default Bucket Policies:**
- `proje-pazari-avatars`: public read (anonymous download enabled)
- `proje-pazari-documents`: private
- `proje-pazari-files`: private
- `proje-pazari-backups`: private

**Storage Configuration:**
```properties
# In application.properties
storage.provider=minio
minio.url=http://localhost:9002
minio.bucket-name=proje-pazari-files
minio.avatars-bucket=proje-pazari-avatars
minio.documents-bucket=proje-pazari-documents
minio.backups-bucket=proje-pazari-backups
```

**Object Organization:**
```text
proje-pazari-avatars/users/{userId}/avatar.{ext}
proje-pazari-documents/projects/{projectId}/{documentId}.{ext}
proje-pazari-files/users/{userId}/profile/*
proje-pazari-files/projects/{projectId}/attachments/*
proje-pazari-files/temp/uploads/*
```

**Production:**
Set environment variables for your S3-compatible storage:
```bash
MINIO_URL=https://your-storage-endpoint
MINIO_ADMIN_USER=your-admin-user
MINIO_ADMIN_PASSWORD=your-admin-password
MINIO_APP_ACCESS_KEY=your-app-access-key
MINIO_APP_SECRET_KEY=your-app-secret-key
MINIO_CREATE_BACKEND_USER=true
MINIO_FILES_BUCKET=proje-pazari-files
MINIO_AVATARS_BUCKET=proje-pazari-avatars
MINIO_DOCUMENTS_BUCKET=proje-pazari-documents
MINIO_BACKUPS_BUCKET=proje-pazari-backups
SPRING_PROFILES_ACTIVE=prod
```

**Migrating from Local Storage:**
If you have existing files in the `./uploads` folder, run the migration script:
```bash
# Install MinIO client first
curl https://dl.min.io/client/mc/release/linux-amd64/mc -o mc
chmod +x mc && sudo mv mc /usr/local/bin/

# Run migration
./scripts/migrate-to-minio.sh
```

**Common MinIO `mc` Operations:**
```bash
# List buckets
mc ls myminio

# List files in files bucket
mc ls myminio/proje-pazari-files

# Upload a file
mc cp ./myfile.pdf myminio/proje-pazari-files/projects/proj-123/

# Download a file
mc cp myminio/proje-pazari-files/projects/proj-123/myfile.pdf ./

# Bucket usage
mc du myminio/proje-pazari-files
```

For full storage architecture, policies, backup, retention, and troubleshooting, see `STORAGE.md`.

**Monitoring Profile (Prometheus + Grafana):**
```bash
docker compose --profile monitoring up -d prometheus alertmanager grafana
```

- Prometheus UI: http://localhost:9090
- Alertmanager UI: http://localhost:9093
- Grafana UI: http://localhost:3030 (default `admin` / `admin`)

**Automated MinIO Backups (Daily by default):**
```bash
docker compose --profile maintenance up -d minio-backup-scheduler
```

- Uses `BACKUP_INTERVAL_SECONDS` (default `86400`)
- Writes timestamped backups under `./backups/minio/`

### 6. Access API Documentation

Open your browser and navigate to:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs JSON**: http://localhost:8080/v3/api-docs

## 📁 Project Structure

```
src/main/java/com/iyte_yazilim/proje_pazari/
├── presentation/          # Controllers, Security, Configuration
│   ├── controllers/       # REST API endpoints
│   ├── security/          # JWT, filters, security config
│   └── config/            # Application configuration
├── application/           # Application layer (CQRS)
│   ├── commands/          # Write operations
│   ├── queries/           # Read operations
│   ├── handlers/          # Command/Query handlers
│   ├── dtos/              # Data Transfer Objects
│   └── mappers/           # DTO <-> Domain mapping
├── domain/                # Domain layer (Business logic)
│   ├── entities/          # Domain entities
│   ├── interfaces/        # Repository interfaces
│   ├── events/            # Domain events
│   └── enums/             # Domain enumerations
└── infrastructure/        # Infrastructure layer
    ├── persistence/       # Database implementation
    │   ├── entities/      # JPA entities
    │   ├── repositories/  # JPA repositories
    │   └── mappers/       # Entity <-> Domain mapping
    └── storage/           # Cloud storage adapters
        ├── MinioStorageAdapter.java   # MinIO/S3 implementation
        └── LocalStorageAdapter.java   # Local filesystem fallback
```

### Architecture Pattern

This project follows **Clean Architecture** with **CQRS** (Command Query Responsibility Segregation):

- **Commands**: Handle write operations (Create, Update, Delete)
- **Queries**: Handle read operations (Get, List, Search)
- **Handlers**: Process commands and queries
- **Mappers**: Transform data between layers

## 👥 Development Workflow

### Working on an Issue

1. **Find your assigned issue** on the [Project Board](https://github.com/orgs/IYTE-Yazilim-Toplulugu/projects/23)

2. **Checkout the feature branch**:
   ```bash
   git fetch origin
   git checkout feature/issue-X-task-name
   ```

3. **Make your changes** following the coding standards

4. **Run tests**:
   ```bash
   ./gradlew test
   ```

5. **Commit your changes**:
   ```bash
   git add .
   git commit -m "feat: add user authentication endpoint"
   ```

   Follow [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat:` - New feature
   - `fix:` - Bug fix
   - `docs:` - Documentation
   - `refactor:` - Code refactoring
   - `test:` - Adding tests
   - `chore:` - Maintenance tasks

6. **Push to remote**:
   ```bash
   git push origin feature/issue-X-task-name
   ```

7. **Create a Pull Request**:
   - Base branch: `dev`
   - Compare branch: `feature/issue-X-task-name`
   - Link to the issue: `Closes #X`
   - Request reviews from team members

### Branch Strategy

- `main` - Production-ready code
- `dev` - Development branch (default)
- `feature/issue-X-*` - Feature branches for each issue

## 📚 API Documentation

### Authentication Endpoints

```http
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/logout
POST /api/v1/auth/refresh
GET  /api/v1/auth/verify-email?token={token}
POST /api/v1/auth/resend-verification
POST /api/v1/auth/forgot-password
POST /api/v1/auth/reset-password
```

### User Endpoints

```http
GET    /api/v1/users/{userId}
GET    /api/v1/users
GET    /api/v1/users/me
PUT    /api/v1/users/me
POST   /api/v1/users/me/profile-picture
PUT    /api/v1/users/me/password
DELETE /api/v1/users/me
```

### Project Endpoints

```http
GET    /api/v1/projects
GET    /api/v1/projects/{id}
POST   /api/v1/projects
PUT    /api/v1/projects/{id}
DELETE /api/v1/projects/{id}
PATCH  /api/v1/projects/{id}/status
GET    /api/v1/search/projects?q={keyword}
```

### Application Endpoints

```http
POST   /api/v1/projects/{id}/applications
GET    /api/v1/projects/{id}/applications
PUT    /api/v1/applications/{id}/review
DELETE /api/v1/applications/{id}
GET    /api/v1/users/me/applications
```

For detailed API documentation, visit the **Swagger UI** when the app is running.

## 📖 Javadoc Documentation

Generate API documentation:

```bash
./gradlew javadoc
```

View documentation at: `build/docs/javadoc/index.html`

### Generate Javadoc JAR

```bash
./gradlew javadocJar
```

The JAR file will be created at: `build/libs/proje-pazari-0.0.1-SNAPSHOT-javadoc.jar`

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
# or
make test
```

### Run Tests with Coverage

```bash
./gradlew test jacocoTestReport
# or
make coverage
```

View coverage report: `build/reports/jacoco/test/html/index.html`

### Verify Coverage Threshold

```bash
./gradlew jacocoTestCoverageVerification
# or
make coverage-verify
```

### Run Specific Test Class

```bash
./gradlew test --tests "com.iyte_yazilim.proje_pazari.application.handlers.RegisterUserHandlerTest"
```

### Writing Tests

Example test structure:

```java
@ExtendWith(MockitoExtension.class)
class RegisterUserHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegisterUserHandler handler;

    @Test
    void shouldRegisterUser_whenValidCommand() {
        // Given
        RegisterUserCommand command = new RegisterUserCommand(
            "student@std.iyte.edu.tr",
            "SecurePass123!",
            "John",
            "Doe"
        );

        // When
        RegisterUserResult result = handler.handle(command);

        // Then
        assertNotNull(result);
        assertEquals("student@std.iyte.edu.tr", result.email());
    }
}
```

## 🎨 Coding Standards

### Java Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Write self-documenting code
- Add comments only where logic isn't self-evident

### Code Formatting

Format your code with Google Java Format:

```bash
# Auto-format all code
make format
# or
./gradlew spotlessApply

# Check formatting without changing files
make format-check
# or
./gradlew spotlessCheck
```

### Code Quality

Run all quality checks before committing:

```bash
# Run format check, tests, and coverage
make quality

# Individual checks
make lint           # Check code formatting
make test           # Run all tests
make coverage       # Generate coverage report
make coverage-verify # Verify coverage meets threshold (70%)
```

### Database

- Use ULID for IDs (sortable UUIDs)
- Always use `@Transactional` for write operations
- Use Flyway or Liquibase for migrations (recommended for production)

### Security

- Never log sensitive data (passwords, tokens)
- Validate all user inputs
- Use parameterized queries (JPA does this automatically)
- Follow OWASP Top 10 guidelines

## 🐛 Common Issues

### Port Already in Use

If port 8080 is already in use:

```bash
# Find the process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

Or change the port in `application.properties`:

```properties
server.port=8081
```

### Database Connection Issues

1. Ensure PostgreSQL is running: `docker-compose ps`
2. Check database credentials in `application.properties`
3. Restart the database: `docker-compose restart postgres`

### Build Issues

Clean and rebuild:

```bash
./gradlew clean build
```

# 🌍 Internationalization (i18n)

The API supports **multi-language responses** to provide localized error messages and user feedback.

## Supported Languages

- **Turkish (tr)** - Default
- **English (en)**

## How to Use

### 1. Using Accept-Language Header

Send the `Accept-Language` header in your HTTP requests:

```bash
# Request in English
curl -H "Accept-Language: en" http://localhost:8080/api/v1/auth/login

# Request in Turkish (default)
curl -H "Accept-Language: tr" http://localhost:8080/api/v1/auth/login
```

### 2. User Language Preference (Optional)

Authenticated users can set their preferred language in their profile. This preference **overrides** the `Accept-Language` header.

**Update your language preference:**

```bash
PUT /api/v1/users/me
{
  "preferredLanguage": "en"
}
```

**Priority:**
1. User's preferred language (if authenticated and set)
2. `Accept-Language` header
3. Default language (Turkish)

## Examples

### Registration Error (Turkish - Default)

```bash
POST /api/v1/auth/register
{
  "email": "existing@example.com",
  "password": "weak"
}

# Response:
{
  "message": "Bu e-posta adresi zaten kayıtlı",
  "code": "BAD_REQUEST",
  "timestamp": "2025-01-02T10:30:00"
}
```

### Registration Error (English)

```bash
POST /api/v1/auth/register
Accept-Language: en
{
  "email": "existing@example.com",
  "password": "weak"
}

# Response:
{
  "message": "This email address is already registered",
  "code": "BAD_REQUEST",
  "timestamp": "2025-01-02T10:30:00"
}
```

## Adding New Languages

To add support for a new language:

1. Create a new message file: `src/main/resources/messages_{lang}.properties`
2. Translate all message keys from `messages_en.properties`
3. Update `InternationalizationConfig.java` to include the new locale
4. Update API documentation

Example for Spanish:

```properties
# src/main/resources/messages_es.properties
auth.login.success=Inicio de sesión exitoso
auth.login.failed=Nombre de usuario o contraseña no válidos
...
```

## Configuration

Language configuration is in `src/main/java/com/iyte_yazilim/proje_pazari/presentation/config/InternationalizationConfig.java`:

```java
@Bean
public LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
    localeResolver.setDefaultLocale(Locale.forLanguageTag("tr"));
    localeResolver.setSupportedLocales(
        Arrays.asList(
            Locale.forLanguageTag("tr"),
            Locale.forLanguageTag("en")
        )
    );
    return localeResolver;
}
```

## Testing i18n

### Manual Testing with curl

```bash
# Test Turkish response
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -H "Accept-Language: tr" \
  -d '{"email":"wrong@test.com","password":"wrong"}'

# Test English response
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -H "Accept-Language: en" \
  -d '{"email":"wrong@test.com","password":"wrong"}'
```

### Testing with Postman

1. Add `Accept-Language` header to your request
2. Set value to `en` or `tr`
3. Send request and verify response messages

### Swagger UI

When using Swagger UI, you can set the `Accept-Language` header for each request in the "Parameters" section.

## 📖 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)

## 📊 Monitoring

The application ships with a full observability stack: **Prometheus** for metric collection, **Grafana** for visualization, and **Micrometer** for instrumentation.

### Multi-environment startup

```bash
# Development (fast scraping, anonymous Grafana read access, SQL logging)
docker-compose -f docker-compose.yml -f docker-compose.dev.yml --env-file .env.dev up

# Staging (production-like, all secrets via env)
docker-compose -f docker-compose.yml -f docker-compose.staging.yml --env-file .env.staging up

# Production (resource limits, Prometheus port closed, restart:always)
docker-compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env.prod up -d
```

Copy and fill in the env template for your target environment:

```bash
cp .env.dev.example .env.dev          # development
cp .env.staging.example .env.staging  # staging
cp .env.prod.example .env.prod        # production (never commit!)
```

### Service URLs

| Service | URL | Credentials |
|---|---|---|
| **Prometheus** | http://localhost:9090 | — (no auth) |
| **Grafana** | http://localhost:3030 | `admin` / `admin123` |
| **Spring Actuator** | http://localhost:8080/actuator | — |
| **Prometheus metrics endpoint** | http://localhost:8080/actuator/prometheus | — |

---

### Accessing Metrics

#### Prometheus Raw Metrics

```bash
# All metrics exposed by the application
curl http://localhost:8080/actuator/prometheus

# Health check
curl http://localhost:8080/actuator/health

# Specific metric via Actuator JSON
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/http.server.requests
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

#### Prometheus UI

Open http://localhost:9090 to run ad-hoc PromQL queries. Useful queries:

```promql
# JVM heap usage %
sum(jvm_memory_used_bytes{area="heap"}) / sum(jvm_memory_max_bytes{area="heap"}) * 100

# HTTP p95 response time
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket[5m])) by (le))

# 5xx error rate %
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
/ sum(rate(http_server_requests_seconds_count[5m])) * 100

# Active DB connections
hikaricp_connections_active

# Auth failure rate
rate(auth_login_total{status="failure"}[5m])
```

#### Grafana Dashboards

Log in to Grafana at http://localhost:3030 with `admin` / `admin123`.
Three dashboards are provisioned automatically on first start:

| Dashboard | UID | Direct URL | Description |
|---|---|---|---|
| **Application Performance** | `spring-boot-app` | http://localhost:3030/d/spring-boot-app | JVM memory, CPU, threads, HTTP request rate, response time percentiles (p50/p95/p99), status code distribution, active sessions, DB connection pool, query performance |
| **Business Metrics** | `business-metrics` | http://localhost:3030/d/business-metrics | Total users, active projects, application submissions by status, new registrations (24 h / 7 d / 30 d), project/category distribution, auth success rate, user activity heatmap, file storage activity |
| **Infrastructure** | `infrastructure` | http://localhost:3030/d/infrastructure | PostgreSQL HikariCP pool, Redis command latency & cache hit ratio, MinIO operation rates & upload duration, Elasticsearch request latency, container CPU / memory / network / disk I/O |

> 📸 Dashboard screenshots are stored in [`docs/screenshots/`](docs/screenshots/). Run the stack and capture them with **Grafana → Share → Export PNG**.

#### Dashboard Backup

```bash
# Windows
.\scripts\Backup-Dashboards.ps1

# Linux / macOS
./scripts/backup-dashboards.sh

# Custom URL / credentials
.\scripts\Backup-Dashboards.ps1 -GrafanaUrl http://my-server:3030 -Password secret
```

Backups are saved under `backups/grafana/<YYYY-MM-DD_HH-mm>/` as individual JSON files plus a `manifest.json`.

#### Metrics Reference

See **[`docs/metrics-explorer.md`](docs/metrics-explorer.md)** for the complete PromQL query reference, tag filtering guide, and cheat sheet for all exposed metrics.

#### Stack Verification

Run the automated testing checklist after starting the stack:

```bash
# Windows
.\scripts\Verify-Stack.ps1

# Linux / macOS
./scripts/verify-stack.sh

# Include container restart / persistence test
.\scripts\Verify-Stack.ps1 -TestPersistence
./scripts/verify-stack.sh --test-persistence
```

The script covers all 12 checklist items: service health, Prometheus scraping, Grafana login, datasource connection, dashboard provisioning, business metric counters, HTTP metrics, alert rules, data persistence, and README completeness.

---

### Custom Business Metrics

The following application-level metrics are exposed at `/actuator/prometheus`:

| Metric | Labels | Description |
|---|---|---|
| `user_registration_total` | `status={success,failure}` | User registration counter |
| `project_creation_total` | `status={success,failure}` | Project creation counter |
| `application_submission_total` | `status={success,failure}` | Application submission counter |
| `auth_login_total` | `status={success,failure}` | Authentication attempts |
| `minio_upload_total` | `status={success,failure}` | MinIO upload counter |
| `minio_upload_duration_seconds` | — | MinIO upload duration histogram |
| `minio_download_total` | `status={success,failure}` | MinIO presigned URL generation counter |
| `minio_delete_total` | `status={success,failure}` | MinIO delete counter |

All metrics carry an `application="proje-pazari"` tag set via:

```properties
management.metrics.tags.application=${spring.application.name}
```

Auto-instrumented metrics (no code needed):

- **`http_server_requests_seconds`** — per-endpoint request rate, duration histogram, status codes
- **`hikaricp_connections_*`** — full HikariCP connection pool stats
- **`jvm_memory_*`, `jvm_threads_*`** — JVM internals
- **`process_cpu_usage`, `system_cpu_usage`** — CPU
- **`tomcat_sessions_*`** — active HTTP sessions
- **`lettuce_command_*`, `cache_gets_total`** — Redis / Lettuce

---

### Alert Rules

Prometheus evaluates `docker/prometheus/rules.yml` every 15 s.
Alerts fire to Grafana's unified alerting engine which routes notifications based on severity.

| Alert | Condition | Severity | Notification |
|---|---|---|---|
| `HighHeapMemoryUsage` | Heap > 80 % for 2 m | warning | Email + Slack |
| `CriticalHeapMemoryUsage` | Heap > 95 % for 1 m | critical | Email + Slack + Discord |
| `HighProcessCpuUsage` | CPU > 70 % for 3 m | warning | Email + Slack |
| `HighHttpErrorRate` | 5xx > 5 % for 2 m | warning | Email + Slack |
| `CriticalHttpErrorRate` | 5xx > 20 % for 1 m | critical | Email + Slack + Discord |
| `DatabaseConnectionPoolExhaustion` | Pool full + pending waiters | critical | Email + Slack + Discord |
| `DatabaseConnectionPoolHighUsage` | Pool > 80 % for 2 m | warning | Email + Slack |
| `DatabaseConnectionAcquireTimeout` | Any timeout in 5 m | warning | Email + Slack |
| `SlowApiResponseTime` | p95 > 2 s for 5 m | warning | Email + Slack |
| `CriticalApiResponseTime` | p95 > 5 s for 2 m | critical | Email + Slack + Discord |
| `SlowApiEndpoint` | Per-endpoint p95 > 2 s | warning | Email + Slack |
| `SpringBootAppDown` | `up == 0` for 1 m | critical | Email + Slack + Discord |
| `PrometheusTargetMissing` | Any target `up == 0` for 2 m | critical | Email + Slack + Discord |
| `HighAuthenticationFailureRate` | Failure rate > 30 % for 3 m | warning | Email + Slack |
| `AuthenticationFailureSurge` | > 5 failures/s for 1 m | critical | Email + Slack + Discord |
| `MinioUploadFailureRateHigh` | Upload failure > 5 % for 3 m | warning | Email + Slack |
| `SlowMinioUpload` | Upload p95 > 10 s for 5 m | warning | Email + Slack |

#### Notification channels

Configure alert destinations by setting environment variables (in `.env` or shell):

```bash
# Email (SMTP)
GF_SMTP_ENABLED=true
GF_SMTP_HOST=smtp.gmail.com:587
GF_SMTP_USER=your-email@gmail.com
GF_SMTP_PASSWORD=your-app-password
GF_ALERT_EMAIL_TO=oncall@proje-pazari.com

# Slack (optional)
GF_SLACK_WEBHOOK_URL=https://hooks.slack.com/services/T.../B.../xxx

# Discord (optional)
GF_DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/.../xxx
```

Routing policy:

| Severity | Email | Slack | Discord | Repeat interval |
|---|---|---|---|---|
| critical | ✅ | ✅ `#alerts-critical` | ✅ | 1 h |
| warning | ✅ | ✅ `#alerts-warning` | — | 8 h |
| service-down | ✅ | ✅ | ✅ | 30 min |

---

### Monitoring File Structure

```
docker/
├── prometheus/
│   ├── prometheus.yml          # Scrape config — targets app:8080/actuator/prometheus
│   └── rules.yml               # 19 alert rules across 8 groups
└── grafana/
    └── provisioning/
        ├── datasources/
        │   └── prometheus.yml  # Auto-wires Prometheus as default datasource
        ├── dashboards/
        │   ├── dashboard.yml                    # Dashboard loader config
        │   ├── spring-boot-dashboard.json       # JVM + HTTP dashboard
        │   ├── business-metrics-dashboard.json  # Business KPIs dashboard
        │   └── infrastructure-dashboard.json    # Infra health dashboard
        └── alerting/
            ├── contact-points.yml       # Email, Slack, Discord contact points
            └── notification-policies.yml # Alert routing rules
```

## 🤝 Contributingfor details on our code of conduct and the process for submitting pull requests.

## 📝 License

This project is developed by IYTE Yazılım Topluluğu for educational purposes.

## 👨‍💻 Team

- **Web Team**: DrHalley, UlasGokkaya, neonid0, Xerkara, AliKemalMiloglu, bdurgut06, ygt-ernsy, ErkanArikan

## 📞 Support

For questions or issues:
- Open an issue on GitHub
- Contact: yazilim@iyte.edu.tr
- Project Board: https://github.com/orgs/IYTE-Yazilim-Toplulugu/projects/23

---

Made with ❤️ by IYTE Yazılım Topluluğu
