# Deployment Guide

This document covers deployment options for the Proje Pazarı Backend.

## Environment Variables

| Variable | Description | Required | Default | Startup if missing |
|----------|-------------|----------|---------|-------------------|
| `JWT_SECRET` | Secret key for JWT signing (min 32 chars, no placeholder substrings) | **Yes** | — | `IllegalStateException`, non-zero exit |
| `JWT_EXPIRATION` | Token expiration in milliseconds | No | `86400000` (24h) | — |
| `ELASTIC_PASSWORD` | Elasticsearch built-in `elastic` user password (production only) | Prod | — | ES returns 401, app fails to index |
| `KIBANA_SYSTEM_PASSWORD` | Password assigned to Elasticsearch's `kibana_system` user for Kibana 9.x | Prod when Kibana enabled | — | Kibana refuses to start |
| `SPRING_ELASTICSEARCH_USERNAME` | ES username forwarded to Spring Boot | Prod | `elastic` | — |
| `SPRING_ELASTICSEARCH_PASSWORD` | ES password forwarded to Spring Boot | Prod | `""` | App starts but cannot authenticate to ES |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | Yes | — | — |
| `SPRING_DATASOURCE_USERNAME` | Database username | Yes | — | — |
| `SPRING_DATASOURCE_PASSWORD` | Database password | Yes | — | — |
| `APP_IMAGE` | Backend Docker image tag used by `docker-compose.prod.yml` overlay | Prod compose | — | Compose config fails |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | DDL handling strategy | No | `update` | — |
| `SPRING_JPA_SHOW_SQL` | Log SQL statements | No | `false` | — |
| `APP_UPLOAD_DIR` | File upload directory | No | `./uploads` | — |

> [!CAUTION]
> **`JWT_SECRET` is enforced at startup.** The application contains a fail-fast validator
> (`JwtSecretValidator`) that runs on every startup via `@PostConstruct`. It will throw an
> `IllegalStateException` and abort with a non-zero exit code if:
>
> - `JWT_SECRET` is not set (the built-in fallback deliberately contains `"change-this-in-production"` and is rejected), or
> - The secret contains the known placeholder substring `"change-this-in-production"`, or
> - The secret is shorter than 32 characters.
>
> **Generate a compliant secret:**
> ```bash
> openssl rand -base64 64
> ```
> The output is ~88 characters of random base64 — well above the 32-character minimum.

---

## Local Development

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Gradle 8.x (or use wrapper)

### Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/IYTE-Yazilim-Toplulugu/proje-pazari-backend.git
cd proje-pazari-backend

# 2. Start PostgreSQL with Docker
docker-compose up -d postgres

# 3. Run the application
./gradlew bootRun

# 4. Access the API
curl http://localhost:8080/health
```

---

## Docker Deployment

### Using Docker Compose (Recommended)

The `docker-compose.yml` includes all necessary services:

```bash
# Start all services (app + postgres + pgadmin)
docker-compose --profile tools up -d

# Start only app and postgres
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

**Services:**
| Service | Port | Description |
|---------|------|-------------|
| `app` | 8080 | Spring Boot application |
| `postgres` | 5432 | PostgreSQL database |
| `pgadmin` | 5050 | Database admin (optional) |

### Building Docker Image Manually

```bash
# Build the image
docker build -t proje-pazari-backend .

# Run the container
docker run -d \
  --name proje-pazari-app \
  -p 8080:8080 \
  -e JWT_SECRET=your-secure-secret-key-here \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/proje_pazari_db \
  -e SPRING_DATASOURCE_USERNAME=yazilim \
  -e SPRING_DATASOURCE_PASSWORD=yazilim123 \
  proje-pazari-backend
```

---

## Production Deployment

### Checklist

Before deploying to production, ensure:

- [ ] JWT_SECRET is a secure, random value
- [ ] Database credentials are properly secured
- [ ] `ddl-auto` is set to `validate` or `none`
- [ ] Logging is configured appropriately
- [ ] Health endpoints are accessible
- [ ] SSL/TLS is configured
- [ ] CORS is properly configured

### Environment-Specific Configuration

Create `application-prod.properties`:

```properties
# Database
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

# JPA - Don't auto-update schema in production
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Security
jwt.secret=${JWT_SECRET}

# Logging
logging.level.root=WARN
logging.level.com.iyte_yazilim.proje_pazari=INFO
```

Run with production profile:

```bash
java -jar app.jar --spring.profiles.active=prod
```

---

## Cloud Deployment Options

### AWS Elastic Beanstalk

```bash
# Install EB CLI
pip install awsebcli

# Initialize
eb init proje-pazari-backend --platform java-21 --region eu-central-1

# Create environment
eb create proje-pazari-prod

# Deploy
eb deploy

# View logs
eb logs
```

