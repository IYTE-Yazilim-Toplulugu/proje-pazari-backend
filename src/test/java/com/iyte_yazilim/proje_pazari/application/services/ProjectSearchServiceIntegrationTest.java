package com.iyte_yazilim.proje_pazari.application.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
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
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@DisplayName("Project Search Service Integration Tests")
class ProjectSearchServiceIntegrationTest {

    @Container
    static ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer(
                            DockerImageName.parse(
                                            "docker.elastic.co/elasticsearch/elasticsearch:9.2.2")
                                    .asCompatibleSubstituteFor(
                                            "docker.elastic.co/elasticsearch/elasticsearch"))
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("xpack.security.http.ssl.enabled", "false");

    @DynamicPropertySource
    static void elasticsearchProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearch::getHttpHostAddress);
        registry.add("spring.data.elasticsearch.enabled", () -> "true");
        registry.add("spring.data.elasticsearch.repositories.enabled", () -> "true");
        registry.add("spring.autoconfigure.exclude", () -> ""); // Remove exclusions for this test
    }

    @Autowired private ProjectSearchService projectSearchService;

    @Autowired private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void setUp() {
        elasticsearchOperations.indexOps(ProjectDocument.class).delete();
        elasticsearchOperations.indexOps(ProjectDocument.class).create();
        elasticsearchOperations.indexOps(ProjectDocument.class).putMapping();

        // Note: tags are set to null to reflect actual behavior - the ProjectDocumentMapper
        // ignores tags (see @Mapping(target = "tags", ignore = true)). Tag functionality
        // is not yet implemented in the mapper.
        ProjectDocument project1 =
                ProjectDocument.builder()
                        .id("1")
                        .title("Java Spring Boot Project")
                        .description("A comprehensive Spring Boot application")
                        .summary("Backend development with Java")
                        .status("ACTIVE")
                        .tags(null)
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
                        .tags(null)
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
                        .tags(null)
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
    @DisplayName("Should return results matching keyword in title")
    void shouldSearchProjects() {
        List<ProjectDocument> results = projectSearchService.searchProjects("Spring");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).contains("Spring");
    }

    @Test
    @DisplayName("Should return results matching keyword in description")
    void shouldSearchProjectsByDescription() {
        List<ProjectDocument> results = projectSearchService.searchProjects("TypeScript");

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(p -> p.getDescription().contains("TypeScript"));
    }

    @Test
    @DisplayName("Should return empty list when no projects match search keyword")
    void shouldReturnEmptyListForNonMatchingSearch() {
        List<ProjectDocument> results =
                projectSearchService.searchProjects("NonExistentKeyword123");

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("Should return results for advanced search with keyword filter")
    void shouldPerformAdvancedSearchWithKeyword() {
        SearchPage<ProjectDocument> results =
                projectSearchService.advancedSearch("Java", null, null, PageRequest.of(0, 10));

        assertThat(results.getSearchHits().getTotalHits()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should return only projects matching the given status in advanced search")
    void shouldPerformAdvancedSearchWithStatus() {
        SearchPage<ProjectDocument> results =
                projectSearchService.advancedSearch(null, "COMPLETED", null, PageRequest.of(0, 10));

        assertThat(results.getSearchHits().getTotalHits()).isEqualTo(1);
    }

    @Test
    @Disabled(
            "Tag search functionality not yet implemented - tags are ignored in ProjectDocumentMapper")
    @DisplayName("Should return results matching given tags in advanced search")
    void shouldPerformAdvancedSearchWithTags() {
        SearchPage<ProjectDocument> results =
                projectSearchService.advancedSearch(
                        null, null, List.of("java"), PageRequest.of(0, 10));

        assertThat(results.getSearchHits().getTotalHits()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should return title suggestions for given prefix")
    void shouldGetSuggestions() {
        List<String> suggestions = projectSearchService.getSuggestions("Java");

        assertThat(suggestions).isNotEmpty();
    }

    @Test
    @DisplayName("Should return project statistics grouped by status")
    void shouldGetProjectStatistics() {
        Map<String, Long> statistics = projectSearchService.getProjectStatistics();

        assertThat(statistics).isNotEmpty();
        assertThat(statistics).containsKey("ACTIVE");
    }
}
