package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.elasticsearch.services.ProjectSearchService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchHitsImpl;
import org.springframework.data.elasticsearch.core.SearchPage;
import org.springframework.data.elasticsearch.core.TotalHitsRelation;
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

    @MockitoBean private ProjectSearchService searchService;

    private ProjectDocument sampleProject;

    @MockitoBean private JwtUtil jwtUtil;

    // 2. Mock the standard UserDetailsService
    @MockitoBean private UserDetailsService userDetailsService;

    // 3. Mock the AuthenticationProvider (if your SecurityConfig uses it)
    @MockitoBean private AuthenticationProvider authenticationProvider;

    // 4. Mock MessageService required by GlobalExceptionHandler
    @MockitoBean private MessageService messageService;

    // 5. Mock UserRepository required by UserLocaleInterceptor
    @MockitoBean private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Setup MessageService mock to return validation error message
        when(messageService.getMessage(anyString())).thenReturn("Validation error");

        sampleProject =
                ProjectDocument.builder()
                        .id("1")
                        .title("Java Spring Boot Project")
                        .description("A comprehensive Spring Boot application")
                        .summary("Backend development with Java")
                        .status("ACTIVE")
                        .tags(List.of("java", "spring"))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .applicationsCount(5)
                        .build();
    }

    private SearchPage<ProjectDocument> createSearchPage(List<ProjectDocument> documents) {
        List<SearchHit<ProjectDocument>> searchHits =
                documents.stream()
                        .map(
                                doc ->
                                        new SearchHit<>(
                                                null,
                                                doc.getId(),
                                                null,
                                                1.0f,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                doc))
                        .toList();

        SearchHits<ProjectDocument> hits =
                new SearchHitsImpl<>(
                        documents.size(),
                        TotalHitsRelation.EQUAL_TO,
                        1.0f,
                        null,
                        null,
                        null,
                        searchHits,
                        null,
                        null,
                        null);

        return SearchHitSupport.searchPageFor(hits, PageRequest.of(0, 10));
    }

    @Nested
    @DisplayName("GET /api/v1/search/projects")
    class SearchProjectsTests {

        @Test
        @WithMockUser
        @DisplayName("Should return projects when search is successful")
        void shouldReturnProjectsWhenSearchIsSuccessful() throws Exception {
            SearchPage<ProjectDocument> searchPage = createSearchPage(List.of(sampleProject));
            when(searchService.advancedSearch(eq("java"), isNull(), isNull(), any()))
                    .thenReturn(searchPage);

            mockMvc.perform(get("/api/v1/search/projects").param("q", "java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("Projects retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].title").value("Java Spring Boot Project"));

            verify(searchService).advancedSearch(eq("java"), isNull(), isNull(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return empty list when no projects match")
        void shouldReturnEmptyListWhenNoProjectsMatch() throws Exception {
            SearchPage<ProjectDocument> emptyPage = createSearchPage(Collections.emptyList());
            when(searchService.advancedSearch(eq("nonexistent"), isNull(), isNull(), any()))
                    .thenReturn(emptyPage);

            mockMvc.perform(get("/api/v1/search/projects").param("q", "nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("Should filter by status when provided")
        void shouldFilterByStatusWhenProvided() throws Exception {
            SearchPage<ProjectDocument> searchPage = createSearchPage(List.of(sampleProject));
            when(searchService.advancedSearch(eq("java"), eq("ACTIVE"), isNull(), any()))
                    .thenReturn(searchPage);

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "java")
                                    .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

            verify(searchService).advancedSearch(eq("java"), eq("ACTIVE"), isNull(), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should filter by tags when provided")
        void shouldFilterByTagsWhenProvided() throws Exception {
            SearchPage<ProjectDocument> searchPage = createSearchPage(List.of(sampleProject));
            when(searchService.advancedSearch(eq("java"), isNull(), eq(List.of("spring")), any()))
                    .thenReturn(searchPage);

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "java")
                                    .param("tags", "spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());

            verify(searchService)
                    .advancedSearch(eq("java"), isNull(), eq(List.of("spring")), any());
        }

        @Test
        @WithMockUser
        @DisplayName("Should use pagination parameters")
        void shouldUsePaginationParameters() throws Exception {
            SearchPage<ProjectDocument> searchPage = createSearchPage(List.of(sampleProject));
            when(searchService.advancedSearch(anyString(), any(), any(), any()))
                    .thenReturn(searchPage);

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "java")
                                    .param("page", "2")
                                    .param("size", "20"))
                    .andExpect(status().isOk());

            verify(searchService)
                    .advancedSearch(
                            eq("java"),
                            isNull(),
                            isNull(),
                            argThat(
                                    pageable ->
                                            pageable.getPageNumber() == 2
                                                    && pageable.getPageSize() == 20));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when query is blank")
        void shouldReturnBadRequestWhenQueryIsBlank() throws Exception {
            mockMvc.perform(get("/api/v1/search/projects").param("q", ""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when query is missing")
        void shouldReturnBadRequestWhenQueryIsMissing() throws Exception {
            mockMvc.perform(get("/api/v1/search/projects")).andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when query is too short")
        void shouldReturnBadRequestWhenQueryIsTooShort() throws Exception {
            // @Size(min = 2) for projects search
            mockMvc.perform(get("/api/v1/search/projects").param("q", "a"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when query exceeds max length")
        void shouldReturnBadRequestWhenQueryExceedsMaxLength() throws Exception {
            String longQuery = "a".repeat(101);
            mockMvc.perform(get("/api/v1/search/projects").param("q", longQuery))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when page is negative")
        void shouldReturnBadRequestWhenPageIsNegative() throws Exception {
            mockMvc.perform(get("/api/v1/search/projects").param("q", "java").param("page", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when size is zero")
        void shouldReturnBadRequestWhenSizeIsZero() throws Exception {
            mockMvc.perform(get("/api/v1/search/projects").param("q", "java").param("size", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when size exceeds maximum")
        void shouldReturnBadRequestWhenSizeExceedsMaximum() throws Exception {
            mockMvc.perform(get("/api/v1/search/projects").param("q", "java").param("size", "101"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        // REMOVED @WithMockUser to ensure true unauthorized response
        @DisplayName("Should return unauthorized when user is not authenticated")
        void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/search/projects").param("q", "java"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/search/projects/suggest")
    class SuggestProjectsTests {

        @Test
        @WithMockUser
        @DisplayName("Should return suggestions when query is valid")
        void shouldReturnSuggestionsWhenQueryIsValid() throws Exception {
            List<String> suggestions =
                    List.of("Java Spring Boot Project", "Java Backend Application");
            when(searchService.getSuggestions("java")).thenReturn(suggestions);

            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", "java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("Suggestions retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0]").value("Java Spring Boot Project"))
                    .andExpect(jsonPath("$.data[1]").value("Java Backend Application"));

            verify(searchService).getSuggestions("java");
        }

        @Test
        @WithMockUser
        @DisplayName("Should return empty list when no suggestions found")
        void shouldReturnEmptyListWhenNoSuggestionsFound() throws Exception {
            when(searchService.getSuggestions("xyz")).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("Should accept single character query")
        void shouldAcceptSingleCharacterQuery() throws Exception {
            // @Size(min = 1) for suggestions
            when(searchService.getSuggestions("j")).thenReturn(List.of("Java Project"));

            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", "j"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0]").value("Java Project"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when query is blank")
        void shouldReturnBadRequestWhenQueryIsBlank() throws Exception {
            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", ""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when query is missing")
        void shouldReturnBadRequestWhenQueryIsMissing() throws Exception {
            mockMvc.perform(get("/api/v1/search/projects/suggest"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when query exceeds max length")
        void shouldReturnBadRequestWhenQueryExceedsMaxLength() throws Exception {
            String longQuery = "a".repeat(101);
            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", longQuery))
                    .andExpect(status().isBadRequest());
        }

        @Test
        // REMOVED @WithMockUser to ensure true unauthorized response
        @DisplayName("Should return unauthorized when user is not authenticated")
        void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", "java"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/search/projects/statistics")
    class GetStatisticsTests {

        @Test
        @WithMockUser
        @DisplayName("Should return statistics successfully")
        void shouldReturnStatisticsSuccessfully() throws Exception {
            Map<String, Long> stats = Map.of("ACTIVE", 10L, "COMPLETED", 5L, "DRAFT", 3L);
            when(searchService.getProjectStatistics()).thenReturn(stats);

            mockMvc.perform(get("/api/v1/search/projects/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("Statistics retrieved successfully"))
                    .andExpect(jsonPath("$.data.ACTIVE").value(10))
                    .andExpect(jsonPath("$.data.COMPLETED").value(5))
                    .andExpect(jsonPath("$.data.DRAFT").value(3));

            verify(searchService).getProjectStatistics();
        }

        @Test
        @WithMockUser
        @DisplayName("Should return empty statistics when no data")
        void shouldReturnEmptyStatisticsWhenNoData() throws Exception {
            when(searchService.getProjectStatistics()).thenReturn(Collections.emptyMap());

            mockMvc.perform(get("/api/v1/search/projects/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        // REMOVED @WithMockUser to ensure true unauthorized response
        @DisplayName("Should return unauthorized when user is not authenticated")
        void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/search/projects/statistics"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
