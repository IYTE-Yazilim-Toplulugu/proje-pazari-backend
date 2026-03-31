#!/bin/sh
set -eu

MINIO_ALIAS="${MINIO_ALIAS:-myminio}"
MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://minio:9000}"
MINIO_ADMIN_USER="${MINIO_ADMIN_USER:-minioadmin}"
MINIO_ADMIN_PASSWORD="${MINIO_ADMIN_PASSWORD:-minioadmin123}"
MINIO_FILES_BUCKET="${MINIO_FILES_BUCKET:-proje-pazari-files}"
MINIO_AVATARS_BUCKET="${MINIO_AVATARS_BUCKET:-proje-pazari-avatars}"
MINIO_DOCUMENTS_BUCKET="${MINIO_DOCUMENTS_BUCKET:-proje-pazari-documents}"
BACKUP_ROOT="${BACKUP_ROOT:-/backups/minio}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_DIR="${BACKUP_ROOT}/${TIMESTAMP}"

log() {
    echo "[minio-backup] $1"
}

log "Configuring MinIO alias ${MINIO_ALIAS}..."
/usr/bin/mc alias set "${MINIO_ALIAS}" "${MINIO_ENDPOINT}" "${MINIO_ADMIN_USER}" "${MINIO_ADMIN_PASSWORD}" --api S3v4

log "Waiting for MinIO readiness..."
until /usr/bin/mc ready "${MINIO_ALIAS}" >/dev/null 2>&1; do
    sleep 2
done

mkdir -p "${BACKUP_DIR}"

log "Backing up ${MINIO_FILES_BUCKET}..."
/usr/bin/mc mirror "${MINIO_ALIAS}/${MINIO_FILES_BUCKET}" "${BACKUP_DIR}/files"

log "Backing up ${MINIO_AVATARS_BUCKET}..."
/usr/bin/mc mirror "${MINIO_ALIAS}/${MINIO_AVATARS_BUCKET}" "${BACKUP_DIR}/avatars"

log "Backing up ${MINIO_DOCUMENTS_BUCKET}..."
/usr/bin/mc mirror "${MINIO_ALIAS}/${MINIO_DOCUMENTS_BUCKET}" "${BACKUP_DIR}/documents"

log "Backup completed at ${BACKUP_DIR}."
