#!/bin/bash

# =============================================================================
# Migration Script: Local Files to MinIO
# =============================================================================
# This script migrates files from the local ./uploads folder to MinIO bucket
# and updates the database to reflect the new storage location.
#
# Prerequisites:
#   - MinIO client (mc) installed: https://min.io/docs/minio/linux/reference/minio-mc.html
#   - Docker running with MinIO container
#   - PostgreSQL accessible
#
# Usage:
#   ./scripts/migrate-to-minio.sh
#
# =============================================================================

set -e

# Configuration
UPLOADS_DIR="${UPLOADS_DIR:-./uploads}"
MINIO_ALIAS="${MINIO_ALIAS:-local}"
MINIO_ENDPOINT="${MINIO_ENDPOINT:-http://localhost:9002}"
MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY:-minioadmin}"
MINIO_SECRET_KEY="${MINIO_SECRET_KEY:-minioadmin123}"
MINIO_BUCKET="${MINIO_BUCKET:-proje-pazari-files}"

# Database configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-proje_pazari_db}"
DB_USER="${DB_USER:-yazilim}"
DB_PASSWORD="${DB_PASSWORD:-yazilim123}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    if ! command -v mc &> /dev/null; then
        log_error "MinIO client (mc) not found. Install it from: https://min.io/docs/minio/linux/reference/minio-mc.html"
        echo ""
        echo "Quick install:"
        echo "  curl https://dl.min.io/client/mc/release/linux-amd64/mc -o mc"
        echo "  chmod +x mc"
        echo "  sudo mv mc /usr/local/bin/"
        exit 1
    fi

    if ! command -v psql &> /dev/null; then
        log_warn "psql not found. Database URL updates will be skipped."
        log_warn "You may need to manually update profile_picture_url in the users table."
    fi

    if [ ! -d "$UPLOADS_DIR" ]; then
        log_warn "Uploads directory not found: $UPLOADS_DIR"
        log_info "Nothing to migrate. Exiting."
        exit 0
    fi

    log_info "Prerequisites check passed."
}

# Configure MinIO client
configure_minio() {
    log_info "Configuring MinIO client..."

    mc alias set "$MINIO_ALIAS" "$MINIO_ENDPOINT" "$MINIO_ACCESS_KEY" "$MINIO_SECRET_KEY" --api S3v4

    # Create bucket if not exists
    if ! mc ls "$MINIO_ALIAS/$MINIO_BUCKET" &> /dev/null; then
        log_info "Creating bucket: $MINIO_BUCKET"
        mc mb "$MINIO_ALIAS/$MINIO_BUCKET"
    else
        log_info "Bucket already exists: $MINIO_BUCKET"
    fi

    log_info "MinIO client configured."
}

# Migrate files to MinIO
migrate_files() {
    log_info "Migrating files from $UPLOADS_DIR to MinIO..."

    local count=0
    local failed=0

    # Find all files in uploads directory
    while IFS= read -r -d '' file; do
        # Get relative path from uploads directory
        local relative_path="${file#$UPLOADS_DIR/}"

        log_info "Uploading: $relative_path"

        if mc cp "$file" "$MINIO_ALIAS/$MINIO_BUCKET/$relative_path"; then
            ((count++))
        else
            log_error "Failed to upload: $relative_path"
            ((failed++))
        fi
    done < <(find "$UPLOADS_DIR" -type f -print0)

    log_info "Migration complete. Uploaded: $count files, Failed: $failed files"

    if [ $failed -gt 0 ]; then
        log_warn "Some files failed to upload. Please check and retry."
    fi
}

# Update database URLs
update_database() {
    if ! command -v psql &> /dev/null; then
        log_warn "Skipping database update (psql not available)"
        echo ""
        echo "Run this SQL manually to update profile picture URLs:"
        echo "------------------------------------------------------"
        echo "UPDATE users"
        echo "SET profile_picture_url = REPLACE(profile_picture_url, '/api/v1/files/', '')"
        echo "WHERE profile_picture_url LIKE '/api/v1/files/%';"
        echo "------------------------------------------------------"
        return
    fi

    log_info "Updating database URLs..."

    export PGPASSWORD="$DB_PASSWORD"

    # Update profile picture URLs from local path format to MinIO path format
    # Old format: /api/v1/files/profiles/filename.jpg
    # New format: profiles/filename.jpg (stored in DB, presigned URL generated on access)
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" <<EOF
-- Update profile picture URLs
UPDATE users
SET profile_picture_url = REPLACE(profile_picture_url, '/api/v1/files/', '')
WHERE profile_picture_url LIKE '/api/v1/files/%';

-- Show updated records
SELECT id, email, profile_picture_url
FROM users
WHERE profile_picture_url IS NOT NULL
LIMIT 10;
EOF

    log_info "Database URLs updated."
}

# Create backup of uploads folder
create_backup() {
    if [ -d "$UPLOADS_DIR" ]; then
        local backup_name="uploads_backup_$(date +%Y%m%d_%H%M%S).tar.gz"
        log_info "Creating backup: $backup_name"
        tar -czf "$backup_name" "$UPLOADS_DIR"
        log_info "Backup created: $backup_name"
    fi
}

# Main execution
main() {
    echo ""
    echo "=========================================="
    echo "  Local Files to MinIO Migration Script"
    echo "=========================================="
    echo ""

    check_prerequisites

    echo ""
    read -p "Create backup before migration? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        create_backup
    fi

    echo ""
    read -p "Continue with migration? (y/n): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_info "Migration cancelled."
        exit 0
    fi

    configure_minio
    migrate_files
    update_database

    echo ""
    log_info "=========================================="
    log_info "  Migration Complete!"
    log_info "=========================================="
    echo ""
    log_info "Next steps:"
    echo "  1. Verify files in MinIO Console: http://localhost:9003"
    echo "  2. Test file access in the application"
    echo "  3. Once verified, you can delete the ./uploads folder"
    echo ""
}

main "$@"
