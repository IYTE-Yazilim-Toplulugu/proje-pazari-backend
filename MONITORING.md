# 📊 MONITORING.md — Proje Pazarı Observability Guide

> Complete reference for the monitoring stack: architecture, metrics, dashboards, alerts, and troubleshooting.

---

## 📋 Table of Contents

- [Architecture](#architecture)
- [Metrics Glossary](#metrics-glossary)
- [Custom Metrics Documentation](#custom-metrics-documentation)
- [Dashboards](#dashboards)
- [Alert Runbook](#alert-runbook)
- [Troubleshooting Guide](#troubleshooting-guide)

---

## Architecture

### Observability Stack

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Docker Network: proje-pazari-network            │
│                                                                         │
│  ┌────────────────────┐    scrape /actuator/prometheus every 15s        │
│  │  Spring Boot App   │◄────────────────────────────────────────────┐  │
│  │  :8080             │                                             │  │
│  │                    │  Micrometer                                 │  │
│  │  ┌──────────────┐  │  ┌─────────────────────────────────────┐   │  │
│  │  │ Controllers  │──┼─►│ BusinessMetricsService              │   │  │
│  │  │ Handlers     │  │  │  • user.registration.total          │   │  │
│  │  │ MinIO Adapter│  │  │  • project.creation.total           │   │  │
│  │  └──────────────┘  │  │  • application.submission.total     │   │  │
│  │                    │  │  • auth.login.total                  │   │  │
│  │  Auto-instrumented │  │  • minio.upload/download/delete.*   │   │  │
│  │  • HikariCP        │  └─────────────────────────────────────┘   │  │
│  │  • JVM / CPU       │                                             │  │
│  │  • HTTP requests   │                                             │  │
│  │  • Lettuce/Redis   │                                             │  │
│  │  • Tomcat sessions │                                             │  │
│  └────────────────────┘                                             │  │
│                                                                     │  │
│  ┌────────────────────┐    evaluate rules.yml every 15s             │  │
│  │   Prometheus       │────────────────────────────────────────────►│  │
│  │   :9090            │                                             │  │
│  │                    │◄────── alert state ──────────────────────┐  │  │
│  │  • TSDB storage    │                                          │  │  │
│  │  • PromQL engine   │                                          │  │  │
│  │  • Alert manager   │                                          │  │  │
│  └────────┬───────────┘                                          │  │  │
│           │ PromQL queries                                        │  │  │
│           ▼                                                       │  │  │
│  ┌────────────────────┐                                          │  │  │
│  │   Grafana          │──── notifications ──► Email / Slack /    │  │  │
│  │   :3030            │                       Discord            │  │  │
│  │                    │                                          │  │  │
│  │  Dashboards:       │                                          │  │  │
│  │  • Spring Boot App │                                          │  │  │
│  │  • Business Metrics│                                          │  │  │
│  │  • Infrastructure  │                                          │  │  │
│  └────────────────────┘                                          │  │  │
│                                                                   │  │  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────────┐ │  │  │
│  │PostgreSQL│  │  Redis   │  │  MinIO   │  │ Elasticsearch    │ │  │  │
│  │  :5432   │  │  :6379   │  │:9002/9003│  │    :9200         │ │  │  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────────┘ │  │  │
└─────────────────────────────────────────────────────────────────────────┘
```

### Data Flow

```
HTTP Request
     │
     ▼
Spring Boot Controller
     │
     ├──► Handler (business logic)
     │         │
     │         ├──► BusinessMetricsService.increment*()
     │         │         │
     │         │         ▼
     │         │    MeterRegistry (in-memory)
     │         │
     │         └──► Repository / MinIO / Redis
     │
     └──► Micrometer HTTP instrumentation (auto)
               │
               ▼
         /actuator/prometheus   ◄── Prometheus scrape (15s)
               │
               ▼
         Prometheus TSDB  ──► Grafana dashboards
               │
               └──► rules.yml evaluation ──► Grafana alerts ──► Notifications
```

### Configuration Files

```
docker/
├── prometheus/
│   ├── prometheus.yml          # Global config + scrape jobs
│   └── rules.yml               # 19 alert rules / 8 groups
└── grafana/
    └── provisioning/
        ├── datasources/
        │   └── prometheus.yml  # Prometheus datasource
        ├── dashboards/
        │   ├── dashboard.yml                    # File provider config
        │   ├── spring-boot-dashboard.json       # 16 panels
        │   ├── business-metrics-dashboard.json  # 17 panels
        │   └── infrastructure-dashboard.json    # 24 panels
        └── alerting/
            ├── contact-points.yml       # 6 contact points
            └── notification-policies.yml # Routing tree

src/main/java/…/infrastructure/metrics/
└── BusinessMetricsService.java  # Custom metric definitions

src/main/resources/
└── application.properties       # management.* config
```

---

## Metrics Glossary

### JVM Metrics (auto — Micrometer JVM binder)

| Metric | Unit | Description |
|---|---|---|
| `jvm_memory_used_bytes` | bytes | Current memory used. Tags: `area={heap,nonheap}`, `id` |
| `jvm_memory_committed_bytes` | bytes | Memory committed by OS to JVM |
| `jvm_memory_max_bytes` | bytes | Maximum memory available (-1 if unlimited) |
| `jvm_threads_live_threads` | count | Current live thread count |
| `jvm_threads_daemon_threads` | count | Current daemon thread count |
| `jvm_threads_peak_threads` | count | Peak live thread count since JVM start |
| `jvm_threads_states_threads` | count | Threads by state. Tag: `state` |
| `jvm_gc_pause_seconds` | seconds | GC pause duration histogram. Tags: `action`, `cause` |
| `jvm_gc_memory_promoted_bytes` | bytes | Bytes promoted from young to old generation |
| `jvm_classes_loaded_classes` | count | Currently loaded classes |
| `jvm_buffer_memory_used_bytes` | bytes | Buffer pool memory used. Tag: `id` |

### Process & System Metrics (auto)

| Metric | Unit | Description |
|---|---|---|
| `process_cpu_usage` | ratio 0–1 | CPU used by the JVM process |
| `system_cpu_usage` | ratio 0–1 | CPU used by the whole system |
| `process_uptime_seconds` | seconds | Time since JVM start |
| `process_start_time_seconds` | epoch | JVM start timestamp |
| `system_load_average_1m` | ratio | System 1-minute load average |

### HTTP Server Metrics (auto — Spring MVC instrumentation)

| Metric | Unit | Description |
|---|---|---|
| `http_server_requests_seconds_count` | count | Total request count. Tags: `method`, `uri`, `status`, `exception` |
| `http_server_requests_seconds_sum` | seconds | Cumulative response time |
| `http_server_requests_seconds_bucket` | count | Histogram buckets for percentile calculation |
| `http_server_requests_seconds_max` | seconds | Max observed response time in rolling window |

### HikariCP (Database Connection Pool — auto)

| Metric | Unit | Description |
|---|---|---|
| `hikaricp_connections_active` | count | Connections currently in use. Tag: `pool` |
| `hikaricp_connections_idle` | count | Connections idle in the pool |
| `hikaricp_connections_pending` | count | Threads waiting for a connection |
| `hikaricp_connections_max` | count | `maximumPoolSize` setting |
| `hikaricp_connections_min` | count | `minimumIdle` setting |
| `hikaricp_connections_timeout_total` | count | Total connection acquisition timeouts |
| `hikaricp_connections_acquire_seconds` | seconds | Time to acquire a connection (histogram) |
| `hikaricp_connections_usage_seconds` | seconds | Time connection was held by caller (histogram) |
| `hikaricp_connections_creation_seconds` | seconds | Time to create a new physical connection (histogram) |

### Redis / Lettuce (auto)

| Metric | Unit | Description |
|---|---|---|
| `lettuce_command_completion_duration_seconds` | seconds | Full round-trip duration (histogram) |
| `lettuce_command_firstresponse_duration_seconds` | seconds | Time to first byte of response (histogram) |
| `cache_gets_total` | count | Cache lookups. Tags: `name`, `result={hit,miss}` |
| `cache_puts_total` | count | Cache put operations. Tag: `name` |
| `cache_evictions_total` | count | Cache evictions. Tag: `name` |

### Tomcat Sessions (auto)

| Metric | Unit | Description |
|---|---|---|
| `tomcat_sessions_active_current_sessions` | count | Sessions active right now |
| `tomcat_sessions_active_max_sessions` | count | Max concurrent sessions observed |
| `tomcat_sessions_created_sessions_total` | count | Total sessions created |
| `tomcat_sessions_expired_sessions_total` | count | Total sessions expired |
| `tomcat_sessions_rejected_sessions_total` | count | Sessions rejected (max active limit) |

---

## Custom Metrics Documentation

All custom metrics are defined in `BusinessMetricsService` and injected into handlers/adapters via Spring's `@RequiredArgsConstructor`.

### User Registration

```
Metric  : user_registration_total
Type    : Counter
Labels  : application, status={success, failure}
Source  : RegisterUserHandler.handle()
```

| Event | When incremented |
|---|---|
| `status=success` | User saved, verification email sent, `ApiResponse.created()` returned |
| `status=failure` | Validation error, invalid IYTE email, duplicate email |

**Example PromQL:**
```promql
# Registration success rate (last 5 min)
rate(user_registration_total{status="success"}[5m])

# Cumulative total
sum(user_registration_total) by (status)

# Failure ratio
sum(user_registration_total{status="failure"})
/ sum(user_registration_total) * 100
```

---

### Project Creation

```
Metric  : project_creation_total
Type    : Counter
Labels  : application, status={success, failure}
Source  : CreateProjectHandler.handle()
```

| Event | When incremented |
|---|---|
| `status=success` | Project persisted, event published, `ApiResponse.created()` returned |
| `status=failure` | Validation error, owner not found |

**Example PromQL:**
```promql
# New projects per hour
increase(project_creation_total{status="success"}[1h])
```

---

### Application Submission

```
Metric  : application_submission_total
Type    : Counter
Labels  : application, status={success, failure}
Source  : SubmitApplicationHandler.handle()
```

| Event | When incremented |
|---|---|
| `status=success` | Application saved, notification event published |
| `status=failure` | Validation error, project/user not found, duplicate application |

**Example PromQL:**
```promql
# Submission success rate (last 10 min)
sum(rate(application_submission_total{status="success"}[10m]))
/ sum(rate(application_submission_total[10m])) * 100
```

---

### Authentication

```
Metric  : auth_login_total
Type    : Counter
Labels  : application, status={success, failure}
Source  : LoginUserHandler.handle()
```

| Event | When incremented |
|---|---|
| `status=success` | JWT + refresh token generated, `ApiResponse.success()` returned |
| `status=failure` | Validation error, user not found, account inactive, wrong password |

> ⚠️ A high `status=failure` rate may indicate a brute-force attack.  
> The `AuthenticationFailureSurge` alert fires at > 5 failures/s.

**Example PromQL:**
```promql
# Auth success rate
sum(rate(auth_login_total{status="success"}[5m]))
/ sum(rate(auth_login_total[5m])) * 100

# Failure rate over time
rate(auth_login_total{status="failure"}[1m])
```

---

### MinIO Upload

```
Metric  : minio_upload_total
Type    : Counter
Labels  : application, status={success, failure}

Metric  : minio_upload_duration_seconds
Type    : Timer (histogram + summary)
Labels  : application
Source  : MinioStorageAdapter.store()
```

The `store()` method is wrapped with `Timer.record()`, so every call — success or failure — contributes to `minio_upload_duration_seconds`.

**Example PromQL:**
```promql
# Upload throughput (per second)
rate(minio_upload_total{status="success"}[1m])

# p99 upload duration
histogram_quantile(0.99,
  sum(rate(minio_upload_duration_seconds_bucket[5m])) by (le)
)
```

---

### MinIO Download (Presigned URL generation)

```
Metric  : minio_download_total
Type    : Counter
Labels  : application, status={success, failure}
Source  : MinioStorageAdapter.generatePresignedUrl()
```

Each call to `generatePresignedUrl()` represents a client preparing to download a file.

---

### MinIO Delete

```
Metric  : minio_delete_total
Type    : Counter
Labels  : application, status={success, failure}
Source  : MinioStorageAdapter.delete()
```

---

### Adding New Custom Metrics

1. Inject `MeterRegistry` into `BusinessMetricsService` constructor.
2. Define the meter (Counter / Timer / Gauge) as a `final` field.
3. Add a public `increment*()` or `record*()` method.
4. Inject `BusinessMetricsService` into the handler/service that needs it.
5. Call the method at the appropriate success/failure code path.

**Example — adding a Gauge for active search queries:**
```java
// In BusinessMetricsService constructor:
private final AtomicInteger activeSearchQueries = new AtomicInteger(0);

Gauge.builder("search.queries.active", activeSearchQueries, AtomicInteger::get)
     .description("Currently executing Elasticsearch search queries")
     .register(registry);

// Public methods:
public void incrementActiveSearchQueries() { activeSearchQueries.incrementAndGet(); }
public void decrementActiveSearchQueries() { activeSearchQueries.decrementAndGet(); }
```

---

## Dashboards

### Dashboard Screenshots

> Screenshots below reflect the provisioned dashboards when all services are running (`docker-compose up`).
> Capture your own by visiting each URL and pressing **Share → Export → Copy link** in Grafana.

| Dashboard | Preview |
|---|---|
| **Application Performance** | ![Application Performance Dashboard](docs/screenshots/app-performance-dashboard.png) |
| **Business Metrics** | ![Business Metrics Dashboard](docs/screenshots/business-metrics-dashboard.png) |
| **Infrastructure** | ![Infrastructure Dashboard](docs/screenshots/infrastructure-dashboard.png) |

> 💡 To add screenshots: run the stack, navigate to each dashboard, take a screenshot, and save it under `docs/screenshots/`.

---

### Application Performance Dashboard (`spring-boot-app`)

**URL:** http://localhost:3030/d/spring-boot-app

| Row | Panels | Key metrics |
|---|---|---|
| JVM Memory | Heap Memory, Non-Heap Memory | `jvm_memory_used_bytes`, `jvm_memory_max_bytes` |

| CPU & Threads | CPU Usage, Thread Count | `process_cpu_usage`, `jvm_threads_live_threads` |
| HTTP Requests | Request Rate, Response Time p50/p95/p99, Status Distribution | `http_server_requests_seconds_*` |
| Sessions & DB | Active Sessions, Connection Pool, Query Performance | `tomcat_sessions_*`, `hikaricp_connections_*` |
| Business Metrics | Registration, Project, Submission, Auth, MinIO rates | Custom counters + timers |

**Template variables:**
- `$datasource` — Prometheus datasource selector
- `$application` — populated from `application` label (`proje-pazari`)

---

### Business Metrics (`business-metrics`)

**URL:** http://localhost:3030/d/business-metrics

| Row | Panels | Key metrics |
|---|---|---|
| Key Totals | Total Users, Projects, Submissions, Auth Success % | Cumulative counters |
| New Registrations | Last 24 h / 7 d / 30 d | `increase(user_registration_total[Xd])` |
| Trends | Registration Trend, Projects Trend | `increase(...[$__rate_interval])` |
| Submissions | By Status (pie), Trend | `application_submission_total` |
| Popular Tags | Top 10 Tags (bar), Category Donut | `project_tag_total` ⚠️ requires extra instrumentation |
| User Activity | HTTP Heatmap | `http_server_requests_seconds_count` |
| Auth & Storage | Auth pie, Login timeline, File storage | Custom counters |

> ⚠️ **Project Tags / Category panels** require adding `project_tag_total{tag="…"}` and `project_category_total{category="…"}` counters to `BusinessMetricsService` and incrementing them in `CreateProjectHandler`.

---

### Infrastructure (`infrastructure`)

**URL:** http://localhost:3030/d/infrastructure

| Row | Panels | Key metrics |
|---|---|---|
| PostgreSQL | Active/Pending/Idle/Timeout stats, Pool timeseries, Connection latency | `hikaricp_connections_*` |
| Redis | Command latency, Command rate by type, Cache hit ratio, Cache ops by name | `lettuce_command_*`, `cache_gets_total` |
| MinIO | Upload/Download/Delete/Failure stats, Operation rate, Upload duration | Custom `minio_*` |
| Elasticsearch | Request latency, Rate by method, Repository invocations, Repo latency | `elasticsearch_client_*`, `spring_data_repository_invocations_*` |
| Containers (cAdvisor) | CPU, Memory, Network I/O, Disk I/O per container | `container_cpu_usage_seconds_total`, `container_memory_usage_bytes` |

> ⚠️ **Container panels** require adding the [cAdvisor](https://github.com/google/cadvisor) service to `docker-compose.yml` and a scrape job to `prometheus.yml`.

---

## Alert Runbook

### JVM Memory Alerts

#### `HighHeapMemoryUsage` (warning — heap > 80%)

**What it means:** The application is using more than 80% of the allocated heap. GC pressure is increasing.

**Immediate steps:**
1. Check Grafana → Spring Boot App → JVM Memory panel for the usage trend.
2. Check for memory leaks: `curl http://localhost:8080/actuator/metrics/jvm.memory.used`
3. Identify large allocations via heap dump:
   ```bash
   docker exec proje-pazari-app jcmd 1 GC.heap_info
   docker exec proje-pazari-app jmap -dump:format=b,file=/tmp/heap.hprof 1
   docker cp proje-pazari-app:/tmp/heap.hprof ./heap.hprof
   ```
4. Analyse with Eclipse MAT or JVisualVM.

**Resolution:**
- Increase heap: set `JAVA_OPTS=-Xmx1g` in the `app` service environment.
- Fix memory leak in application code.
- Scale horizontally if load is the cause.

---

#### `CriticalHeapMemoryUsage` (critical — heap > 95%)

**What it means:** `OutOfMemoryError` is imminent. The application will likely crash within minutes.

**Immediate steps:**
1. Restart the application immediately to restore service:
   ```bash
   docker-compose restart app
   ```
2. Capture a heap dump **before** restarting if possible (see above).
3. Increase heap size as a temporary fix.
4. Investigate root cause after service is restored.

---

### CPU Alerts

#### `HighProcessCpuUsage` (warning — process CPU > 70% for 3 min)

**What it means:** The JVM is consuming sustained high CPU. May be caused by heavy GC, CPU-bound workloads, or a hot loop.

**Immediate steps:**
1. Check which threads are consuming CPU:
   ```bash
   docker exec proje-pazari-app jcmd 1 Thread.print
   ```
2. Check GC activity: look at `jvm_gc_pause_seconds` in Grafana.
3. Check request rate in the HTTP panels — a traffic spike may be the cause.

**Resolution:**
- Optimise the hot code path.
- Add caching to reduce repeated computation.
- Scale horizontally under sustained load.

---

### HTTP Error Alerts

#### `HighHttpErrorRate` (warning — 5xx > 5% for 2 min)

**What it means:** More than 5% of requests are returning server errors.

**Immediate steps:**
1. Check application logs:
   ```bash
   docker-compose logs --tail=100 app | grep -i "error\|exception"
   ```
2. Identify which endpoints are failing:
   ```promql
   sum by (uri, status) (rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
   ```
3. Check downstream dependencies (DB, Redis, MinIO, Elasticsearch).

**Resolution:**
- Fix the code bug causing exceptions.
- If a dependency is down, apply circuit-breaking or fallback logic.

---

#### `CriticalHttpErrorRate` (critical — 5xx > 20% for 1 min)

**What it means:** The application is largely broken for users.

**Immediate steps:**
1. Check if the app container is healthy:
   ```bash
   docker-compose ps
   curl http://localhost:8080/actuator/health
   ```
2. Restart if the app is in a bad state:
   ```bash
   docker-compose restart app
   ```
3. Roll back the last deployment if this started after a release.

---

### Database Alerts

#### `DatabaseConnectionPoolExhaustion` (critical — pool full + pending waiters)

**What it means:** All HikariCP connections are in use and threads are queuing. New database operations will fail or time out.

**Immediate steps:**
1. Check the current pool state:
   ```bash
   curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
   curl http://localhost:8080/actuator/metrics/hikaricp.connections.pending
   ```
2. Look for long-running transactions:
   ```bash
   docker exec proje-pazari-db psql -U yazilim -d proje_pazari_db \
     -c "SELECT pid, now() - pg_stat_activity.query_start AS duration, query
         FROM pg_stat_activity
         WHERE (now() - pg_stat_activity.query_start) > interval '5 minutes';"
   ```
3. Kill blocking queries if necessary:
   ```bash
   docker exec proje-pazari-db psql -U yazilim -d proje_pazari_db \
     -c "SELECT pg_terminate_backend(<pid>);"
   ```

**Resolution:**
- Increase `spring.datasource.hikari.maximum-pool-size` (default: 10).
- Audit transaction scope — avoid holding connections longer than necessary.
- Add connection pool monitoring to detect trends early.

---

#### `DatabaseConnectionAcquireTimeout` (warning)

**What it means:** At least one thread failed to obtain a connection within `connection-timeout` (30 s).

**Immediate steps:**
1. Check `hikaricp_connections_pending` — if > 0, the pool is under pressure.
2. Review slow queries: check `hikaricp_connections_usage_seconds` p95/p99.
3. Check PostgreSQL `max_connections`:
   ```bash
   docker exec proje-pazari-db psql -U yazilim -d proje_pazari_db \
     -c "SHOW max_connections;"
   ```

---

### Response Time Alerts

#### `SlowApiResponseTime` (warning — p95 > 2 s for 5 min)

**What it means:** 95% of requests complete within more than 2 seconds.

**Immediate steps:**
1. Identify the slowest endpoints:
   ```promql
   topk(10,
     histogram_quantile(0.95,
       sum by (le, uri, method) (rate(http_server_requests_seconds_bucket[5m]))
     )
   )
   ```
2. Check DB query performance in the Infrastructure dashboard.
3. Check Redis cache hit ratio — a low hit ratio causes extra DB load.
4. Check Elasticsearch latency if search is involved.

**Resolution:**
- Add missing database indexes.
- Increase Redis cache TTL or expand cached queries.
- Paginate or optimise slow queries.

---

#### `SlowApiEndpoint` (warning — specific endpoint p95 > 2 s)

**What it means:** A specific endpoint is slow, narrowing the investigation.

**Steps:**
1. Alert label `uri` and `method` tell you exactly which endpoint.
2. Add `@Timed` to the controller method for finer-grained tracing.
3. Enable SQL logging temporarily:
   ```properties
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   ```

---

### Service Availability Alerts

#### `SpringBootAppDown` (critical — `up == 0` for 1 min)

**What it means:** Prometheus cannot reach `/actuator/prometheus` on `app:8080`.

**Immediate steps:**
1. Check container status:
   ```bash
   docker-compose ps app
   docker-compose logs --tail=50 app
   ```
2. Attempt to restart:
   ```bash
   docker-compose restart app
   ```
3. If restart fails, check startup logs for configuration errors:
   ```bash
   docker-compose up app  # foreground for full output
   ```
4. Verify all dependencies are healthy:
   ```bash
   docker-compose ps
   curl http://localhost:5432  # postgres
   curl http://localhost:6379  # redis
   curl http://localhost:9200/_cluster/health  # elasticsearch
   ```

---

#### `PrometheusTargetMissing` (critical — any `up == 0` for 2 min)

**What it means:** A Prometheus scrape target has disappeared.

**Steps:**
1. Open http://localhost:9090/targets to see which target is down.
2. Restart the affected service:
   ```bash
   docker-compose restart <service>
   ```
3. Verify the container is reachable from the Prometheus container:
   ```bash
   docker exec proje-pazari-prometheus wget -qO- http://app:8080/actuator/health
   ```

---

### Authentication Alerts

#### `HighAuthenticationFailureRate` (warning — failure > 30% for 3 min)

**What it means:** An abnormally high proportion of login attempts are failing. May be a brute-force or credential-stuffing attack.

**Immediate steps:**
1. Check the current failure rate:
   ```promql
   rate(auth_login_total{status="failure"}[1m])
   ```
2. Review access logs for repeated IPs.
3. Consider enabling rate limiting on `/api/v1/auth/login` (see `RateLimitConfig`).

---

#### `AuthenticationFailureSurge` (critical — > 5 failures/s for 1 min)

**What it means:** High-volume attack in progress.

**Immediate steps:**
1. Identify attacking IPs from application logs:
   ```bash
   docker-compose logs app | grep "auth.login.failed" | awk '{print $NF}' | sort | uniq -c | sort -rn | head
   ```
2. Block at the reverse proxy / firewall level.
3. Temporarily reduce the JWT secret exposure window if tokens may be compromised.

---

### MinIO Alerts

#### `MinioUploadFailureRateHigh` (warning — upload failure > 5% for 3 min)

**What it means:** More than 5% of file upload attempts are failing.

**Immediate steps:**
1. Check MinIO container health:
   ```bash
   docker-compose ps minio
   curl http://localhost:9002/minio/health/live
   ```
2. Check free disk space:
   ```bash
   docker exec proje-pazari-minio df -h /data
   ```
3. Review MinIO logs:
   ```bash
   docker-compose logs --tail=50 minio
   ```

**Resolution:**
- Free disk space or expand the MinIO volume.
- Restart MinIO if it is in a degraded state.

---

#### `SlowMinioUpload` (warning — upload p95 > 10 s for 5 min)

**What it means:** File uploads are taking an unusually long time.

**Steps:**
1. Check network throughput between the app and MinIO containers.
2. Check MinIO disk I/O:
   ```bash
   docker stats proje-pazari-minio
   ```
3. Consider checking if large files are driving the p95 up — review `file.max-size` config.

---

## Troubleshooting Guide

### Prometheus cannot scrape the app

**Symptom:** http://localhost:9090/targets shows `app:8080` as DOWN.

```bash
# 1. Is the app running?
docker-compose ps app

# 2. Is the actuator endpoint reachable from inside the Docker network?
docker exec proje-pazari-prometheus wget -qO- http://app:8080/actuator/prometheus | head -20

# 3. Is the prometheus endpoint enabled?
curl http://localhost:8080/actuator/prometheus | head -5

# 4. Check application.properties
grep "management.endpoints" src/main/resources/application.properties
# Should contain: management.endpoints.web.exposure.include=health,info,prometheus,metrics
```

---

### Grafana shows "No data" on panels

**Symptom:** Panels display "No data" or "N/A".

```bash
# 1. Check Prometheus has data for the metric
# Open http://localhost:9090 and query:
#   user_registration_total
#   http_server_requests_seconds_count

# 2. Verify the application label matches the dashboard variable
curl -s http://localhost:8080/actuator/prometheus | grep 'application="'
# Should show: application="proje-pazari"

# 3. Check the datasource in Grafana
# Grafana → Connections → Data sources → Prometheus
# Test: should return "Data source is working"

# 4. Adjust the dashboard time range — metrics only exist from app start
```

---

### Grafana dashboards not auto-loading

**Symptom:** Grafana opens but the 3 dashboards are missing.

```bash
# 1. Verify provisioning files are mounted correctly
docker exec proje-pazari-grafana ls /etc/grafana/provisioning/dashboards/
# Expected: dashboard.yml  spring-boot-dashboard.json  ...

# 2. Check Grafana logs for provisioning errors
docker-compose logs grafana | grep -i "provision\|error"

# 3. Verify JSON is valid
node -e "JSON.parse(require('fs').readFileSync('docker/grafana/provisioning/dashboards/spring-boot-dashboard.json'))" && echo "OK"

# 4. Restart Grafana to force re-provisioning
docker-compose restart grafana
```

---

### Prometheus alert rules not loading

**Symptom:** http://localhost:9090/rules shows no rules.

```bash
# 1. Verify rules file is mounted
docker exec proje-pazari-prometheus ls /etc/prometheus/
# Expected: prometheus.yml  rules.yml

# 2. Validate rule syntax
docker exec proje-pazari-prometheus promtool check rules /etc/prometheus/rules.yml

# 3. Validate full config
docker exec proje-pazari-prometheus promtool check config /etc/prometheus/prometheus.yml

# 4. Reload Prometheus config (no restart needed)
curl -X POST http://localhost:9090/-/reload
```

---

### Grafana alert notifications not sending

**Symptom:** Alerts fire in Grafana but no email/Slack/Discord messages arrive.

```bash
# 1. Check SMTP is enabled
docker exec proje-pazari-grafana env | grep GF_SMTP

# 2. Test SMTP from inside the container
docker exec proje-pazari-grafana \
  curl -s smtp://smtp.gmail.com:587 --ssl-reqd \
       --mail-from "grafana@proje-pazari.com" \
       --mail-rcpt "admin@proje-pazari.com" \
       --user "your-email:your-password" \
       -T /dev/null

# 3. Check Grafana alerting logs
docker-compose logs grafana | grep -i "alert\|notif\|smtp\|slack"

# 4. Verify webhook URLs are set
docker exec proje-pazari-grafana env | grep GF_SLACK
docker exec proje-pazari-grafana env | grep GF_DISCORD

# 5. Send a test notification from Grafana UI
# Grafana → Alerting → Contact points → (point) → Test
```

---

### High memory alert firing unexpectedly

**Symptom:** `HighHeapMemoryUsage` fires but the app seems healthy.

```bash
# 1. Check actual heap usage
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used \
  | python -m json.tool

# 2. Check if max heap is set too low (default is 256m)
docker exec proje-pazari-app java -XX:+PrintFlagsFinal -version 2>&1 | grep -i maxheap

# 3. Increase heap in docker-compose app environment:
#    JAVA_OPTS: "-Xms256m -Xmx1g"

# 4. Force GC and recheck
curl -X POST http://localhost:8080/actuator/gc   # if gc endpoint is enabled
```

---

### Metrics not appearing after code changes

**Symptom:** A new counter added to `BusinessMetricsService` doesn't appear at `/actuator/prometheus`.

```bash
# 1. Rebuild and restart the app
./gradlew build -x test
docker-compose build app
docker-compose up -d app

# 2. Verify the metric is registered
curl http://localhost:8080/actuator/prometheus | grep "my_new_metric"

# 3. Check that the bean is being injected (no @Lazy, no circular dependency)
docker-compose logs app | grep "ERROR\|WARN" | head -20
```

---

*Last updated: 2026-02-28 — IYTE Yazılım Topluluğu*
