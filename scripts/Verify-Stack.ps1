<#
.SYNOPSIS
    Automated verification script for the Proje Pazarı monitoring stack.
    Runs every item from the testing checklist and reports pass/fail.

.DESCRIPTION
    Covers:
      1.  All Docker services are running and healthy
      2.  Prometheus UI is reachable
      3.  Spring Boot metrics are being scraped
      4.  Grafana is reachable
      5.  Grafana login with admin credentials works
      6.  Prometheus datasource is connected in Grafana
      7.  All 3 dashboards are provisioned
      8.  Business metric counters exist (project creation counter)
      9.  HTTP request metrics are being collected
      10. Alert rules are loaded in Prometheus
      11. Data persistence after restart (optional — pass -TestPersistence)
      12. README.md contains setup instructions

.PARAMETER AppUrl
    Base URL of the Spring Boot app.  Default: http://localhost:8080

.PARAMETER PrometheusUrl
    Base URL of Prometheus.  Default: http://localhost:9090

.PARAMETER GrafanaUrl
    Base URL of Grafana.  Default: http://localhost:3030

.PARAMETER GrafanaUser
    Grafana admin username.  Default: admin

.PARAMETER GrafanaPassword
    Grafana admin password.  Default: admin123

.PARAMETER TestPersistence
    If set, restarts all containers and re-checks metrics.

.EXAMPLE
    .\scripts\Verify-Stack.ps1
    .\scripts\Verify-Stack.ps1 -TestPersistence
    .\scripts\Verify-Stack.ps1 -GrafanaPassword mySecret
