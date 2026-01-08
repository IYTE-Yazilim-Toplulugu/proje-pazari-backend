package com.iyte_yazilim.proje_pazari.infrastructure.persistence.elasticsearch.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class ProjectSearchServiceTest {

    @Container
    static ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.11.0")
                    .withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
    }

    @Autowired private ProjectSearchService projectSearchService;

    @Autowired private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void setUp() {
        elasticsearchOperations.indexOps(ProjectDocument.class).delete();
        elasticsearchOperations.indexOps(ProjectDocument.class).create();

        ProjectDocument project1 =
                ProjectDocument.builder()
                        .id("1")
                        .title("Java Spring Boot Project")
                        .description("A comprehensive Spring Boot application")
                        .summary("Backend development with Java")
                        .status("ACTIVE")
                        .tags(List.of("java", "spring", "backend"))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .applicationsCount(5)
                        .build();

        ProjectDocument project2 =
                ProjectDocument.builder()
                        .id("2")
                        .title("React Frontend Application")
                        .description("Modern React application with TypeScript")
                        .summary("Frontend development with React")
                        .status("ACTIVE")
                        .tags(List.of("react", "typescript", "frontend"))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .applicationsCount(3)
                        .build();

        ProjectDocument project3 =
                ProjectDocument.builder()
                        .id("3")
                        .title("Python Machine Learning")
                        .description("ML project using Python and TensorFlow")
                        .summary("Data science and machine learning")
                        .status("COMPLETED")
                        .tags(List.of("python", "ml", "tensorflow"))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .applicationsCount(10)
                        .build();

        elasticsearchOperations.save(project1);
        elasticsearchOperations.save(project2);
        elasticsearchOperations.save(project3);

        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();
    }

    @Test
    void shouldSearchProjects() {
        List<ProjectDocument> results = projectSearchService.searchProjects("Spring");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).contains("Spring");
    }

    @Test
    void shouldSearchProjectsByDescription() {
        List<ProjectDocument> results = projectSearchService.searchProjects("TypeScript");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getDescription()).contains("TypeScript");
    }

    @Test
    void shouldReturnEmptyListForNonMatchingSearch() {
        List<ProjectDocument> results =
                projectSearchService.searchProjects("NonExistentKeyword123");

        assertThat(results).isEmpty();
    }

    @Test
    void shouldPerformAdvancedSearchWithKeyword() {
        SearchPage<ProjectDocument> results =
                projectSearchService.advancedSearch("Java", null, null, PageRequest.of(0, 10));

        assertThat(results.getSearchHits().getTotalHits()).isGreaterThan(0);
    }

    @Test
    void shouldPerformAdvancedSearchWithStatus() {
        SearchPage<ProjectDocument> results =
                projectSearchService.advancedSearch(null, "COMPLETED", null, PageRequest.of(0, 10));

        assertThat(results.getSearchHits().getTotalHits()).isEqualTo(1);
    }

    @Test
    void shouldPerformAdvancedSearchWithTags() {
        SearchPage<ProjectDocument> results =
                projectSearchService.advancedSearch(
                        null, null, List.of("java"), PageRequest.of(0, 10));

        assertThat(results.getSearchHits().getTotalHits()).isGreaterThan(0);
    }

    @Test
    void shouldGetSuggestions() {
        List<String> suggestions = projectSearchService.getSuggestions("Java");

        assertThat(suggestions).isNotEmpty();
    }

    @Test
    void shouldGetProjectStatistics() {
        Map<String, Long> statistics = projectSearchService.getProjectStatistics();

        assertThat(statistics).isNotEmpty();
        assertThat(statistics).containsKey("ACTIVE");
    }
}
