# Storage Infrastructure (MinIO)

## Overview

The backend uses MinIO as an S3-compatible object storage backend.

- API endpoint (local): `http://localhost:9002`
- Console (local): `http://localhost:9003`
- Default admin credentials (local): `minioadmin` / `minioadmin123`
- Default app bucket: `proje-pazari-files`

MinIO data is persisted through the `minio_data` Docker volume, so objects survive container restarts.

## Services

`docker-compose.yml` defines:

- `minio`: main object storage service
- `minio-setup`: one-shot initialization service (creates buckets and policies)
- `minio-backup`: optional maintenance profile service to mirror buckets into `./backups`
- `minio-backup-scheduler`: optional maintenance profile service for recurring backups

## Buckets

Buckets created automatically by `docker/minio/init.sh`:

- `proje-pazari-files`
- `proje-pazari-avatars`
- `proje-pazari-documents`
- `proje-pazari-backups`

## Policies

Default policy setup:

- `proje-pazari-avatars`: anonymous download enabled (public avatar reads)
- `proje-pazari-documents`: private
- `proje-pazari-files`: private
- `proje-pazari-backups`: private

Additional defaults:

- Versioning enabled on `proje-pazari-files`
- Lifecycle rule deletes `temp/` objects in `proje-pazari-files` older than 7 days

## Object Naming

Current organized naming conventions:

- Avatars: `proje-pazari-avatars/users/{userId}/avatar.{ext}`
- Project documents: `proje-pazari-documents/projects/{projectId}/{documentId}.{ext}`
- Generic files: `proje-pazari-files/{domain-specific-path}`

Suggested project-level paths for generic files:

- `users/{userId}/profile/`
- `users/{userId}/documents/`
- `projects/{projectId}/attachments/`
- `projects/{projectId}/images/`
- `projects/{projectId}/documents/`
- `temp/uploads/`

## Health & Diagnostics

Admin endpoint:

- `GET /api/v1/admin/storage/health`

Response includes:

- adapter/provider name
- availability (`available`)
- total/used bytes when available
- bucket list
- check timestamp

## Backup

Manual backup with maintenance profile:

```bash
docker compose --profile maintenance up minio-backup
```

This runs `scripts/backup-minio.sh`, which mirrors:

- `proje-pazari-files` -> `./backups/minio/{timestamp}/files`
- `proje-pazari-avatars` -> `./backups/minio/{timestamp}/avatars`
- `proje-pazari-documents` -> `./backups/minio/{timestamp}/documents`

Automated recurring backups:

```bash
docker compose --profile maintenance up -d minio-backup-scheduler
```

Scheduler config:

- `BACKUP_INTERVAL_SECONDS` (default `86400`, i.e. daily)

## Monitoring

Optional monitoring stack is available through Docker Compose profile:

```bash
docker compose --profile monitoring up -d prometheus alertmanager grafana
```

- Prometheus: `http://localhost:9090`
- Alertmanager: `http://localhost:9093`
- Grafana: `http://localhost:3001`

Configured scrapes:

- backend actuator: `/actuator/prometheus`
- MinIO cluster metrics: `/minio/v2/metrics/cluster`

Alert rules configured:

- `MinIOStorageUsageHigh` (>80% for 10m)
- `MinIOStorageUsageCritical` (>90% for 5m)
- `MinIODown` (target down for 2m)

Local MinIO metrics are enabled with:

- `MINIO_PROMETHEUS_AUTH_TYPE=public`

For production, prefer token-based metrics auth and private network access.

## Service Accounts

Optional backend user bootstrap is supported in `docker/minio/init.sh`:

- `MINIO_CREATE_BACKEND_USER=true`
- `MINIO_APP_ACCESS_KEY=<backend-user>`
- `MINIO_APP_SECRET_KEY=<backend-password>`

When enabled, setup script creates/updates the backend user and attaches `readwrite` policy.

## Common `mc` Commands

```bash
mc alias set myminio http://localhost:9002 minioadmin minioadmin123
mc ls myminio
mc ls myminio/proje-pazari-files
mc du myminio/proje-pazari-files
mc share download myminio/proje-pazari-files/path/to/object --expire=7d
```

## Troubleshooting

1. Console not accessible
- Check container: `docker compose ps minio`
- Check logs: `docker compose logs minio`
- Verify ports `9002` and `9003` are free

2. Buckets missing
- Check setup logs: `docker compose logs minio-setup`
- Re-run setup: `docker compose up minio-setup`

3. Upload fails from backend
- Verify app env vars: `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`
- Verify bucket permissions and bucket names
- Check backend logs for `FileStorageException`

4. Storage health endpoint reports unavailable
- Verify MinIO container health
- Verify backend can reach `MINIO_URL` from its network

## Migration (Local Files -> MinIO)

Use existing migration script:

```bash
./scripts/migrate-to-minio.sh
```

The script uploads files from `./uploads` to MinIO and provides SQL guidance for updating stored paths.
