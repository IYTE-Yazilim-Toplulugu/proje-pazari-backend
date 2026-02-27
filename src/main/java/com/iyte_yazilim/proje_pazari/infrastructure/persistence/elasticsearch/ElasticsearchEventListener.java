package com.iyte_yazilim.proje_pazari.infrastructure.persistence.elasticsearch;

import com.iyte_yazilim.proje_pazari.application.services.ElasticsearchSyncService;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectCreatedEvent;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectDeletedEvent;
import com.iyte_yazilim.proje_pazari.domain.events.ProjectUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ElasticsearchEventListener {

    private final ElasticsearchSyncService syncService;

    @EventListener
    @Async
    public void handleProjectCreated(ProjectCreatedEvent event) {
        try {
            log.debug("Indexing newly created project: {}", event.projectId());
            syncService.indexProject(event.projectId());
        } catch (Exception e) {
            log.error(
                    "Failed to index newly created project {}: {}",
                    event.projectId(),
                    e.getMessage(),
                    e);
        }
    }

    @EventListener
    @Async
    public void handleProjectUpdated(ProjectUpdatedEvent event) {
        try {
            log.debug("Re-indexing updated project: {}", event.projectId());
            syncService.indexProject(event.projectId());
        } catch (Exception e) {
            log.error(
                    "Failed to re-index updated project {}: {}",
                    event.projectId(),
                    e.getMessage(),
                    e);
        }
    }

    @EventListener
    @Async
    public void handleProjectDeleted(ProjectDeletedEvent event) {
        try {
            log.debug("Removing deleted project from index: {}", event.projectId());
            syncService.deleteProjectIndex(event.projectId());
        } catch (Exception e) {
            log.error(
                    "Failed to remove project {} from index: {}",
                    event.projectId(),
                    e.getMessage(),
                    e);
        }
    }
}
