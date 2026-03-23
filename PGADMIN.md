# pgAdmin — Database Management Guide

pgAdmin provides a web-based UI for browsing, querying, and managing the PostgreSQL database used by Proje Pazarı.

## Access

### Starting pgAdmin

pgAdmin runs under the `tools` Docker Compose profile:

```bash
docker compose --profile tools up -d pgadmin
```

### Login

| Field    | Default value              | Override env var              |
|----------|----------------------------|-------------------------------|
| URL      | http://localhost:5050      | —                             |
| Email    | admin@proje-pazari.com     | `PGADMIN_DEFAULT_EMAIL`       |
| Password | admin123                   | `PGADMIN_DEFAULT_PASSWORD`    |

The server **"Proje Pazari - Local"** connects automatically on first login — no manual configuration needed.

### Navigation

```
Servers
└── Development
    └── Proje Pazari - Local
        └── Databases
            └── proje_pazari_db
                └── Schemas → public → Tables
```

To open the query editor: right-click `proje_pazari_db` → **Query Tool** (or press `Alt+Shift+Q`).

---

## Query Collections

### User Management

```sql
-- All users with account status
SELECT id, email, first_name, last_name, is_active, created_at
FROM users
ORDER BY created_at DESC;

-- User count by active status
SELECT is_active, COUNT(*) AS count
FROM users
GROUP BY is_active;

-- Users with project and application counts
SELECT u.email,
       COUNT(DISTINCT p.id)  AS projects_owned,
       COUNT(DISTINCT pa.id) AS applications_submitted
FROM users u
LEFT JOIN projects p  ON p.owner_id = u.id
LEFT JOIN project_applications pa ON pa.applicant_id = u.id
GROUP BY u.id, u.email
ORDER BY projects_owned DESC;

-- Find users by email pattern
SELECT id, email, first_name, last_name, created_at
FROM users
WHERE email ILIKE '%@std.iyte.edu.tr'
ORDER BY created_at DESC;

-- Recently registered users (last 30 days)
SELECT id, email, first_name, last_name, created_at
FROM users
WHERE created_at >= NOW() - INTERVAL '30 days'
ORDER BY created_at DESC;
```

### Project Management

```sql
-- All projects with owner email
SELECT p.id, p.title, p.status, p.category,
       p.max_team_size, p.current_team_size,
       u.email AS owner_email,
       p.created_at
FROM projects p
JOIN users u ON p.owner_id = u.id
ORDER BY p.created_at DESC;

-- Projects by status
SELECT status, COUNT(*) AS count
FROM projects
GROUP BY status
ORDER BY count DESC;

-- Most popular projects (by application count)
SELECT p.title, p.status,
       u.email AS owner_email,
       COUNT(pa.id) AS application_count
FROM projects p
JOIN users u ON p.owner_id = u.id
LEFT JOIN project_applications pa ON pa.project_id = p.id
GROUP BY p.id, p.title, p.status, u.email
ORDER BY application_count DESC
LIMIT 10;

-- Projects with required skills
SELECT p.title, p.status, STRING_AGG(prs.skill, ', ') AS required_skills
FROM projects p
LEFT JOIN project_required_skills prs ON prs.project_id = p.id
GROUP BY p.id, p.title, p.status
ORDER BY p.created_at DESC;

-- Projects with upcoming deadlines
SELECT p.title, p.status, p.deadline, u.email AS owner_email
FROM projects p
JOIN users u ON p.owner_id = u.id
WHERE p.deadline IS NOT NULL
  AND p.deadline > NOW()
ORDER BY p.deadline ASC;

-- Search projects by keyword
SELECT p.id, p.title, p.status, p.category
FROM projects p
WHERE p.title ILIKE '%keyword%'
   OR p.description ILIKE '%keyword%'
ORDER BY p.created_at DESC;
```

### Application Management

```sql
-- All pending applications
SELECT pa.id,
       p.title  AS project_title,
       u.email  AS applicant_email,
       pa.status,
       pa.created_at
FROM project_applications pa
JOIN projects p ON pa.project_id = p.id
JOIN users u    ON pa.applicant_id = u.id
WHERE pa.status = 'PENDING'
ORDER BY pa.created_at ASC;

-- Application counts by status
SELECT status, COUNT(*) AS count
FROM project_applications
GROUP BY status
ORDER BY count DESC;

-- Applications per project
SELECT p.title,
       COUNT(pa.id)                                             AS total,
       COUNT(pa.id) FILTER (WHERE pa.status = 'PENDING')       AS pending,
       COUNT(pa.id) FILTER (WHERE pa.status = 'APPROVED')      AS approved,
       COUNT(pa.id) FILTER (WHERE pa.status = 'REJECTED')      AS rejected
FROM projects p
LEFT JOIN project_applications pa ON pa.project_id = p.id
GROUP BY p.id, p.title
ORDER BY total DESC;

-- Applications submitted by a specific user (replace with actual email)
SELECT pa.id, p.title, pa.status, pa.message, pa.created_at
FROM project_applications pa
JOIN projects p ON pa.project_id = p.id
JOIN users u    ON pa.applicant_id = u.id
WHERE u.email = 'user@example.com'
ORDER BY pa.created_at DESC;
```