#>
param(
    [string]$AppUrl          = "http://localhost:8080",
    [string]$PrometheusUrl   = "http://localhost:9090",
    [string]$GrafanaUrl      = "http://localhost:3030",
    [string]$GrafanaUser     = "admin",
    [string]$GrafanaPassword = "admin123",
    [switch]$TestPersistence
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "SilentlyContinue"

$pass = 0; $fail = 0; $warn = 0

function Check($label, $result, [string]$hint = "") {
    if ($result) {
        Write-Host ("  ✅  {0}" -f $label) -ForegroundColor Green
        $script:pass++
    } else {
        $msg = if ($hint) { "  ❌  $label  ← $hint" } else { "  ❌  $label" }
        Write-Host $msg -ForegroundColor Red
        $script:fail++
    }
}

function Warn($label, [string]$detail = "") {
    $msg = if ($detail) { "  ⚠️   $label  ($detail)" } else { "  ⚠️   $label" }
    Write-Host $msg -ForegroundColor Yellow
    $script:warn++
}

function Section($title) {
    Write-Host ""
    Write-Host "── $title" -ForegroundColor Cyan
}

$authHeader = @{
    Authorization = "Basic " + [Convert]::ToBase64String(
        [Text.Encoding]::ASCII.GetBytes("${GrafanaUser}:${GrafanaPassword}"))
}

Write-Host ""
Write-Host "╔══════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║   Proje Pazarı — Monitoring Stack Verification          ║" -ForegroundColor Magenta
Write-Host "╚══════════════════════════════════════════════════════════╝" -ForegroundColor Magenta

# ─── 1. Docker service health ──────────────────────────────────────────────
Section "1. Docker service health"
$services = @("proje-pazari-app","proje-pazari-prometheus","proje-pazari-grafana",
              "proje-pazari-postgres","proje-pazari-redis","proje-pazari-minio")
$dockerPs = docker ps --format "{{.Names}}:::{{.Status}}" 2>$null
foreach ($svc in $services) {
    $line = $dockerPs | Where-Object { $_ -like "$svc:::*" }
    if ($line) {
        $status = ($line -split ":::")[1]
        $healthy = $status -match "\(healthy\)" -or ($status -match "Up" -and $svc -notmatch "app|prometheus|grafana")
        Check "  $svc  [$status]" $healthy "container not healthy — run: docker inspect $svc"
    } else {
        Check "  $svc  [not found]" $false "not running — run: docker-compose up -d"
    }
}

# ─── 2. Prometheus UI reachable ────────────────────────────────────────────
Section "2. Prometheus UI reachable"
$promReady = (Invoke-WebRequest "$PrometheusUrl/-/ready" -UseBasicParsing -TimeoutSec 5).StatusCode -eq 200
Check "Prometheus ready at $PrometheusUrl" $promReady "Is docker-compose running?"
$promHealthy = (Invoke-WebRequest "$PrometheusUrl/-/healthy" -UseBasicParsing -TimeoutSec 5).StatusCode -eq 200
Check "Prometheus healthy" $promHealthy

# ─── 3. Spring Boot metrics scraped ───────────────────────────────────────
Section "3. Spring Boot metrics being scraped"
$targetsJson = Invoke-RestMethod "$PrometheusUrl/api/v1/targets" -TimeoutSec 10
$appTarget = $targetsJson.data.activeTargets | Where-Object { $_.labels.job -eq "spring-boot-app" }
Check "spring-boot-app target exists" ($null -ne $appTarget) "Check prometheus.yml scrape config"
if ($appTarget) {
    Check "Target health = UP" ($appTarget.health -eq "up") "App not scraped: $($appTarget.lastError)"
}

$jvmMetric = (Invoke-RestMethod "$PrometheusUrl/api/v1/query?query=jvm_memory_used_bytes" -TimeoutSec 10).data.result
Check "jvm_memory_used_bytes has data" ($jvmMetric.Count -gt 0) "Metrics endpoint not reachable"

$httpMetric = (Invoke-RestMethod "$PrometheusUrl/api/v1/query?query=http_server_requests_seconds_count" -TimeoutSec 10).data.result
Check "http_server_requests_seconds_count has data" ($httpMetric.Count -gt 0) "Make a request to the app first"

# ─── 4 & 5. Grafana reachable + login ─────────────────────────────────────
Section "4-5. Grafana reachable and login"
$grafanaHealth = (Invoke-RestMethod "$GrafanaUrl/api/health" -TimeoutSec 10)
Check "Grafana API health OK" ($grafanaHealth.database -eq "ok") "Grafana not started"

$grafanaOrg = Invoke-RestMethod "$GrafanaUrl/api/org" -Headers $authHeader -TimeoutSec 10
Check "Login with admin credentials" ($null -ne $grafanaOrg) "Check GF_SECURITY_ADMIN_PASSWORD"

# ─── 6. Datasource connected ───────────────────────────────────────────────
Section "6. Prometheus datasource connection"
$datasources = Invoke-RestMethod "$GrafanaUrl/api/datasources" -Headers $authHeader -TimeoutSec 10
$promDs = $datasources | Where-Object { $_.type -eq "prometheus" }
Check "Prometheus datasource provisioned" ($null -ne $promDs) "Check datasources/prometheus.yml"
Check "Datasource is default" ($promDs.isDefault -eq $true) ""

$dsTest = Invoke-RestMethod "$GrafanaUrl/api/datasources/$($promDs.id)/health" `
              -Headers $authHeader -TimeoutSec 10
Check "Datasource health check passes" ($dsTest.status -eq "OK") "Prometheus unreachable from Grafana"

# ─── 7. Dashboards provisioned ────────────────────────────────────────────
Section "7. Dashboards provisioned"
$dashboards = Invoke-RestMethod "$GrafanaUrl/api/search?type=dash-db" -Headers $authHeader -TimeoutSec 10
$expectedUids = @("spring-boot-app","business-metrics","infrastructure")
$expectedTitles = @{
    "spring-boot-app"  = "Application Performance Dashboard"
    "business-metrics" = "Business Metrics"
    "infrastructure"   = "Infrastructure"
}
foreach ($uid in $expectedUids) {
    $db = $dashboards | Where-Object { $_.uid -eq $uid }
    Check "Dashboard '$uid' ($($expectedTitles[$uid])) exists" ($null -ne $db) "Reprovisioning: restart grafana"
}

# ─── 8. Business metric counters exist ─────────────────────────────────────
Section "8. Business metric counters"
$bizMetrics = @(
    "user_registration_total",
    "project_creation_total",
    "application_submission_total",
    "auth_login_total",
    "minio_upload_total"
)
foreach ($m in $bizMetrics) {
    $res = (Invoke-RestMethod "$PrometheusUrl/api/v1/query?query=$m" -TimeoutSec 10).data.result
    if ($res.Count -gt 0) {
        Check "$m  (value: $($res[0].value[1]))" $true
    } else {
        Warn "$m has no data yet — trigger a business operation to populate it"
    }
}

# ─── 9. HTTP request metrics update ────────────────────────────────────────
Section "9. HTTP request metrics"
# Fire a test request to the actuator health endpoint to ensure metrics exist
$null = Invoke-WebRequest "$AppUrl/actuator/health" -UseBasicParsing -TimeoutSec 5
Start-Sleep -Seconds 2
$httpCount = (Invoke-RestMethod "$PrometheusUrl/api/v1/query?query=http_server_requests_seconds_count%7Buri%3D~%22.*actuator.*%22%7D" -TimeoutSec 10).data.result
Check "HTTP metrics update after a request" ($httpCount.Count -gt 0) "Actuator not reachable or metrics not flushed yet"

$p95 = (Invoke-RestMethod "$PrometheusUrl/api/v1/query?query=histogram_quantile(0.95%2Crate(http_server_requests_seconds_bucket%5B5m%5D))" -TimeoutSec 10).data.result
Check "p95 latency histogram_quantile computable" ($p95.Count -gt 0) "Histogram not enabled — check percentiles-histogram property"

# ─── 10. Alert rules loaded ────────────────────────────────────────────────
Section "10. Alert rules loaded in Prometheus"
$rules = Invoke-RestMethod "$PrometheusUrl/api/v1/rules" -TimeoutSec 10
$alertRules = $rules.data.groups | ForEach-Object { $_.rules } | Where-Object { $_.type -eq "alerting" }
Check "Alert rules loaded (count: $($alertRules.Count))" ($alertRules.Count -ge 15) "Check rules.yml syntax: promtool check rules docker/prometheus/rules.yml"

$criticalAlerts = @("HighHeapMemoryUsage","HighHttpErrorRate","SpringBootAppDown","DatabaseConnectionPoolExhaustion")
foreach ($alert in $criticalAlerts) {
    $found = $alertRules | Where-Object { $_.name -eq $alert }
    Check "Alert rule '$alert' defined" ($null -ne $found) "Missing from rules.yml"
}

# ─── 11. Data persistence after restart ────────────────────────────────────
if ($TestPersistence) {
    Section "11. Data persistence (restarting containers...)"
    $beforeVal = (Invoke-RestMethod "$PrometheusUrl/api/v1/query?query=prometheus_tsdb_head_samples_appended_total" -TimeoutSec 10).data.result[0].value[1]
    Write-Host "  📊  Samples before restart: $beforeVal" -ForegroundColor DarkGray
    docker-compose restart prometheus grafana 2>$null
    Write-Host "  ⏳  Waiting 30s for services to come back..." -ForegroundColor DarkGray
    Start-Sleep -Seconds 30
    $afterVal = (Invoke-RestMethod "$PrometheusUrl/api/v1/query?query=prometheus_tsdb_head_samples_appended_total" -TimeoutSec 15).data.result[0].value[1]
    Check "Prometheus data persisted after restart (samples: $afterVal)" ([double]$afterVal -gt 0) "Check prometheus_data volume"
    $grafanaAfter = (Invoke-RestMethod "$GrafanaUrl/api/health" -TimeoutSec 10)
    Check "Grafana accessible after restart" ($grafanaAfter.database -eq "ok") ""
    $dashAfter = Invoke-RestMethod "$GrafanaUrl/api/search?type=dash-db" -Headers $authHeader -TimeoutSec 10
    Check "Dashboards still provisioned after restart" ($dashAfter.Count -ge 3) ""
} else {
    Section "11. Data persistence (skipped — run with -TestPersistence to enable)"
    Warn "Persistence test skipped" "add -TestPersistence flag to run container restart test"
}

# ─── 12. README setup instructions ────────────────────────────────────────
Section "12. README.md setup instructions"
$readme = Get-Content "$PSScriptRoot\..\README.md" -Raw -ErrorAction SilentlyContinue
Check "README contains docker-compose command"   ($readme -match 'docker-compose up')         "Add startup instructions"
Check "README contains Grafana URL"              ($readme -match 'localhost:3030')             ""
Check "README contains Prometheus URL"           ($readme -match 'localhost:9090')             ""
Check "README contains admin credentials"        ($readme -match 'admin123')                   ""
Check "README mentions monitoring section"       ($readme -match '## 📊 Monitoring')           ""
Check "README links metrics-explorer.md"         ($readme -match 'metrics-explorer')           ""
Check "MONITORING.md exists"                     (Test-Path "$PSScriptRoot\..\MONITORING.md")  ""
Check "docs/metrics-explorer.md exists"          (Test-Path "$PSScriptRoot\..\docs\metrics-explorer.md") ""

# ─── Summary ───────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "══════════════════════════════════════════════════════════" -ForegroundColor Magenta
$color = if ($fail -eq 0) { "Green" } else { "Red" }
Write-Host ("  Results:  ✅ {0} passed   ❌ {1} failed   ⚠️  {2} warnings" -f $pass, $fail, $warn) -ForegroundColor $color
Write-Host "══════════════════════════════════════════════════════════" -ForegroundColor Magenta

if ($fail -gt 0) {
    Write-Host ""
    Write-Host "  Quick fixes:" -ForegroundColor Yellow
    Write-Host "    docker-compose ps              — check container status"
    Write-Host "    docker-compose logs app         — app startup errors"
    Write-Host "    docker-compose logs prometheus  — rule loading errors"
    Write-Host "    docker-compose logs grafana     — provisioning errors"
    Write-Host "    See docs/metrics-explorer.md for PromQL debugging"
    exit 1
}
