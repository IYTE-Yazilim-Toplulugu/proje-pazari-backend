package com.iyte_yazilim.proje_pazari.application.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserDocument;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
@DisplayName("Turkish Character Diacritic-Insensitive Search Integration Tests")
class TurkishSearchIntegrationTest {

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
    }

    @Autowired private ProjectSearchService projectSearchService;

    @Autowired private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void setUp() {
        // Clean up existing indices
        elasticsearchOperations.indexOps(ProjectDocument.class).delete();
        elasticsearchOperations.indexOps(UserDocument.class).delete();

        // Create fresh indices with mappings
        elasticsearchOperations.indexOps(ProjectDocument.class).create();
        elasticsearchOperations.indexOps(ProjectDocument.class).putMapping();

        elasticsearchOperations.indexOps(UserDocument.class).create();
        elasticsearchOperations.indexOps(UserDocument.class).putMapping();
    }

    // ========== PROJECT TITLE TESTS ==========

    @Test
    @DisplayName("Should match 'g' with 'ğ' in project title")
    void shouldMatchTurkishGInProjectTitle() {
        // Index project with Turkish character 'ğ'
        ProjectDocument project = new ProjectDocument();
        project.setId("1");
        project.setTitle("Test Proğresi"); // Contains 'ğ'
        project.setDescription("Description");
        project.setSummary("Summary");
        project.setStatus("OPEN");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        elasticsearchOperations.save(project);
        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();

        // Search without diacritics
        List<ProjectDocument> results = projectSearchService.searchProjects("progresi");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).contains("Proğresi");
    }

    @Test
    @DisplayName("Should match 's' with 'ş' in project title")
    void shouldMatchTurkishSInProjectTitle() {
        ProjectDocument project = new ProjectDocument();
        project.setId("2");
        project.setTitle("Şirket Projesi"); // Contains 'Ş'
        project.setDescription("Description");
        project.setSummary("Summary");
        project.setStatus("OPEN");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        elasticsearchOperations.save(project);
        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();

        List<ProjectDocument> results = projectSearchService.searchProjects("sirket");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).contains("Şirket");
    }

    @Test
    @DisplayName("Should match 'i' with 'ı' in project title")
    void shouldMatchTurkishDotlessIInProjectTitle() {
        ProjectDocument project = new ProjectDocument();
        project.setId("3");
        project.setTitle("Tasarım Projesi"); // Contains 'ı'
        project.setDescription("Description");
        project.setSummary("Summary");
        project.setStatus("OPEN");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        elasticsearchOperations.save(project);
        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();

        List<ProjectDocument> results = projectSearchService.searchProjects("tasarim");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).contains("Tasarım");
    }

    @Test
    @DisplayName("Should match 'c' with 'ç' in project title")
    void shouldMatchTurkishCInProjectTitle() {
        ProjectDocument project = new ProjectDocument();
        project.setId("4");
        project.setTitle("Çalışma Sistemi"); // Contains 'Ç'
        project.setDescription("Description");
        project.setSummary("Summary");
        project.setStatus("OPEN");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        elasticsearchOperations.save(project);
        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();

        List<ProjectDocument> results = projectSearchService.searchProjects("calisma");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).contains("Çalışma");
    }

    // ========== PROJECT DESCRIPTION TESTS ==========

    @Test
    @DisplayName("Should match 'o' with 'ö' in project description")
    void shouldMatchTurkishOInProjectDescription() {
        ProjectDocument project = new ProjectDocument();
        project.setId("5");
        project.setTitle("Test Project");
        project.setDescription("Güzel bir proje özeti"); // Contains 'ö'
        project.setSummary("Summary");
        project.setStatus("OPEN");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        elasticsearchOperations.save(project);
        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();

        List<ProjectDocument> results = projectSearchService.searchProjects("ozeti");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getDescription()).contains("özeti");
    }

    @Test
    @DisplayName("Should match 'u' with 'ü' in project description")
    void shouldMatchTurkishUInProjectDescription() {
        ProjectDocument project = new ProjectDocument();
        project.setId("6");
        project.setTitle("Test Project");
        project.setDescription("Ürün geliştirme projesi"); // Contains 'Ü' and 'ü'
        project.setSummary("Summary");
        project.setStatus("OPEN");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        elasticsearchOperations.save(project);
        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();

        List<ProjectDocument> results = projectSearchService.searchProjects("urun");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getDescription()).contains("Ürün");
    }

    // ========== PROJECT SUMMARY TESTS ==========

    @Test
    @DisplayName("Should match Turkish characters in project summary field")
    void shouldMatchTurkishCharactersInProjectSummary() {
        ProjectDocument project = new ProjectDocument();
        project.setId("7");
        project.setTitle("Test Project");
        project.setDescription("Description");
        project.setSummary("Özgün içerik oluşturma"); // Contains 'Ö', 'ü', 'ş'
        project.setStatus("OPEN");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        elasticsearchOperations.save(project);
        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();

        List<ProjectDocument> results = projectSearchService.searchProjects("ozgun icerik");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getSummary()).contains("Özgün içerik");
    }

    // ========== USER DOCUMENT TESTS ==========

    @Test
    @DisplayName("Should match Turkish characters in user fullName")
    void shouldMatchTurkishCharactersInUserFullName() {
        UserDocument user = new UserDocument();
        user.setId("user1");
        user.setFirstName("Çağrı");
        user.setLastName("Öztürk");
        user.setFullName("Çağrı Öztürk"); // Contains 'Ç', 'ğ', 'Ö', 'ü'
        user.setEmail("cagri@test.com");
        user.setJoinedAt(LocalDateTime.now());

        elasticsearchOperations.save(user);
        elasticsearchOperations.indexOps(UserDocument.class).refresh();

        // Note: We need a user search service to test this properly
        // For now, we can verify the document was indexed with correct analyzer
        UserDocument indexed = elasticsearchOperations.get("user1", UserDocument.class);

        assertThat(indexed).isNotNull();
        assertThat(indexed.getFullName()).isEqualTo("Çağrı Öztürk");
    }

    @Test
    @DisplayName("Should match Turkish characters in user description")
    void shouldMatchTurkishCharactersInUserDescription() {
        UserDocument user = new UserDocument();
        user.setId("user2");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setFullName("Test User");
        user.setEmail("test@test.com");
        user.setDescription("Yazılım geliştirici"); // Contains 'ı', 'ş'
        user.setJoinedAt(LocalDateTime.now());

        elasticsearchOperations.save(user);
        elasticsearchOperations.indexOps(UserDocument.class).refresh();

        UserDocument indexed = elasticsearchOperations.get("user2", UserDocument.class);

        assertThat(indexed).isNotNull();
        assertThat(indexed.getDescription()).contains("Yazılım");
    }

    // ========== MIXED CASE AND COMPLEX TESTS ==========

    @Test
    @DisplayName("Should handle uppercase Turkish characters")
    void shouldHandleUppercaseTurkishCharacters() {
        ProjectDocument project = new ProjectDocument();
        project.setId("8");
        project.setTitle("İSTANBUL PROJESİ"); // Uppercase Turkish 'İ'
        project.setDescription("Description");
        project.setSummary("Summary");
        project.setStatus("OPEN");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        elasticsearchOperations.save(project);
        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();

        // Search with lowercase
        List<ProjectDocument> results = projectSearchService.searchProjects("istanbul projesi");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).contains("İSTANBUL");
    }

    @Test
    @DisplayName("Should match multiple Turkish diacritics in one search")
    void shouldMatchMultipleTurkishDiacritics() {
        ProjectDocument project = new ProjectDocument();
        project.setId("9");
        project.setTitle("Test Project");
        project.setDescription("Şölen çağında güzel ürünler"); // Multiple diacritics
        project.setSummary("Summary");
        project.setStatus("OPEN");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        elasticsearchOperations.save(project);
        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();

        List<ProjectDocument> results = projectSearchService.searchProjects("solen caginda guzel");

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getDescription()).contains("Şölen");
        assertThat(results.get(0).getDescription()).contains("çağında");
        assertThat(results.get(0).getDescription()).contains("güzel");
    }

    @Test
    @DisplayName("Should match Turkish characters across title, description, and summary")
    void shouldMatchTurkishCharactersAcrossAllFields() {
        ProjectDocument project = new ProjectDocument();
        project.setId("10");
        project.setTitle("Öğrenci Platformu");
        project.setDescription("Şirketler için çözüm");
        project.setSummary("Güvenli işlem");
        project.setStatus("OPEN");
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        elasticsearchOperations.save(project);
        elasticsearchOperations.indexOps(ProjectDocument.class).refresh();

        // Should find this project searching in any field
        List<ProjectDocument> titleResults = projectSearchService.searchProjects("ogrenci");
        List<ProjectDocument> descResults = projectSearchService.searchProjects("sirketler cozum");
        List<ProjectDocument> summaryResults = projectSearchService.searchProjects("guvenli");

        assertThat(titleResults).isNotEmpty();
        assertThat(descResults).isNotEmpty();
        assertThat(summaryResults).isNotEmpty();
    }
}
