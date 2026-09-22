package com.iyte_yazilim.proje_pazari.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch.indices.PutIndicesSettingsRequest;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserDocument;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ElasticsearchIndexConfiguration {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;

    @Value("${app.elasticsearch.number-of-replicas:0}")
    private int numberOfReplicas;

    @Value("${app.elasticsearch.project-index:projects}")
    private String projectIndexName;

    @Value("${app.elasticsearch.user-index:users}")
    private String userIndexName;

    @PostConstruct
    public void configureIndexSettings() {
        ensureIndexExists(ProjectDocument.class, projectIndexName);
        ensureIndexExists(UserDocument.class, userIndexName);

        updateIndexReplicas(projectIndexName);
        updateIndexReplicas(userIndexName);
    }

    private void ensureIndexExists(Class<?> documentClass, String indexName) {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(documentClass);
            if (!indexOps.exists()) {
                log.info("Creating index: {}", indexName);
                indexOps.createWithMapping();
            }
        } catch (Exception e) {
            log.warn("Could not ensure index {} exists: {}", indexName, e.getMessage());
        }
    }

    private void updateIndexReplicas(String indexName) {
        try {
            IndexSettings settings =
                    IndexSettings.of(
                            builder -> builder.numberOfReplicas(String.valueOf(numberOfReplicas)));

            PutIndicesSettingsRequest request =
                    PutIndicesSettingsRequest.of(
                            builder -> builder.index(indexName).settings(settings));

            elasticsearchClient.indices().putSettings(request);

            log.info("Updated {} index replica count to {}", indexName, numberOfReplicas);
        } catch (Exception e) {
            log.warn(
                    "Could not update {} index replicas (may need manual configuration): {}",
                    indexName,
                    e.getMessage());
        }
    }
}
