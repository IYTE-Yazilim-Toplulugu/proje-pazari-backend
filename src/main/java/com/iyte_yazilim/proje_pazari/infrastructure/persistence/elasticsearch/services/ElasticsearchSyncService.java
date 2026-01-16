package com.iyte_yazilim.proje_pazari.infrastructure.persistence.elasticsearch.services;

import com.iyte_yazilim.proje_pazari.domain.exceptions.ProjectNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectSearchRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserSearchRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectDocumentMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserDocument;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.utils.NameUtils;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@ConditionalOnProperty(
        name = "spring.data.elasticsearch.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ElasticsearchSyncService {

    private final ProjectRepository projectRepository;
    private final ProjectSearchRepository projectSearchRepository;
    private final UserRepository userRepository;
    private final UserSearchRepository userSearchRepository;
    private final ProjectDocumentMapper mapper;
    private final ElasticsearchOperations elasticsearchOperations;

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
        ProjectEntity project =
                projectRepository
                        .findById(projectId)
                        .orElseThrow(() -> new ProjectNotFoundException(projectId));

        ProjectDocument document = mapper.toDocument(project);
        projectSearchRepository.save(document);
    }

    public void deleteProjectIndex(String projectId) {
        projectSearchRepository.deleteById(projectId);
    }

    @Transactional(readOnly = true)
    @Async
    public void reindexAllProjects() {
        projectSearchRepository.deleteAll();

        int batchSize = 100;
        Page<ProjectEntity> page;
        int pageNumber = 0;

        do {
            page = projectRepository.findAll(PageRequest.of(pageNumber, batchSize));

            List<ProjectDocument> documents =
                    page.getContent().stream().map(mapper::toDocument).collect(Collectors.toList());

            projectSearchRepository.saveAll(documents);
            pageNumber++;

        } while (page.hasNext());
    }

    @Transactional(readOnly = true)
    @Async
    public void reindexAllUsers() {
        userSearchRepository.deleteAll();

        int batchSize = 100;
        Page<UserEntity> page;
        int pageNumber = 0;

        do {
            page = userRepository.findAll(PageRequest.of(pageNumber, batchSize));

            List<UserDocument> documents =
                    page.getContent().stream()
                            .map(this::toUserDocument)
                            .collect(Collectors.toList());

            userSearchRepository.saveAll(documents);
            pageNumber++;

        } while (page.hasNext());
    }

    @Async
    public void deleteAllIndexes() {
        IndexOperations projectIndexOps = elasticsearchOperations.indexOps(ProjectDocument.class);
        if (projectIndexOps.exists()) {
            projectIndexOps.delete();
        }

        IndexOperations userIndexOps = elasticsearchOperations.indexOps(UserDocument.class);
        if (userIndexOps.exists()) {
            userIndexOps.delete();
        }
    }

    private UserDocument toUserDocument(UserEntity entity) {
        UserDocument document = new UserDocument();
        document.setId(entity.getId());
        document.setFirstName(entity.getFirstName());
        document.setLastName(entity.getLastName());
        document.setFullName(NameUtils.buildFullName(entity.getFirstName(), entity.getLastName()));
        document.setEmail(entity.getEmail());
        document.setDescription(entity.getDescription());
        document.setJoinedAt(entity.getCreatedAt());
        return document;
    }
}
