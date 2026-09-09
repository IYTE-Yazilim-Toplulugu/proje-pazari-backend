package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IMediator;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.queries.getProjectStatistics.GetProjectStatisticsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.searchProjects.SearchProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.suggestProjects.SuggestProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import com.iyte_yazilim.proje_pazari.presentation.mappers.IRequestMapper;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = SearchController.class,
        properties = "spring.data.elasticsearch.enabled=true")
class SearchControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private IMediator mediator;

    @MockitoBean private IRequestMapper requestMapper;

    @MockitoBean private JwtUtil jwtUtil;

    @MockitoBean private UserDetailsService userDetailsService;

    @MockitoBean private AuthenticationProvider authenticationProvider;

    @MockitoBean private MessageService messageService;

    @MockitoBean private UserRepository userRepository;

    @MockitoBean
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService
            tokenBlacklistService;

    @MockitoBean
    private com.iyte_yazilim.proje_pazari.infrastructure.security.config.RateLimitConfig
            rateLimitConfig;

    @MockitoBean
    private com.iyte_yazilim.proje_pazari.infrastructure.security.filter.IpBanFilter ipBanFilter;

    @MockitoBean
    private com.iyte_yazilim.proje_pazari.infrastructure.security.filter.MaintenanceModeFilter
            maintenanceModeFilter;

    private ProjectDocument projectWithTitle;
    private ProjectDocument projectWithDescription;

    @BeforeEach
    void setUp() throws Exception {
        when(messageService.getMessage(anyString())).thenReturn("Validation error");

        Mockito.doAnswer(
                        invocation -> {
                            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
                            return null;
                        })
                .when(ipBanFilter)
                .doFilter(any(), any(), any());

        Mockito.doAnswer(
                        invocation -> {
                            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
                            return null;
                        })
                .when(maintenanceModeFilter)
                .doFilter(any(), any(), any());

        projectWithTitle =
                ProjectDocument.builder()
                        .id("1")
                        .title("Machine Learning Pipeline")
                        .description("A data processing pipeline")
                        .summary("ML project for data engineering")
                        .status("OPEN")
                        .ownerId("owner-1")
                        .ownerName("Jane Smith")
                        .ownerEmail("jane@example.com")
                        .requiredSkills(List.of("python", "ml", "data"))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .applicationCount(3)
                        .build();

        projectWithDescription =
                ProjectDocument.builder()
                        .id("2")
                        .title("Web Application")
                        .description("A comprehensive React frontend with machine learning backend")
                        .summary("Full-stack web app")
                        .status("COMPLETED")
                        .ownerId("owner-2")
                        .ownerName("John Smith")
                        .ownerEmail("john@example.com")
                        .requiredSkills(List.of("react", "javascript"))
                        .createdAt(LocalDateTime.now().minusDays(10))
                        .updatedAt(LocalDateTime.now().minusDays(5))
                        .applicationCount(8)
                        .build();
    }

    // ── 1. Search by Keyword in Title ───────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/projects - Search by Title")
    class SearchByTitleTests {

        @Test
        @WithMockUser
        @DisplayName("1. Search keyword matching title returns matching projects")
        void search_keywordInTitle_returnsMatches() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "Machine Learning"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects").isArray())
                    .andExpect(
                            jsonPath("$.data.projects[0].projectName")
                                    .value("Machine Learning Pipeline"))
                    .andExpect(jsonPath("$.data.projects[0].projectId").value("1"));
        }

        @Test
        @WithMockUser
        @DisplayName("2. Search with partial title match returns results")
        void search_partialTitle_returnsMatches() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "Pipeline"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.projects[0].projectName")
                                    .value("Machine Learning Pipeline"));
        }
    }

    // ── 3. Search by Keyword in Description ─────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/projects - Search by Description")
    class SearchByDescriptionTests {

        @Test
        @WithMockUser
        @DisplayName("1. Search keyword matching description returns matching projects")
        void search_keywordInDescription_returnsMatches() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithDescription));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "React frontend"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects").isArray())
                    .andExpect(jsonPath("$.data.projects[0].projectName").value("Web Application"))
                    .andExpect(
                            jsonPath("$.data.projects[0].description")
                                    .value(
                                            "A comprehensive React frontend with machine learning backend"));
        }

        @Test
        @WithMockUser
        @DisplayName("2. Search returns results from both title and description matches")
        void search_matchesTitleAndDescription_returnsBoth() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle, projectWithDescription));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "machine learning"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects").isArray())
                    .andExpect(jsonPath("$.data.projects.length()").value(2));
        }
    }

    // ── 4. Search with Filters ──────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/projects - Filter Tests")
    class FilterTests {

        @Test
        @WithMockUser
        @DisplayName("1. Filter by status OPEN returns only active projects")
        void search_filterByActiveStatus_returnsOnlyActive() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle));

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "project")
                                    .param("status", "OPEN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects[0].status").value("OPEN"));

            ArgumentCaptor<SearchProjectsQuery> captor =
                    ArgumentCaptor.forClass(SearchProjectsQuery.class);
            verify(mediator).send(captor.capture());
            assertThat(captor.getValue().status()).isEqualTo("OPEN");
        }

        @Test
        @WithMockUser
        @DisplayName("2. Filter by status COMPLETED returns only completed projects")
        void search_filterByCompletedStatus_returnsOnlyCompleted() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithDescription));

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "project")
                                    .param("status", "COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects[0].status").value("COMPLETED"));

            ArgumentCaptor<SearchProjectsQuery> captor =
                    ArgumentCaptor.forClass(SearchProjectsQuery.class);
            verify(mediator).send(captor.capture());
            assertThat(captor.getValue().status()).isEqualTo("COMPLETED");
        }

        @Test
        @WithMockUser
        @DisplayName("3. Filter by status passes status to query")
        void search_filterByStatus_passesStatus() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle));

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "project")
                                    .param("status", "ACTIVE"))
                    .andExpect(status().isOk());

            ArgumentCaptor<SearchProjectsQuery> captor =
                    ArgumentCaptor.forClass(SearchProjectsQuery.class);
            verify(mediator).send(captor.capture());
            assertThat(captor.getValue().status()).isEqualTo("ACTIVE");
        }

        @Test
        @WithMockUser
        @DisplayName("4. Search without filters uses default values")
        void search_noFilters_usesDefaults() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "project"))
                    .andExpect(status().isOk());

            ArgumentCaptor<SearchProjectsQuery> captor =
                    ArgumentCaptor.forClass(SearchProjectsQuery.class);
            verify(mediator).send(captor.capture());
            assertThat(captor.getValue().page()).isZero();
            assertThat(captor.getValue().size()).isEqualTo(10);
            assertThat(captor.getValue().status()).isNull();
        }
    }

    // ── 5. Pagination Tests ─────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/projects - Pagination")
    class PaginationTests {

        @Test
        @WithMockUser
        @DisplayName("1. Custom page and size values are passed to query")
        void search_customPagination_passedToQuery() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle));

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "java")
                                    .param("page", "3")
                                    .param("size", "25"))
                    .andExpect(status().isOk());

            ArgumentCaptor<SearchProjectsQuery> captor =
                    ArgumentCaptor.forClass(SearchProjectsQuery.class);
            verify(mediator).send(captor.capture());
            assertThat(captor.getValue().page()).isEqualTo(3);
            assertThat(captor.getValue().size()).isEqualTo(25);
        }

        @Test
        @WithMockUser
        @DisplayName("2. First page with default size returns results")
        void search_firstPageDefaultSize_returnsResults() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle, projectWithDescription));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "project").param("page", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects.length()").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("3. Page beyond results returns empty list")
        void search_pageBeyondResults_returnsEmpty() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class))).thenReturn(searchResponse());

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "project")
                                    .param("page", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("4. Small page size returns limited results")
        void search_smallPageSize_returnsLimited() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "project").param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects.length()").value(1));
        }
    }

    // ── 6. Result Ranking and Content Tests ─────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/projects - Result Content")
    class ResultContentTests {

        @Test
        @WithMockUser
        @DisplayName("1. Search results contain expected fields")
        void search_results_containExpectedFields() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "machine"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects[0].projectId").exists())
                    .andExpect(jsonPath("$.data.projects[0].projectName").exists())
                    .andExpect(jsonPath("$.data.projects[0].description").exists())
                    .andExpect(jsonPath("$.data.projects[0].status").exists())
                    .andExpect(jsonPath("$.data.projects[0].requiredSkills").exists())
                    .andExpect(jsonPath("$.data.projects[0].applicationCount").exists());
        }

        @Test
        @WithMockUser
        @DisplayName("2. Search results have correct requiredSkills values")
        void search_results_haveCorrectRequiredSkills() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "machine"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects[0].requiredSkills").isArray())
                    .andExpect(jsonPath("$.data.projects[0].requiredSkills[0]").value("python"))
                    .andExpect(jsonPath("$.data.projects[0].requiredSkills[1]").value("ml"))
                    .andExpect(jsonPath("$.data.projects[0].requiredSkills[2]").value("data"));
        }

        @Test
        @WithMockUser
        @DisplayName("3. Title-match results ranked before description-match results")
        void search_titleMatchRankedHigher() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(searchResponse(projectWithTitle, projectWithDescription));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "machine learning"))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.projects[0].projectName")
                                    .value("Machine Learning Pipeline"))
                    .andExpect(jsonPath("$.data.projects[1].projectName").value("Web Application"));
        }
    }

    // ── 7. No Results Tests ─────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/projects - No Results")
    class NoResultsTests {

        @Test
        @WithMockUser
        @DisplayName("1. Search with no matching results returns empty list with 200")
        void search_noResults_returnsEmptyList() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class))).thenReturn(searchResponse());

            mockMvc.perform(get("/api/v1/search/projects").param("q", "xyznonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.projects").isArray())
                    .andExpect(jsonPath("$.data.projects").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("2. Search with filters yielding no results returns empty list")
        void search_noResultsWithFilters_returnsEmptyList() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class))).thenReturn(searchResponse());

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "project")
                                    .param("status", "DRAFT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects").isEmpty());
        }
    }

    // ── 8. Suggest Endpoint Tests ───────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/projects/suggest - Additional Tests")
    class SuggestAdditionalTests {

        @Test
        @WithMockUser
        @DisplayName("1. Suggest returns multiple suggestions")
        void suggest_returnsMultipleSuggestions() throws Exception {
            List<String> suggestions =
                    List.of("Machine Learning Pipeline", "Machine Learning Basics", "ML Project");
            when(mediator.send(any(SuggestProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(suggestions, "Suggestions retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", "machine"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0]").value("Machine Learning Pipeline"));
        }

        @Test
        @WithMockUser
        @DisplayName("2. Suggest passes correct query parameter")
        void suggest_passesCorrectQuery() throws Exception {
            when(mediator.send(any(SuggestProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    Collections.emptyList(), "Suggestions retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", "spring boot"))
                    .andExpect(status().isOk());

            ArgumentCaptor<SuggestProjectsQuery> captor =
                    ArgumentCaptor.forClass(SuggestProjectsQuery.class);
            verify(mediator).send(captor.capture());
            assertThat(captor.getValue().q()).isEqualTo("spring boot");
        }

        @Test
        @WithMockUser
        @DisplayName("3. Suggest with special characters in query succeeds")
        void suggest_specialCharacters_succeeds() throws Exception {
            when(mediator.send(any(SuggestProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    Collections.emptyList(), "Suggestions retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", "C++ & Java"))
                    .andExpect(status().isOk());
        }
    }

    // ── 9. Statistics Endpoint Tests ────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/search/projects/statistics - Additional Tests")
    class StatisticsAdditionalTests {

        @Test
        @WithMockUser
        @DisplayName("1. Statistics returns all status counts")
        void statistics_returnsAllStatusCounts() throws Exception {
            Map<String, Long> stats =
                    Map.of("OPEN", 15L, "COMPLETED", 8L, "DRAFT", 4L, "CANCELLED", 2L);
            when(mediator.send(any(GetProjectStatisticsQuery.class)))
                    .thenReturn(ApiResponse.success(stats, "Statistics retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.OPEN").value(15))
                    .andExpect(jsonPath("$.data.COMPLETED").value(8))
                    .andExpect(jsonPath("$.data.DRAFT").value(4))
                    .andExpect(jsonPath("$.data.CANCELLED").value(2));
        }

        @Test
        @WithMockUser
        @DisplayName("2. Statistics with only one status returns single entry")
        void statistics_singleStatus_returnsSingleEntry() throws Exception {
            Map<String, Long> stats = Map.of("OPEN", 5L);
            when(mediator.send(any(GetProjectStatisticsQuery.class)))
                    .thenReturn(ApiResponse.success(stats, "Statistics retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.OPEN").value(5))
                    .andExpect(jsonPath("$.data.COMPLETED").doesNotExist());
        }
    }

    // ── 10. Edge Cases ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Search Endpoints - Edge Cases")
    class EdgeCaseTests {

        @Test
        @WithMockUser
        @DisplayName("1. Search with special characters returns results")
        void search_specialCharacters_returnsResults() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class))).thenReturn(searchResponse());

            mockMvc.perform(get("/api/v1/search/projects").param("q", "C++ programming"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects").isArray());
        }

        @Test
        @WithMockUser
        @DisplayName("2. Search with unicode characters returns results")
        void search_unicodeCharacters_returnsResults() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class))).thenReturn(searchResponse());

            mockMvc.perform(get("/api/v1/search/projects").param("q", "proje gelistirme"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects").isArray());
        }

        @Test
        @WithMockUser
        @DisplayName("3. Search with exactly minimum length query succeeds")
        void search_minLengthQuery_succeeds() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class))).thenReturn(searchResponse());

            mockMvc.perform(get("/api/v1/search/projects").param("q", "ab"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("4. Search with exactly maximum length query succeeds")
        void search_maxLengthQuery_succeeds() throws Exception {
            String maxQuery = "a".repeat(100);
            when(mediator.send(any(SearchProjectsQuery.class))).thenReturn(searchResponse());

            mockMvc.perform(get("/api/v1/search/projects").param("q", maxQuery))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("5. Search with maximum valid page size succeeds")
        void search_maxPageSize_succeeds() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class))).thenReturn(searchResponse());

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "project")
                                    .param("size", "100"))
                    .andExpect(status().isOk());

            ArgumentCaptor<SearchProjectsQuery> captor =
                    ArgumentCaptor.forClass(SearchProjectsQuery.class);
            verify(mediator).send(captor.capture());
            assertThat(captor.getValue().size()).isEqualTo(100);
        }
    }

    private ApiResponse<PagedProjectsResult> searchResponse(ProjectDocument... documents) {
        List<ProjectDetailDto> projects = List.of(documents).stream().map(this::toDto).toList();
        return ApiResponse.success(
                new PagedProjectsResult(projects, 0, projects.isEmpty() ? 0 : 1, projects.size()),
                "Projects retrieved successfully");
    }

    private ProjectDetailDto toDto(ProjectDocument document) {
        return new ProjectDetailDto(
                document.getId(),
                document.getOwnerId(),
                document.getOwnerName(),
                document.getOwnerEmail(),
                document.getTitle(),
                document.getDescription(),
                document.getSummary(),
                document.getApplicationCount(),
                ProjectStatus.fromString(document.getStatus()),
                document.getMaxTeamSize(),
                document.getRequiredSkills() != null ? document.getRequiredSkills() : List.of(),
                document.getCategory(),
                document.getDeadline(),
                document.getCreatedAt());
    }
}