### Heroku

```bash
# Login to Heroku
heroku login

# Create app
heroku create proje-pazari-backend

# Add PostgreSQL addon
heroku addons:create heroku-postgresql:mini

# Set environment variables
heroku config:set JWT_SECRET=your-secure-secret-key

# Deploy
git push heroku main
```

### Railway

1. Connect your GitHub repository
2. Add PostgreSQL database
3. Set environment variables in dashboard
4. Deploy automatically on push

### DigitalOcean App Platform

1. Create new App from GitHub
2. Select Java 21 runtime
3. Add PostgreSQL database
4. Configure environment variables
5. Deploy

---

## Database Migration

### Current Setup

The application uses Hibernate's `ddl-auto=update` for development, which automatically updates the schema based on entity changes.

> [!WARNING]
> For production, use `ddl-auto=validate` or `ddl-auto=none` and manage migrations manually.

### Recommended: Flyway Migration

For production, consider adding Flyway:

1. Add dependency:
```gradle
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'
```

2. Create migration files in `src/main/resources/db/migration/`:
```sql
-- V1__initial_schema.sql
CREATE TABLE users (
    id VARCHAR(26) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    ...
);
```

3. Configure Flyway:
```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
```

---

## Elasticsearch Index Management

Project and user search are backed by Elasticsearch documents (`ProjectDocument`,
`UserDocument`) that are denormalized copies of the PostgreSQL data. The index mappings are
auto-created from the `@Field` annotations at startup, but existing documents are **not**
rewritten when those annotations change.

### Post-Deploy Reindex (required after a document shape/mapping change)

Whenever a release changes the shape of a search document — renaming/adding/removing fields,
flattening nested objects, or changing a `@Field` type/analyzer — pre-existing documents stay
in the **old** shape. Until they are rewritten, search hits deserialize with the old layout
(missing or null fields), so the change appears not to work for already-indexed records.

After such a deploy, once the smoke test passes, trigger a full reindex. The endpoint drops the
index and rebuilds it from PostgreSQL (runs asynchronously):

```bash
# Projects (ADMIN role + Bearer token required)
curl -X POST https://api.projepazari.site/api/v1/admin/elasticsearch/reindex/projects \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# Users (only when the user document shape changed)
curl -X POST https://api.projepazari.site/api/v1/admin/elasticsearch/reindex/users \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

> [!NOTE]
> The CD pipeline does **not** reindex automatically — this is a manual post-deploy step.
> Monitor the application logs for completion since the reindex is `@Async`.

---

## Health Checks

### Built-in Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info
```

### Docker Health Check

The `docker-compose.yml` includes health checks:

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
```

---

## Scaling

### Horizontal Scaling

1. Use a load balancer (nginx, HAProxy, cloud LB)
2. Ensure session state is externalized (JWT is stateless)
3. Use shared file storage for uploads
4. Configure multiple application instances

### Vertical Scaling

Adjust JVM settings:

```bash
java -Xms512m -Xmx2g -jar app.jar
```

---

## Monitoring

### Recommended Tools

- **Prometheus + Grafana**: Metrics collection and visualization
- **ELK Stack**: Centralized logging
- **New Relic / Datadog**: APM solutions

### Enable Prometheus Metrics

Add to `application.properties`:

```properties
management.endpoints.web.exposure.include=health,info,prometheus
management.endpoint.prometheus.enabled=true
```

Start monitoring services with Docker Compose profile:

```bash
docker compose --profile monitoring up -d prometheus alertmanager grafana
```

- Prometheus: `http://localhost:9090`
- Alertmanager: `http://localhost:9093`
- Grafana: `http://localhost:3030`

---

## Backup and Recovery

### Database Backup

```bash
# Backup
docker exec proje-pazari-db pg_dump -U yazilim proje_pazari_db > backup.sql

# Restore
docker exec -i proje-pazari-db psql -U yazilim proje_pazari_db < backup.sql
```

### Scheduled Backups

Use the built-in maintenance scheduler service:

```bash
docker compose --profile maintenance up -d minio-backup-scheduler
```

Set interval with:

```bash
BACKUP_INTERVAL_SECONDS=86400
```

---

## Production Elasticsearch Security

The default `docker-compose.yml` runs Elasticsearch with `xpack.security.enabled=false` (development
mode). Before deploying to production, overlay the security configuration:

```bash
# Generate a strong password
ELASTIC_PASSWORD=$(openssl rand -base64 32)

# Start with the production overlay
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

The `docker-compose.prod.yml` overlay:
- Enables `xpack.security.enabled=true`
- Requires `ELASTIC_PASSWORD` to be set in the environment (fails fast otherwise)
- Removes localhost port bindings for ES and Kibana (internal network only)
- Passes credentials to the Spring Boot app via `SPRING_ELASTICSEARCH_USERNAME/PASSWORD`

> [!IMPORTANT]
> Store `ELASTIC_PASSWORD` in your secret manager (Vault, AWS Secrets Manager, k8s Secret).
> Never commit it to version control.

---

## GitHub Actions CD Pipeline

The `prod-cd.yml` workflow automatically deploys to the production VPS on every push to `main`
(and on `workflow_dispatch` for manual re-deploys). The pipeline runs five sequential jobs:

1. **Format** — Spotless check (fail-fast gate).
2. **Build & Test** — Gradle build + JaCoCo verification (≥70% instruction / ≥60% line).
3. **Publish** — Builds the Docker image and pushes it to **GHCR** as
   `ghcr.io/iyte-yazilim-toplulugu/proje-pazari-backend:{<sha>, latest}`.
   Outputs an immutable `image@sha256:digest` reference for the deploy step.
4. **Deploy** — SSHes into the VPS, pulls the exact image digest, updates `APP_IMAGE` in
   `.env.prod`, and runs `docker compose up -d --no-deps app` (zero downtime for all other
   services).
5. **Smoke Test** — Curls `PROD_HEALTH_URL` (Spring Boot Actuator) with 5 retries / 15s
   backoff to confirm the new container is healthy.

### One-time VPS setup

```bash
sudo mkdir -p /opt/proje-pazari
cd /opt/proje-pazari

# Copy docker-compose.yml, docker-compose.prod.yml, and .env.prod
# .env.prod must contain APP_IMAGE=placeholder (the pipeline replaces it on every deploy)

# Log in to GHCR once (or set GHCR_READ_TOKEN + GHCR_READ_USER as persistent env vars)
echo "$GHCR_READ_TOKEN" | docker login ghcr.io -u "$GHCR_READ_USER" --password-stdin
```

Minimal `.env.prod` template:

```env
APP_IMAGE=ghcr.io/iyte-yazilim-toplulugu/proje-pazari-backend:latest
ELASTIC_PASSWORD=<generate with: openssl rand -base64 32>
JWT_SECRET=<generate with: openssl rand -base64 64>
POSTGRES_DB=proje_pazari_db
POSTGRES_USER=<db_user>
POSTGRES_PASSWORD=<db_password>
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/proje_pazari_db
MINIO_ADMIN_USER=<minio_root_user>
MINIO_ADMIN_PASSWORD=<minio_root_password>
MINIO_APP_ACCESS_KEY=<backend_access_key>
MINIO_APP_SECRET_KEY=<backend_secret_key>
FRONTEND_URL=https://projepazari.site
FRONTEND_VERIFY_EMAIL_PATH=/verify-email
GF_SECURITY_ADMIN_USER=<grafana_admin>
GF_SECURITY_ADMIN_PASSWORD=<grafana_password>
```

### Required GitHub Secrets

Configure under **Settings → Secrets and variables → Actions**:

| Secret | Purpose |
|--------|---------|
| `SERVER_HOST` | VPS hostname or IP |
| `SERVER_USER` | SSH user (non-root, in the `docker` group) |
| `SERVER_SSH_KEY` | Private SSH key (ed25519 PEM format) |
| `SERVER_SSH_PORT` | SSH port — optional, defaults to 22 |
| `PROD_HEALTH_URL` | Public health URL, e.g. `https://api.projepazari.site/actuator/health` |
| `GH_PAT` | Classic PAT with `repo` scope (already used by `pr-validation.yml`) |

> [!NOTE]
> Application secrets (`JWT_SECRET`, `ELASTIC_PASSWORD`, database credentials, MinIO keys, etc.)
> live in `.env.prod` **on the VPS** — they are never passed through GitHub Actions and never
> appear in workflow logs.

### Manual approval gate

The `deploy` job uses the **`production`** GitHub Environment. Configure required reviewers
under **Settings → Environments → production** to enforce human approval before any SSH deploy
is triggered. This is strongly recommended for the first several production releases.

### Generating the SSH deploy keypair

```bash
# Generate a dedicated ed25519 keypair for CI/CD
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_deploy -N ""

# Add the public key to the VPS (SERVER_USER's authorized_keys)
ssh-copy-id -i ~/.ssh/github_deploy.pub SERVER_USER@SERVER_HOST

# Add the private key to GitHub Secrets as SERVER_SSH_KEY
cat ~/.ssh/github_deploy   # copy this value into the secret
```
