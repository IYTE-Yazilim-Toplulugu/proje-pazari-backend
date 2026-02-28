#!/usr/bin/env bash
# ==============================================================
# verify-stack.sh — Automated verification of the monitoring stack
# Runs every item on the testing checklist.
#
# Usage:
#   ./scripts/verify-stack.sh
#   ./scripts/verify-stack.sh --test-persistence
#
# Environment overrides:
#   APP_URL          default: http://localhost:8080
#   PROMETHEUS_URL   default: http://localhost:9090
#   GRAFANA_URL      default: http://localhost:3030
#   GRAFANA_USER     default: admin
#   GRAFANA_PASSWORD default: admin123
# ==============================================================
set -uo pipefail

APP_URL="${APP_URL:-http://localhost:8080}"
PROMETHEUS_URL="${PROMETHEUS_URL:-http://localhost:9090}"
GRAFANA_URL="${GRAFANA_URL:-http://localhost:3030}"
GRAFANA_USER="${GRAFANA_USER:-admin}"
GRAFANA_PASSWORD="${GRAFANA_PASSWORD:-admin123}"
TEST_PERSISTENCE=0
[[ "${1:-}" == "--test-persistence" ]] && TEST_PERSISTENCE=1

PASS=0; FAIL=0; WARN=0

GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; MAGENTA='\033[0;35m'; GRAY='\033[0;37m'; NC='\033[0m'

check() {
  local label="$1"; local result="$2"; local hint="${3:-}"
  if [[ "$result" == "true" ]]; then
    echo -e "  ${GREEN}✅  $label${NC}"; ((PASS++))
  else
    local msg="  ${RED}❌  $label${NC}"
    [[ -n "$hint" ]] && msg+=" ${GRAY}← $hint${NC}"
    echo -e "$msg"; ((FAIL++))
  fi
}
warn() { echo -e "  ${YELLOW}⚠️   $1${NC}"; ((WARN++)); }
section() { echo -e "\n${CYAN}── $1${NC}"; }
auth() { echo -n "$GRAFANA_USER:$GRAFANA_PASSWORD"; }

echo -e ""
echo -e "${MAGENTA}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${MAGENTA}║   Proje Pazarı — Monitoring Stack Verification          ║${NC}"
echo -e "${MAGENTA}╚══════════════════════════════════════════════════════════╝${NC}"

# ─── 1. Docker service health ─────────────────────────────────────────────
section "1. Docker service health"
SERVICES=(proje-pazari-app proje-pazari-prometheus proje-pazari-grafana
          proje-pazari-postgres proje-pazari-redis proje-pazari-minio)
for svc in "${SERVICES[@]}"; do
  status=$(docker inspect --format='{{.State.Status}} {{.State.Health.Status}}' "$svc" 2>/dev/null || echo "not found")
  if [[ "$status" == "not found" ]]; then
    check "  $svc" false "not running — run: docker-compose up -d"
  else
    running=$(echo "$status" | grep -c "running")
    healthy=$(echo "$status" | grep -cE "healthy|running" || true)
    check "  $svc  [$status]" "$( [[ $running -gt 0 ]] && echo true || echo false )" \
      "run: docker inspect $svc | grep -i health"
  fi
done

# ─── 2. Prometheus UI ─────────────────────────────────────────────────────
section "2. Prometheus UI reachable"
ready_code=$(curl -so /dev/null -w "%{http_code}" --max-time 5 "$PROMETHEUS_URL/-/ready")
check "Prometheus ready at $PROMETHEUS_URL" "$( [[ $ready_code == "200" ]] && echo true || echo false )" \
  "HTTP $ready_code — is the container running?"
healthy_code=$(curl -so /dev/null -w "%{http_code}" --max-time 5 "$PROMETHEUS_URL/-/healthy")
check "Prometheus healthy" "$( [[ $healthy_code == "200" ]] && echo true || echo false )"

