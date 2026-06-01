package com.iyte_yazilim.proje_pazari.application.services;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.PendingIndexRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PendingIndexEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PendingIndexRetryService {

    private final PendingIndexRepository pendingIndexRepository;
    private final ElasticsearchSyncService syncService;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void retryPendingIndexes() {
        List<PendingIndexEntity> pending = pendingIndexRepository.findByStatus("PENDING");
        if (pending.isEmpty()) {
            return;
        }

        log.info("Retrying {} pending project index entries.", pending.size());

        for (PendingIndexEntity entry : pending) {
            try {
                syncService.indexProject(entry.getProjectId());
                entry.setStatus("DONE");
                log.info(
                        "Successfully reindexed previously failed project: {}",
                        entry.getProjectId());
            } catch (Exception e) {
                entry.setAttemptCount(entry.getAttemptCount() + 1);
                log.warn(
                        "Retry #{} for project {} still failing: {}",
                        entry.getAttemptCount(),
                        entry.getProjectId(),
                        e.getMessage());
            }
            pendingIndexRepository.save(entry);
        }
    }
}
