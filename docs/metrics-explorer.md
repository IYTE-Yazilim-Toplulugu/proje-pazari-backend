# 🔭 Metrics Explorer — Proje Pazarı

> Quick reference for querying, filtering, and exploring every metric exposed by the monitoring stack.  
> Open Prometheus at **http://localhost:9090** and paste any PromQL query below directly into the expression bar.

---

## Table of Contents

- [Filtering by Tags](#filtering-by-tags)
- [Custom Business Metrics](#custom-business-metrics)
- [JVM Metrics](#jvm-metrics)
- [HTTP Request Metrics](#http-request-metrics)
- [Database Metrics (HikariCP)](#database-metrics-hikaricp)
- [Cache Metrics (Redis)](#cache-metrics-redis)
- [Storage Metrics (MinIO)](#storage-metrics-minio)
- [Authentication Metrics](#authentication-metrics)
- [PromQL Cheat Sheet](#promql-cheat-sheet)

---

## Filtering by Tags

Every metric carries these three common labels you can use to filter:

| Label | Values | Example |
|---|---|---|
| `application` | `proje-pazari` | `{application="proje-pazari"}` |
| `environment` | `dev` · `staging` · `prod` | `{environment="prod"}` |
| `layer` | `business` · `security` · `storage` | `{layer="business"}` |

**Filter by environment in any query:**
```promql
user_registration_total{environment="prod"}
```

**Filter by layer (show only storage metrics):**
```promql
{layer="storage"}
```

**Compare environments side-by-side:**
```promql
sum by (environment) (rate(user_registration_total[5m]))
```

---

## Custom Business Metrics

### `user_registration_total`

| Label | Values |
|---|---|
| `status` | `success` · `failure` |
| `layer` | `business` |

```promql
# Registration success rate (last 5 min)
rate(user_registration_total{status="success"}[5m])

# Failure ratio (%)
rate(user_registration_total{status="failure"}[5m])
  / rate(user_registration_total[5m]) * 100

# Cumulative registrations today
increase(user_registration_total{status="success"}[24h])

# Per-environment comparison
sum by (environment) (
  increase(user_registration_total{status="success"}[24h])
)
```

---

### `project_creation_total`

| Label | Values |
|---|---|
| `status` | `success` · `failure` |
| `layer` | `business` |

```promql
# Projects created per minute
rate(project_creation_total{status="success"}[1m]) * 60

# Failure rate
rate(project_creation_total{status="failure"}[5m])

# Last 7 days total
increase(project_creation_total{status="success"}[7d])
```

---

### `application_submission_total`

| Label | Values |
|---|---|
| `status` | `success` · `failure` |
| `layer` | `business` |

```promql
# Submission success rate
rate(application_submission_total{status="success"}[5m])

# All submissions by status (for pie chart)
sum by (status) (application_submission_total)

# 30-day trend
increase(application_submission_total{status="success"}[30d])
```

---

### `auth_login_total`

| Label | Values |
|---|---|
| `status` | `success` · `failure` |
| `layer` | `security` |

```promql
# Login failure rate (alert threshold: >30%)
rate(auth_login_total{status="failure"}[5m])
  / rate(auth_login_total[5m]) * 100

# Failed logins per second (surge detection)
rate(auth_login_total{status="failure"}[1m])

# Auth success rate (%)
rate(auth_login_total{status="success"}[5m])
  / rate(auth_login_total[5m]) * 100
```

---

### `minio_upload_total` · `minio_download_total` · `minio_delete_total`

| Label | Values |
|---|---|
| `status` | `success` · `failure` |
| `layer` | `storage` |

```promql
# Upload failure rate
rate(minio_upload_total{status="failure"}[5m])
  / rate(minio_upload_total[5m]) * 100

# All storage ops by operation type
sum by (__name__) ({layer="storage", status="success"})

# Storage activity rate
sum(rate({layer="storage", status="success"}[5m]))
```

---

### `minio_upload_duration_seconds`

| Label | Values |
|---|---|
| `layer` | `storage` |

```promql
# Upload p95 latency
histogram_quantile(0.95, rate(minio_upload_duration_seconds_bucket[5m]))

# Upload p99 latency
histogram_quantile(0.99, rate(minio_upload_duration_seconds_bucket[5m]))

# Average upload duration
rate(minio_upload_duration_seconds_sum[5m])
  / rate(minio_upload_duration_seconds_count[5m])
```

---

## JVM Metrics

```promql
# ── Memory ────────────────────────────────────────────────────────────────────
# Heap used (bytes)
jvm_memory_used_bytes{area="heap"}

# Heap usage percentage
jvm_memory_used_bytes{area="heap"}
  / jvm_memory_max_bytes{area="heap"} * 100

# Non-heap used
jvm_memory_used_bytes{area="nonheap"}

# ── Garbage Collection ────────────────────────────────────────────────────────
# GC pause rate
rate(jvm_gc_pause_seconds_count[5m])

# GC pause duration p99
histogram_quantile(0.99, rate(jvm_gc_pause_seconds_bucket[5m]))

# Time spent in GC (%)
rate(jvm_gc_pause_seconds_sum[5m]) * 100

# ── Threads ───────────────────────────────────────────────────────────────────
jvm_threads_live_threads
jvm_threads_daemon_threads
jvm_threads_peak_threads

# ── Class Loading ─────────────────────────────────────────────────────────────
jvm_classes_loaded_classes
```

---

## HTTP Request Metrics

```promql
# ── Request Rate ──────────────────────────────────────────────────────────────
# Requests per second (all endpoints)
sum(rate(http_server_requests_seconds_count[5m]))

# Requests per second by URI
sum by (uri) (rate(http_server_requests_seconds_count[5m]))

# ── Error Rates ───────────────────────────────────────────────────────────────
# 5xx error rate (%)
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
  / sum(rate(http_server_requests_seconds_count[5m])) * 100

# 4xx rate
sum(rate(http_server_requests_seconds_count{status=~"4.."}[5m]))

# ── Latency Percentiles ───────────────────────────────────────────────────────
# p50 (median) response time
histogram_quantile(0.50, sum by (le) (
  rate(http_server_requests_seconds_bucket[5m])))

# p95 response time (alert threshold: >2s)
histogram_quantile(0.95, sum by (le) (
  rate(http_server_requests_seconds_bucket[5m])))

# p99 response time
histogram_quantile(0.99, sum by (le) (
  rate(http_server_requests_seconds_bucket[5m])))

# p95 per endpoint
histogram_quantile(0.95, sum by (le, uri) (
  rate(http_server_requests_seconds_bucket[5m])))

# SLO compliance — % of requests under 200ms
sum(rate(http_server_requests_seconds_bucket{le="0.2"}[5m]))
  / sum(rate(http_server_requests_seconds_count[5m])) * 100
```

---

## Database Metrics (HikariCP)

```promql
# Active connections
hikaricp_connections_active

# Idle connections
hikaricp_connections_idle

# Pending connection requests
hikaricp_connections_pending

# Pool utilisation (%)
hikaricp_connections_active
  / hikaricp_connections_max * 100

# Connection timeouts (total)
hikaricp_connections_timeout_total

# Connection acquire p99 latency
histogram_quantile(0.99, rate(hikaricp_connections_acquire_seconds_bucket[5m]))

# Connection usage duration p95
histogram_quantile(0.95, rate(hikaricp_connections_usage_seconds_bucket[5m]))
```

---

## Cache Metrics (Redis)

```promql
# Cache hit ratio (%)
sum(rate(cache_gets_total{result="hit"}[5m]))
  / sum(rate(cache_gets_total[5m])) * 100

# Cache hit rate by cache name
sum by (name) (rate(cache_gets_total{result="hit"}[5m]))

# Cache miss rate
rate(cache_gets_total{result="miss"}[5m])

# Cache evictions
rate(cache_removals_total[5m])

# Redis command latency p95 (Lettuce)
histogram_quantile(0.95, sum by (le) (
  rate(lettuce_command_completion_seconds_bucket[5m])))

# Redis command rate by type
sum by (command) (rate(lettuce_command_completion_seconds_count[5m]))
```

---

## Storage Metrics (MinIO)

> MinIO also exposes its own Prometheus endpoint at `:9000/minio/v2/metrics/cluster`.  
> The metrics below are the application-side counters from `BusinessMetricsService`.

```promql
# All MinIO operations per second
sum(rate({layer="storage"}[5m]))

# Upload success rate
rate(minio_upload_total{status="success"}[5m])

# Upload failure ratio (%)
rate(minio_upload_total{status="failure"}[5m])
  / rate(minio_upload_total[5m]) * 100

# Download (presigned URL generation) rate
rate(minio_download_total{status="success"}[5m])

# Delete rate
rate(minio_delete_total{status="success"}[5m])
```

---

## Authentication Metrics

```promql
# Login success rate (users per minute)
rate(auth_login_total{status="success"}[1m]) * 60

# Login failure rate
rate(auth_login_total{status="failure"}[1m]) * 60

# Failure ratio — alert if >30%
rate(auth_login_total{status="failure"}[5m])
  / rate(auth_login_total[5m]) * 100

# Brute-force detection: spike in failures per second
rate(auth_login_total{status="failure"}[1m])
```

---

## PromQL Cheat Sheet

| Goal | PromQL pattern |
|---|---|
| Rate of a counter over 5 min | `rate(metric_total[5m])` |
| Increase over 24 h | `increase(metric_total[24h])` |
| Percentage of total | `rate(a[5m]) / rate(b[5m]) * 100` |
| Histogram p95 | `histogram_quantile(0.95, rate(metric_bucket[5m]))` |
| Sum across labels | `sum by (label) (rate(metric[5m]))` |
| Filter by label | `metric{label="value"}` |
| Filter by environment | `metric{environment="prod"}` |
| Filter by layer | `metric{layer="business"}` |
| Regex label match | `metric{uri=~"/api/v1/.*"}` |
| Negative label match | `metric{status!="success"}` |
| Absent / down alert | `absent(up{job="spring-boot-app"})` |
| Rate then aggregate | `sum(rate(http_server_requests_seconds_count[5m]))` |
| Compare two time ranges | `rate(m[5m]) / rate(m[5m] offset 1d)` |

### Useful label names

| Label | Carried by |
|---|---|
| `application` | All metrics (global tag) |
| `environment` | All metrics (global tag) |
| `layer` | Custom business metrics |
| `status` | Custom business metrics (`success`/`failure`) |
| `uri` | HTTP server request metrics |
| `method` | HTTP server request metrics |
| `outcome` | HTTP server request metrics (`SUCCESS`, `CLIENT_ERROR`, `SERVER_ERROR`) |
| `pool` | HikariCP metrics |
| `name` | Cache metrics |
| `command` | Redis/Lettuce metrics |
| `area` | JVM memory metrics (`heap`/`nonheap`) |
| `id` | JVM memory pool metrics |

### Time range quick reference

| Window | Use for |
|---|---|
| `[1m]` | Real-time spike detection |
| `[5m]` | Dashboard panels (stable) |
| `[15m]` | Alert rules (reduce flapping) |
| `[1h]` | Hourly summaries |
| `[24h]` | Daily totals |
| `[7d]` | Weekly trends |
| `[30d]` | Monthly KPIs |