### Performance & Maintenance

```sql
-- Database size
SELECT pg_size_pretty(pg_database_size('proje_pazari_db')) AS db_size;

-- Table sizes (largest first)
SELECT schemaname,
       tablename,
       pg_size_pretty(pg_total_relation_size(schemaname || '.' || tablename)) AS total_size,
       pg_size_pretty(pg_relation_size(schemaname || '.' || tablename))        AS table_size,
       pg_size_pretty(pg_indexes_size(schemaname || '.' || tablename))         AS index_size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname || '.' || tablename) DESC;

-- Index usage (low idx_scan = potentially unused index)
SELECT schemaname, tablename, indexname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan ASC;

-- Table statistics (dead tuples indicate need for VACUUM)
SELECT schemaname, tablename,
       n_live_tup  AS live_tuples,
       n_dead_tup  AS dead_tuples,
       last_vacuum,
       last_autovacuum
FROM pg_stat_user_tables
ORDER BY n_dead_tup DESC;

-- Active connections
SELECT pid, usename, application_name, state, query_start,
       LEFT(query, 80) AS query_preview
FROM pg_stat_activity
WHERE datname = 'proje_pazari_db'
  AND state <> 'idle'
ORDER BY query_start;

-- Enable query statistics extension (run once as superuser)
-- CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Slowest queries (requires pg_stat_statements)
-- SELECT query, calls, total_exec_time, mean_exec_time
-- FROM pg_stat_statements
-- ORDER BY mean_exec_time DESC
-- LIMIT 10;
```

---

## Backup & Restore

### Manual Backup via pgAdmin UI

1. In the object browser, right-click `proje_pazari_db` → **Backup...**
2. Set **Format** to `Custom`
3. Choose a file path and click **Backup**

### Automated Backup Script

```bash
# Run once
./scripts/backup-database.sh

# Output: ./backups/postgres/proje_pazari_db_YYYYMMDD_HHMMSS.dump
# Retention: last 7 days (configurable via RETAIN_DAYS)
```

Environment variable overrides:

| Variable             | Default             | Description                  |
|----------------------|---------------------|------------------------------|
| `POSTGRES_CONTAINER` | `proje-pazari-db`   | Running container name       |
| `POSTGRES_USER`      | `yazilim`           | Database user                |
| `POSTGRES_DB`        | `proje_pazari_db`   | Database name                |
| `BACKUP_DIR`         | `./backups/postgres`| Output directory             |
| `RETAIN_DAYS`        | `7`                 | Days of backups to keep      |

### Manual Backup via CLI

```bash
docker exec proje-pazari-db pg_dump \
  -U yazilim -Fc proje_pazari_db > backup_$(date +%Y%m%d).dump
```

### Restore via pgAdmin UI

1. Right-click `proje_pazari_db` → **Restore...**
2. Select the `.dump` file
3. Click **Restore**

### Restore via CLI

```bash
docker exec -i proje-pazari-db pg_restore \
  -U yazilim -d proje_pazari_db < backup_20250215.dump
```

---

## Security

### Read-Only Analytics User

Run this in the Query Tool to create a read-only user for reporting purposes:

```sql
CREATE USER readonly_user WITH PASSWORD 'choose-a-strong-password';
GRANT CONNECT ON DATABASE proje_pazari_db TO readonly_user;
GRANT USAGE ON SCHEMA public TO readonly_user;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO readonly_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT ON TABLES TO readonly_user;
```

---

## Troubleshooting

### pgAdmin won't start

```bash
# Check logs
docker compose --profile tools logs pgadmin

# Ensure postgres is healthy first
docker compose ps postgres
```

### "Server not found" after login

The `servers.json` is only loaded on first startup. If you changed it after pgAdmin already created its data volume:

```bash
docker compose --profile tools down pgadmin
docker volume rm proje-pazari-backend_pgadmin_data
docker compose --profile tools up -d pgadmin
```

### Password authentication failed

The `pgpass` file at `docker/pgadmin/pgpass` must match the `POSTGRES_PASSWORD` used by the postgres container. If you changed the postgres password, update `docker/pgadmin/pgpass` accordingly.

### Direct psql access (without pgAdmin)

```bash
docker exec -it proje-pazari-db psql -U yazilim -d proje_pazari_db
```
