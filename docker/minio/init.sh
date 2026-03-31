#!/bin/sh
set -eu

MINIO_ALIAS="${MINIO_ALIAS:-myminio}"
MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://minio:9000}"
MINIO_ADMIN_USER="${MINIO_ADMIN_USER:-minioadmin}"
MINIO_ADMIN_PASSWORD="${MINIO_ADMIN_PASSWORD:-minioadmin123}"
MINIO_FILES_BUCKET="${MINIO_FILES_BUCKET:-proje-pazari-files}"
MINIO_AVATARS_BUCKET="${MINIO_AVATARS_BUCKET:-proje-pazari-avatars}"
MINIO_DOCUMENTS_BUCKET="${MINIO_DOCUMENTS_BUCKET:-proje-pazari-documents}"
MINIO_BACKUPS_BUCKET="${MINIO_BACKUPS_BUCKET:-proje-pazari-backups}"
MINIO_CREATE_BACKEND_USER="${MINIO_CREATE_BACKEND_USER:-false}"
MINIO_BACKEND_USER="${MINIO_BACKEND_USER:-backend-app}"
MINIO_BACKEND_PASSWORD="${MINIO_BACKEND_PASSWORD:-backend-app-secret-change-me}"

log() {
    echo "[minio-setup] $1"
}

log "Configuring MinIO alias ${MINIO_ALIAS}..."
/usr/bin/mc alias set "${MINIO_ALIAS}" "${MINIO_ENDPOINT}" "${MINIO_ADMIN_USER}" "${MINIO_ADMIN_PASSWORD}" --api S3v4

log "Waiting for MinIO readiness..."
until /usr/bin/mc ready "${MINIO_ALIAS}" >/dev/null 2>&1; do
    sleep 2
done

log "Creating buckets if needed..."
/usr/bin/mc mb "${MINIO_ALIAS}/${MINIO_FILES_BUCKET}" --ignore-existing
/usr/bin/mc mb "${MINIO_ALIAS}/${MINIO_AVATARS_BUCKET}" --ignore-existing
/usr/bin/mc mb "${MINIO_ALIAS}/${MINIO_DOCUMENTS_BUCKET}" --ignore-existing
/usr/bin/mc mb "${MINIO_ALIAS}/${MINIO_BACKUPS_BUCKET}" --ignore-existing

log "Applying bucket policies..."
/usr/bin/mc anonymous set download "${MINIO_ALIAS}/${MINIO_AVATARS_BUCKET}"
/usr/bin/mc anonymous set none "${MINIO_ALIAS}/${MINIO_DOCUMENTS_BUCKET}"
/usr/bin/mc anonymous set none "${MINIO_ALIAS}/${MINIO_FILES_BUCKET}"
/usr/bin/mc anonymous set none "${MINIO_ALIAS}/${MINIO_BACKUPS_BUCKET}"

log "Enabling versioning on files bucket..."
/usr/bin/mc version enable "${MINIO_ALIAS}/${MINIO_FILES_BUCKET}" || true

log "Adding lifecycle rule for temporary uploads (7 days)..."
/usr/bin/mc ilm add "${MINIO_ALIAS}/${MINIO_FILES_BUCKET}" --prefix "temp/" --expiry-days 7 || true

log "Configured buckets:"
/usr/bin/mc ls "${MINIO_ALIAS}"

if [ "${MINIO_CREATE_BACKEND_USER}" = "true" ]; then
    log "Creating or updating backend app user '${MINIO_BACKEND_USER}'..."
    /usr/bin/mc admin user add "${MINIO_ALIAS}" "${MINIO_BACKEND_USER}" "${MINIO_BACKEND_PASSWORD}" || true
    /usr/bin/mc admin policy attach "${MINIO_ALIAS}" readwrite --user "${MINIO_BACKEND_USER}" || true
    log "Backend user '${MINIO_BACKEND_USER}' is configured with readwrite policy."
fi

log "MinIO initialization completed."
