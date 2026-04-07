#!/bin/sh
set -eu

BACKUP_INTERVAL_SECONDS="${BACKUP_INTERVAL_SECONDS:-86400}"

if ! [ "${BACKUP_INTERVAL_SECONDS}" -eq "${BACKUP_INTERVAL_SECONDS}" ] 2>/dev/null; then
    echo "[minio-backup-scheduler] BACKUP_INTERVAL_SECONDS must be an integer"
    exit 1
fi

if [ "${BACKUP_INTERVAL_SECONDS}" -le 0 ]; then
    echo "[minio-backup-scheduler] BACKUP_INTERVAL_SECONDS must be > 0"
    exit 1
fi

echo "[minio-backup-scheduler] Starting backup loop (interval=${BACKUP_INTERVAL_SECONDS}s)"

while true; do
    /bin/sh /backup.sh || echo "[minio-backup-scheduler] Backup run failed"
    echo "[minio-backup-scheduler] Sleeping for ${BACKUP_INTERVAL_SECONDS}s"
    sleep "${BACKUP_INTERVAL_SECONDS}"
done
