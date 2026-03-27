#!/bin/bash
set -euo pipefail
# backup-database.sh — Automated PostgreSQL backup script
#
# Usage:
#   ./scripts/backup-database.sh
#
# Environment variables (all optional, defaults match docker-compose.yml):
#   POSTGRES_CONTAINER  — Docker container name (default: proje-pazari-db)
#   POSTGRES_USER       — Database user (default: yazilim)
#   POSTGRES_DB         — Database name (default: proje_pazari_db)
#   BACKUP_DIR          — Backup output directory (default: <project-root>/backups/postgres)
#   RETAIN_DAYS         — Days of backups to keep (default: 7)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

POSTGRES_CONTAINER="${POSTGRES_CONTAINER:-proje-pazari-db}"
POSTGRES_USER="${POSTGRES_USER:-yazilim}"
POSTGRES_DB="${POSTGRES_DB:-proje_pazari_db}"
BACKUP_DIR="${BACKUP_DIR:-$SCRIPT_DIR/../backups/postgres}"
RETAIN_DAYS="${RETAIN_DAYS:-7}"

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/${POSTGRES_DB}_${TIMESTAMP}.dump"

mkdir -p "$BACKUP_DIR"

echo "Starting PostgreSQL backup..."
echo "  Container : $POSTGRES_CONTAINER"
echo "  Database  : $POSTGRES_DB"
echo "  Output    : $BACKUP_FILE"

if ! docker ps --format '{{.Names}}' | grep -q "^${POSTGRES_CONTAINER}$"; then
  echo "ERROR: Container '$POSTGRES_CONTAINER' is not running."
  exit 1
fi

if ! docker exec "$POSTGRES_CONTAINER" pg_dump \
  -U "$POSTGRES_USER" \
  -Fc \
  "$POSTGRES_DB" > "$BACKUP_FILE"; then
  echo "ERROR: Backup failed."
  rm -f "$BACKUP_FILE"
  exit 1
fi

if [ ! -s "$BACKUP_FILE" ]; then
  echo "ERROR: Backup file is empty."
  rm -f "$BACKUP_FILE"
  exit 1
fi

echo "Backup completed: $BACKUP_FILE ($(du -sh "$BACKUP_FILE" | cut -f1))"

# Remove backups older than RETAIN_DAYS
find "$BACKUP_DIR" -name "*.dump" -mtime +"$RETAIN_DAYS" -delete
echo "Cleaned up backups older than $RETAIN_DAYS days."
