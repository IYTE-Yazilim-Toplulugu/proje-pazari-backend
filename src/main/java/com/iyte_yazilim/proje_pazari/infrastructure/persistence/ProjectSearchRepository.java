package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProjectSearchRepository extends ElasticsearchRepository<ProjectDocument, String> {
    // Search by title or description
    List<ProjectDocument> findByTitleContainingOrDescriptionContaining(
            String title, String description
    );

    // Search by status
    List<ProjectDocument> findByStatus(String status);

    // Search by owner
    List<ProjectDocument> findByOwnerId(String ownerId);
}
