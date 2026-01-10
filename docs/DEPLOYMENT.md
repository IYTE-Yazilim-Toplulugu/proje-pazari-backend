# Deployment Guide

This document covers deployment options for the Proje Pazarı Backend.

## Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `JWT_SECRET` | Secret key for JWT signing (min 256 bits) | Yes | - |
| `JWT_EXPIRATION` | Token expiration in milliseconds | No | `86400000` (24h) |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | Yes | - |
| `SPRING_DATASOURCE_USERNAME` | Database username | Yes | - |
| `SPRING_DATASOURCE_PASSWORD` | Database password | Yes | - |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | DDL handling strategy | No | `update` |
| `SPRING_JPA_SHOW_SQL` | Log SQL statements | No | `false` |
| `APP_UPLOAD_DIR` | File upload directory | No | `./uploads` |

> [!CAUTION]
> **Never use default JWT secrets in production!** Generate a secure random string of at least 32 characters.

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

Use cron or cloud-native backup solutions for automated backups.
