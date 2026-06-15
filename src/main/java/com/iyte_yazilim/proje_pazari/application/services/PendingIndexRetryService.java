package com.iyte_yazilim.proje_pazari.application.services;

import com.iyte_yazilim.proje_pazari.domain.exceptions.ProjectNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.PendingIndexRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PendingIndexEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PendingIndexStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PendingIndexRetryService {

    /** Max transient retries before an entry is marked permanently {@code FAILED}. */
    static final int MAX_ATTEMPTS = 10;

    private final PendingIndexRepository pendingIndexRepository;
    private final ElasticsearchSyncService syncService;

    @Scheduled(fixedDelay = 60_000)
    public void retryPendingIndexes() {
        List<PendingIndexEntity> pending =
                pendingIndexRepository.findByStatus(PendingIndexStatus.PENDING);
        if (pending.isEmpty()) {
            return;
        }

        log.info("Retrying {} pending project index entries.", pending.size());

        // No method-level transaction: each save() below commits on its own, so a DB connection is
        // not pinned across the (potentially slow) Elasticsearch calls while ES is unavailable.
        for (PendingIndexEntity entry : pending) {
            try {
                syncService.indexProject(entry.getProjectId());
                entry.setStatus(PendingIndexStatus.DONE);
                log.info(
                        "Successfully reindexed previously failed project: {}",
                        entry.getProjectId());
            } catch (ProjectNotFoundException e) {
                // The project no longer exists (created during an outage, then deleted). This will
                // never succeed, so mark it terminal and best-effort drop any stale ES document.
                entry.setStatus(PendingIndexStatus.FAILED);
                log.warn(
                        "Project {} no longer exists; marking index retry entry FAILED.",
                        entry.getProjectId());
                try {
                    syncService.deleteProjectIndex(entry.getProjectId());
                } catch (Exception cleanupEx) {
                    log.warn(
                            "Failed to remove stale index document for missing project {}: {}",
                            entry.getProjectId(),
                            cleanupEx.getMessage());
                }
            } catch (Exception e) {
                entry.setAttemptCount(entry.getAttemptCount() + 1);
                if (entry.getAttemptCount() >= MAX_ATTEMPTS) {
                    entry.setStatus(PendingIndexStatus.FAILED);
                    log.error(
                            "Project {} index retry permanently FAILED after {} attempts: {}",
                            entry.getProjectId(),
                            entry.getAttemptCount(),
                            e.getMessage());
                } else {
                    log.warn(
                            "Retry #{} for project {} still failing: {}",
                            entry.getAttemptCount(),
                            entry.getProjectId(),
                            e.getMessage());
                }
            }
            pendingIndexRepository.save(entry);
        }
    }
}
