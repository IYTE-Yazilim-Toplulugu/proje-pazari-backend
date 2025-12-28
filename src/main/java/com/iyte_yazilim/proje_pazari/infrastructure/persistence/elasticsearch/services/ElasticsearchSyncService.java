package com.iyte_yazilim.proje_pazari.infrastructure.persistence.elasticsearch.services;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectSearchRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserDocument;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Getter
@Setter
@AllArgsConstructor
public class ElasticsearchSyncService {

    private final ProjectRepository projectRepository;
    private final ProjectSearchRepository projectSearchRepository;
    private final ProjectDocumentMapper mapper;// TODO: Create mapper.
    private ElasticsearchOperations elasticsearchOperations;

    @PostConstruct
    public void initializeIndexes() {
        // Create indexes if they don't exist
        IndexOperations projectIndexOps = elasticsearchOperations.indexOps(ProjectDocument.class);
        if (!projectIndexOps.exists()) {
            projectIndexOps.createWithMapping();
        }

        IndexOperations userIndexOps = elasticsearchOperations.indexOps(UserDocument.class);
        if (!userIndexOps.exists()) {
            userIndexOps.createWithMapping();
        }
    }

    public void indexProject(String projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));// TODO: Create exception.

        ProjectDocument document = mapper.toDocument(project);
        projectSearchRepository.save(document);
    }

    public void deleteProjectIndex(String projectId) {
        projectSearchRepository.deleteById(projectId);
    }

    @Transactional
    public void reindexAllProjects() {
        projectSearchRepository.deleteAll();

        int batchSize = 100;
        Page<ProjectEntity> page;
        int pageNumber = 0;

        do {
            page = projectRepository.findAll(PageRequest.of(pageNumber, batchSize));

            List<ProjectDocument> documents = page.getContent().stream()
                    .map(mapper::toDocument)
                    .collect(Collectors.toList());

            projectSearchRepository.saveAll(documents);
            pageNumber++;

        } while (page.hasNext());
    }
}
