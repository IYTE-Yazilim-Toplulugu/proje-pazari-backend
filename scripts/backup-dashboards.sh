#!/usr/bin/env bash
# ============================================================
# backup-dashboards.sh
# Backs up all Grafana dashboards to a timestamped directory.
#
# Usage:
#   ./scripts/backup-dashboards.sh
#   GRAFANA_URL=http://my-server:3030 GRAFANA_PASSWORD=secret \
#       ./scripts/backup-dashboards.sh
#
# Environment variables (all optional):
#   GRAFANA_URL       Base URL of Grafana  (default: http://localhost:3030)
#   GRAFANA_USER      Admin username       (default: admin)
#   GRAFANA_PASSWORD  Admin password       (default: admin123)
#   BACKUP_DIR        Output parent dir    (default: <project-root>/backups/grafana)
# ============================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

GRAFANA_URL="${GRAFANA_URL:-http://localhost:3030}"
GRAFANA_USER="${GRAFANA_USER:-admin}"
GRAFANA_PASSWORD="${GRAFANA_PASSWORD:-admin123}"
BACKUP_DIR="${BACKUP_DIR:-$PROJECT_ROOT/backups/grafana}"

DATE=$(date +"%Y-%m-%d_%H-%M")
OUT="$BACKUP_DIR/$DATE"
mkdir -p "$OUT"

AUTH="$GRAFANA_USER:$GRAFANA_PASSWORD"

echo "🔍  Discovering dashboards at $GRAFANA_URL ..."
SEARCH=$(curl -sf --user "$AUTH" "$GRAFANA_URL/api/search?type=dash-db&limit=500") || {
  echo "❌  Cannot reach Grafana at $GRAFANA_URL — is the container running?" >&2
  exit 1
}

UIDS=$(echo "$SEARCH" | python3 -c "
import json, sys
data = json.load(sys.stdin)
for d in data:
    print(d['uid'] + '|' + d['title'])
" 2>/dev/null || echo "$SEARCH" | grep -oP '"uid":"\K[^"]+' | paste - -)

TOTAL=0; SUCCESS=0; FAILED=0

while IFS='|' read -r uid title; do
  [ -z "$uid" ] && continue
  TOTAL=$((TOTAL+1))
  # Sanitise filename
  safe_title=$(echo "$title" | tr '\\/:*?"<>|' '_')
  FILE="$OUT/${uid}-${safe_title}.json"
  if curl -sf --user "$AUTH" "$GRAFANA_URL/api/dashboards/uid/$uid" \
       -o "$FILE" 2>/dev/null; then
    echo "  ✅  $title → ${uid}-${safe_title}.json"
    SUCCESS=$((SUCCESS+1))
  else
    echo "  ❌  Failed: $title (uid=$uid)" >&2
    FAILED=$((FAILED+1))
  fi
done <<< "$UIDS"

# Write manifest
cat > "$OUT/manifest.json" <<JSON
{
  "exported_at": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "grafana_url": "$GRAFANA_URL",
  "total": $TOTAL,
  "success": $SUCCESS,
  "failed": $FAILED
}
JSON

echo ""
echo "✔  Backup complete: $SUCCESS exported, $FAILED failed → $OUT"
[ "$FAILED" -eq 0 ] || exit 1