# ─── 3. Spring Boot metrics scraped ───────────────────────────────────────
section "3. Spring Boot metrics being scraped"
targets=$(curl -sf --max-time 10 "$PROMETHEUS_URL/api/v1/targets" 2>/dev/null)
target_up=$(echo "$targets" | python3 -c "
import json,sys
d=json.load(sys.stdin)
for t in d['data']['activeTargets']:
    if t['labels'].get('job')=='spring-boot-app':
        print(t['health'])
" 2>/dev/null || echo "")
check "spring-boot-app target exists" "$( [[ -n $target_up ]] && echo true || echo false )" \
  "Check prometheus.yml scrape config"
check "Target health = up" "$( [[ $target_up == 'up' ]] && echo true || echo false )" \
  "App not reachable from Prometheus"

jvm_count=$(curl -sf --max-time 10 \
  "$PROMETHEUS_URL/api/v1/query?query=jvm_memory_used_bytes" 2>/dev/null \
  | python3 -c "import json,sys; d=json.load(sys.stdin); print(len(d['data']['result']))" 2>/dev/null || echo 0)
check "jvm_memory_used_bytes has data (${jvm_count} series)" "$( [[ $jvm_count -gt 0 ]] && echo true || echo false )" \
  "Metrics endpoint not scraped yet"

# ─── 4 & 5. Grafana reachable + login ─────────────────────────────────────
section "4-5. Grafana reachable and login"
gf_db=$(curl -sf --max-time 10 "$GRAFANA_URL/api/health" 2>/dev/null \
  | python3 -c "import json,sys; print(json.load(sys.stdin).get('database',''))" 2>/dev/null || echo "")
check "Grafana API health OK" "$( [[ $gf_db == 'ok' ]] && echo true || echo false )" \
  "Grafana not started or wrong port"
gf_org=$(curl -sf --max-time 10 -u "$(auth)" "$GRAFANA_URL/api/org" 2>/dev/null)
check "Login with admin credentials" "$( [[ -n $gf_org ]] && echo true || echo false )" \
  "Check GF_SECURITY_ADMIN_PASSWORD env var"

# ─── 6. Datasource connected ───────────────────────────────────────────────
section "6. Prometheus datasource connection"
ds_list=$(curl -sf --max-time 10 -u "$(auth)" "$GRAFANA_URL/api/datasources" 2>/dev/null)
ds_id=$(echo "$ds_list" | python3 -c "
import json,sys
for d in json.load(sys.stdin):
    if d['type']=='prometheus': print(d['id']); break
" 2>/dev/null || echo "")
check "Prometheus datasource provisioned" "$( [[ -n $ds_id ]] && echo true || echo false )" \
  "Check provisioning/datasources/prometheus.yml"

if [[ -n $ds_id ]]; then
  ds_health=$(curl -sf --max-time 10 -u "$(auth)" "$GRAFANA_URL/api/datasources/$ds_id/health" 2>/dev/null \
    | python3 -c "import json,sys; print(json.load(sys.stdin).get('status',''))" 2>/dev/null || echo "")
  check "Datasource health check passes" "$( [[ $ds_health == 'OK' ]] && echo true || echo false )" \
    "Prometheus unreachable from Grafana container"
fi

# ─── 7. Dashboards provisioned ─────────────────────────────────────────────
section "7. Dashboards provisioned"
declare -A DASH_TITLES=( ["spring-boot-app"]="Application Performance" ["business-metrics"]="Business Metrics" ["infrastructure"]="Infrastructure" )
for uid in "spring-boot-app" "business-metrics" "infrastructure"; do
  found=$(curl -sf --max-time 10 -u "$(auth)" "$GRAFANA_URL/api/dashboards/uid/$uid" 2>/dev/null)
  check "Dashboard '$uid' (${DASH_TITLES[$uid]}) exists" \
    "$( [[ -n $found ]] && echo true || echo false )" \
    "Restart grafana to re-provision: docker-compose restart grafana"
done

# ─── 8. Business metrics exist ────────────────────────────────────────────
section "8. Business metric counters"
BIZ_METRICS=(user_registration_total project_creation_total application_submission_total auth_login_total minio_upload_total)
for m in "${BIZ_METRICS[@]}"; do
  count=$(curl -sf --max-time 10 "$PROMETHEUS_URL/api/v1/query?query=$m" 2>/dev/null \
    | python3 -c "import json,sys; print(len(json.load(sys.stdin)['data']['result']))" 2>/dev/null || echo 0)
  if [[ $count -gt 0 ]]; then
    check "$m  ($count series)" true
  else
    warn "$m has no data yet — trigger a business operation (register a user, create a project)"
  fi
done

# ─── 9. HTTP request metrics ──────────────────────────────────────────────
section "9. HTTP request metrics"
curl -so /dev/null "$APP_URL/actuator/health" --max-time 5 || true
sleep 2
http_count=$(curl -sf --max-time 10 \
  "$PROMETHEUS_URL/api/v1/query?query=http_server_requests_seconds_count" 2>/dev/null \
  | python3 -c "import json,sys; print(len(json.load(sys.stdin)['data']['result']))" 2>/dev/null || echo 0)
check "HTTP request metrics present ($http_count series)" \
  "$( [[ $http_count -gt 0 ]] && echo true || echo false )" \
  "Actuator not reachable or metrics not flushed"

p95=$(curl -sf --max-time 10 \
  "$PROMETHEUS_URL/api/v1/query?query=histogram_quantile(0.95,rate(http_server_requests_seconds_bucket%5B5m%5D))" 2>/dev/null \
  | python3 -c "import json,sys; print(len(json.load(sys.stdin)['data']['result']))" 2>/dev/null || echo 0)
check "p95 latency histogram_quantile computable" \
  "$( [[ $p95 -gt 0 ]] && echo true || echo false )" \
  "Check percentiles-histogram property in application.properties"

# ─── 10. Alert rules ──────────────────────────────────────────────────────
section "10. Alert rules loaded in Prometheus"
rules=$(curl -sf --max-time 10 "$PROMETHEUS_URL/api/v1/rules" 2>/dev/null)
rule_count=$(echo "$rules" | python3 -c "
import json,sys
d=json.load(sys.stdin)
n=sum(1 for g in d['data']['groups'] for r in g['rules'] if r.get('type')=='alerting')
print(n)
" 2>/dev/null || echo 0)
check "Alert rules loaded (count: $rule_count)" \
  "$( [[ $rule_count -ge 15 ]] && echo true || echo false )" \
  "Check rules.yml syntax: promtool check rules docker/prometheus/rules.yml"

for alert in HighHeapMemoryUsage HighHttpErrorRate SpringBootAppDown DatabaseConnectionPoolExhaustion; do
  found=$(echo "$rules" | python3 -c "
import json,sys
d=json.load(sys.stdin)
names=[r['name'] for g in d['data']['groups'] for r in g['rules']]
print('true' if '$alert' in names else 'false')
" 2>/dev/null || echo false)
  check "Alert rule '$alert' defined" "$found" "Missing from rules.yml"
done

# ─── 11. Data persistence ────────────────────────────────────────────────
if [[ $TEST_PERSISTENCE -eq 1 ]]; then
  section "11. Data persistence (restarting containers...)"
  before=$(curl -sf --max-time 10 \
    "$PROMETHEUS_URL/api/v1/query?query=prometheus_tsdb_head_samples_appended_total" 2>/dev/null \
    | python3 -c "import json,sys; r=json.load(sys.stdin)['data']['result']; print(r[0]['value'][1] if r else 0)" 2>/dev/null || echo 0)
  echo -e "  ${GRAY}📊  Samples before restart: $before${NC}"
  docker-compose restart prometheus grafana 2>/dev/null
  echo -e "  ${GRAY}⏳  Waiting 30s for services to come back...${NC}"
  sleep 30
  after=$(curl -sf --max-time 15 \
    "$PROMETHEUS_URL/api/v1/query?query=prometheus_tsdb_head_samples_appended_total" 2>/dev/null \
    | python3 -c "import json,sys; r=json.load(sys.stdin)['data']['result']; print(r[0]['value'][1] if r else 0)" 2>/dev/null || echo 0)
  check "Prometheus data persisted (samples: $after)" \
    "$( [[ $(echo "$after > 0" | bc -l 2>/dev/null || echo 0) -eq 1 ]] && echo true || echo false )" \
    "Check prometheus_data volume is declared"
  gf_after=$(curl -sf --max-time 10 "$GRAFANA_URL/api/health" 2>/dev/null \
    | python3 -c "import json,sys; print(json.load(sys.stdin).get('database',''))" 2>/dev/null || echo "")
  check "Grafana accessible after restart" "$( [[ $gf_after == 'ok' ]] && echo true || echo false )" ""
else
  section "11. Data persistence (skipped)"
  warn "Persistence test skipped — run with --test-persistence to enable"
fi

# ─── 12. README instructions ─────────────────────────────────────────────
section "12. README.md setup instructions"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
README="$SCRIPT_DIR/../README.md"
MONITORING="$SCRIPT_DIR/../MONITORING.md"
EXPLORER="$SCRIPT_DIR/../docs/metrics-explorer.md"
grep -q "docker-compose up"    "$README" 2>/dev/null && check "README contains docker-compose command"   true || check "README contains docker-compose command" false
grep -q "localhost:3030"       "$README" 2>/dev/null && check "README contains Grafana URL"              true || check "README contains Grafana URL"             false
grep -q "localhost:9090"       "$README" 2>/dev/null && check "README contains Prometheus URL"           true || check "README contains Prometheus URL"          false
grep -q "admin123"             "$README" 2>/dev/null && check "README contains admin credentials"        true || check "README contains admin credentials"       false
[[ -f "$MONITORING" ]] && check "MONITORING.md exists" true || check "MONITORING.md exists" false
[[ -f "$EXPLORER"   ]] && check "docs/metrics-explorer.md exists" true || check "docs/metrics-explorer.md exists" false

# ─── Summary ─────────────────────────────────────────────────────────────
echo ""
echo -e "${MAGENTA}══════════════════════════════════════════════════════════${NC}"
if [[ $FAIL -eq 0 ]]; then
  echo -e "${GREEN}  Results:  ✅ $PASS passed   ❌ $FAIL failed   ⚠️  $WARN warnings${NC}"
else
  echo -e "${RED}  Results:  ✅ $PASS passed   ❌ $FAIL failed   ⚠️  $WARN warnings${NC}"
fi
echo -e "${MAGENTA}══════════════════════════════════════════════════════════${NC}"

if [[ $FAIL -gt 0 ]]; then
  echo ""
  echo "  Quick fixes:"
  echo "    docker-compose ps              — check container status"
  echo "    docker-compose logs app         — app startup errors"
  echo "    docker-compose logs prometheus  — rule loading errors"
  echo "    docker-compose logs grafana     — provisioning errors"
  echo "    See docs/metrics-explorer.md for PromQL debugging"
  exit 1
fi
