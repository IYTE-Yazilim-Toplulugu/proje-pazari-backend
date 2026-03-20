package com.iyte_yazilim.proje_pazari.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

/**
 * Centralized service for custom business metrics. Exposes counters and timers for key business
 * operations: user registration, project creation, application submissions, authentication events,
 * and MinIO storage operations. Database connection pool metrics are provided automatically by
 * HikariCP via Micrometer.
 */
@Service
public class BusinessMetricsService {

    // ── User registration ────────────────────────────────────────────────────
    private final Counter userRegistrationSuccess;
    private final Counter userRegistrationFailure;

    // ── Project creation ─────────────────────────────────────────────────────
    private final Counter projectCreationSuccess;
    private final Counter projectCreationFailure;

    // ── Application submission ───────────────────────────────────────────────
    private final Counter applicationSubmissionSuccess;
    private final Counter applicationSubmissionFailure;

    // ── Authentication ───────────────────────────────────────────────────────
    private final Counter authLoginSuccess;
    private final Counter authLoginFailure;

    // ── Elasticsearch sync ───────────────────────────────────────
    private final Counter esIndexSuccess;
    private final Counter esIndexFailure;

    // ── MinIO storage ────────────────────────────────────────────────────────
    private final Counter minioUploadSuccess;
    private final Counter minioUploadFailure;
    private final Timer minioUploadDuration;
    private final Counter minioDownloadSuccess;
    private final Counter minioDownloadFailure;
    private final Counter minioDeleteSuccess;
    private final Counter minioDeleteFailure;

    public BusinessMetricsService(MeterRegistry registry) {

        userRegistrationSuccess =
                Counter.builder("user.registration.total")
                        .description("Total user registration attempts")
                        .tag("status", "success")
                        .tag("layer", "business")
                        .register(registry);

        userRegistrationFailure =
                Counter.builder("user.registration.total")
                        .description("Total user registration attempts")
                        .tag("status", "failure")
                        .tag("layer", "business")
                        .register(registry);

        projectCreationSuccess =
                Counter.builder("project.creation.total")
                        .description("Total project creation attempts")
                        .tag("status", "success")
                        .tag("layer", "business")
                        .register(registry);

        projectCreationFailure =
                Counter.builder("project.creation.total")
                        .description("Total project creation attempts")
                        .tag("status", "failure")
                        .tag("layer", "business")
                        .register(registry);

        applicationSubmissionSuccess =
                Counter.builder("application.submission.total")
                        .description("Total application submission attempts")
                        .tag("status", "success")
                        .tag("layer", "business")
                        .register(registry);

        applicationSubmissionFailure =
                Counter.builder("application.submission.total")
                        .description("Total application submission attempts")
                        .tag("status", "failure")
                        .tag("layer", "business")
                        .register(registry);

        authLoginSuccess =
                Counter.builder("auth.login.total")
                        .description("Total authentication attempts")
                        .tag("status", "success")
                        .tag("layer", "security")
                        .register(registry);

        authLoginFailure =
                Counter.builder("auth.login.total")
                        .description("Total authentication attempts")
                        .tag("status", "failure")
                        .tag("layer", "security")
                        .register(registry);

        esIndexSuccess =
                Counter.builder("elasticsearch.index.total")
                        .description("Total Elasticsearch sync operations")
                        .tag("status", "success")
                        .tag("layer", "infrastructure")
                        .register(registry);

        esIndexFailure =
                Counter.builder("elasticsearch.index.total")
                        .description("Total Elasticsearch sync operations")
                        .tag("status", "failure")
                        .tag("layer", "infrastructure")
                        .register(registry);

        minioUploadSuccess =
                Counter.builder("minio.upload.total")
                        .description("Total MinIO upload operations")
                        .tag("status", "success")
                        .tag("layer", "storage")
                        .register(registry);

        minioUploadFailure =
                Counter.builder("minio.upload.total")
                        .description("Total MinIO upload operations")
                        .tag("status", "failure")
                        .tag("layer", "storage")
                        .register(registry);

        minioUploadDuration =
                Timer.builder("minio.upload.duration")
                        .description("MinIO upload operation duration")
                        .tag("layer", "storage")
                        .register(registry);

        minioDownloadSuccess =
                Counter.builder("minio.download.total")
                        .description("Total MinIO presigned URL (download) generations")
                        .tag("status", "success")
                        .tag("layer", "storage")
                        .register(registry);

        minioDownloadFailure =
                Counter.builder("minio.download.total")
                        .description("Total MinIO presigned URL (download) generations")
                        .tag("status", "failure")
                        .tag("layer", "storage")
                        .register(registry);

        minioDeleteSuccess =
                Counter.builder("minio.delete.total")
                        .description("Total MinIO delete operations")
                        .tag("status", "success")
                        .tag("layer", "storage")
                        .register(registry);

        minioDeleteFailure =
                Counter.builder("minio.delete.total")
                        .description("Total MinIO delete operations")
                        .tag("status", "failure")
                        .tag("layer", "storage")
                        .register(registry);
    }

    // ── User registration ────────────────────────────────────────────────────

    public void incrementUserRegistrationSuccess() {
        userRegistrationSuccess.increment();
    }

    public void incrementUserRegistrationFailure() {
        userRegistrationFailure.increment();
    }

    // ── Project creation ─────────────────────────────────────────────────────

    public void incrementProjectCreationSuccess() {
        projectCreationSuccess.increment();
    }

    public void incrementProjectCreationFailure() {
        projectCreationFailure.increment();
    }

    // ── Application submission ───────────────────────────────────────────────

    public void incrementApplicationSubmissionSuccess() {
        applicationSubmissionSuccess.increment();
    }

    public void incrementApplicationSubmissionFailure() {
        applicationSubmissionFailure.increment();
    }

    // ── Authentication ───────────────────────────────────────────────────────

    public void incrementAuthLoginSuccess() {
        authLoginSuccess.increment();
    }

    public void incrementAuthLoginFailure() {
        authLoginFailure.increment();
    }

    // ── MinIO storage ────────────────────────────────────────────────────────

    public void incrementMinioUploadSuccess() {
        minioUploadSuccess.increment();
    }

    public void incrementMinioUploadFailure() {
        minioUploadFailure.increment();
    }

    public Timer getMinioUploadTimer() {
        return minioUploadDuration;
    }

    public void incrementMinioDownloadSuccess() {
        minioDownloadSuccess.increment();
    }

    public void incrementMinioDownloadFailure() {
        minioDownloadFailure.increment();
    }

    public void incrementMinioDeleteSuccess() {
        minioDeleteSuccess.increment();
    }

    public void incrementMinioDeleteFailure() {
        minioDeleteFailure.increment();
    }

    // ── Elasticsearch sync ───────────────────────────────────────

    public void incrementEsIndexSuccess() {
        esIndexSuccess.increment();
    }

    public void incrementEsIndexFailure() {
        esIndexFailure.increment();
    }
}
