package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.application.common.IMediator;
import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.application.queries.getProjectStatistics.GetProjectStatisticsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.searchProjects.SearchProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.queries.suggestProjects.SuggestProjectsQuery;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
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

    private ProjectDocument sampleProject;

    @BeforeEach
    void setUp() throws Exception {
        when(messageService.getMessage(anyString())).thenReturn("Validation error");

        // Mock filters to allow requests to proceed
        Mockito.doAnswer(
                        invocation -> {
                            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
                            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
                            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                            chain.doFilter(request, response);
                            return null;
                        })
                .when(ipBanFilter)
                .doFilter(any(), any(), any());

        Mockito.doAnswer(
                        invocation -> {
                            jakarta.servlet.ServletRequest request = invocation.getArgument(0);
                            jakarta.servlet.ServletResponse response = invocation.getArgument(1);
                            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
                            chain.doFilter(request, response);
                            return null;
                        })
                .when(maintenanceModeFilter)
                .doFilter(any(), any(), any());

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

    @Nested
    @DisplayName("GET /api/v1/search/projects")
    class SearchProjectsTests {

        @Test
        @WithMockUser
        @DisplayName("Should return projects when search is successful")
        void shouldReturnProjectsWhenSearchIsSuccessful() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    List.of(sampleProject), "Projects retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("Projects retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].title").value("Java Spring Boot Project"));

            verify(mediator).send(any(SearchProjectsQuery.class));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return empty list when no projects match")
        void shouldReturnEmptyListWhenNoProjectsMatch() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    Collections.emptyList(), "Projects retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/search/projects - Advanced Filters")
    class AdvancedSearchProjectsTests {

        @Test
        @WithMockUser
        @DisplayName("Should pass correct parameters to mediator")
        void shouldPassCorrectParametersToMediator() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    List.of(sampleProject), "Projects retrieved successfully"));

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "java")
                                    .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));

            ArgumentCaptor<SearchProjectsQuery> captor =
                    ArgumentCaptor.forClass(SearchProjectsQuery.class);
            verify(mediator).send(captor.capture());
            SearchProjectsQuery captured = captor.getValue();
            assert captured.q().equals("java");
            assert captured.status().equals("ACTIVE");
        }

        @Test
        @WithMockUser
        @DisplayName("Should filter by tags when provided")
        void shouldFilterByTagsWhenProvided() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    List.of(sampleProject), "Projects retrieved successfully"));

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "java")
                                    .param("tags", "spring"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());

            ArgumentCaptor<SearchProjectsQuery> captor =
                    ArgumentCaptor.forClass(SearchProjectsQuery.class);
            verify(mediator).send(captor.capture());
            assert captor.getValue().tags().contains("spring");
        }

        @Test
        @WithMockUser
        @DisplayName("Should use pagination parameters")
        void shouldUsePaginationParameters() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    List.of(sampleProject), "Projects retrieved successfully"));

            mockMvc.perform(
                            get("/api/v1/search/projects")
                                    .param("q", "java")
                                    .param("page", "2")
                                    .param("size", "20"))
                    .andExpect(status().isOk());

            ArgumentCaptor<SearchProjectsQuery> captor =
                    ArgumentCaptor.forClass(SearchProjectsQuery.class);
            verify(mediator).send(captor.capture());
            assert captor.getValue().page() == 2;
            assert captor.getValue().size() == 20;
        }
    }

    @Nested
    @DisplayName("GET /api/v1/search/projects - Validation Tests")
    class ValidationTests {

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when query is blank")
        void shouldReturnBadRequestWhenQueryIsBlank() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenThrow(new ValidationException("q: must not be blank"));

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
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenThrow(new ValidationException("q: size must be between 2 and 100"));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "a"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when query exceeds max length")
        void shouldReturnBadRequestWhenQueryExceedsMaxLength() throws Exception {
            String longQuery = "a".repeat(101);
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenThrow(new ValidationException("q: size must be between 2 and 100"));

            mockMvc.perform(get("/api/v1/search/projects").param("q", longQuery))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when page is negative")
        void shouldReturnBadRequestWhenPageIsNegative() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenThrow(new ValidationException("page: must be greater than or equal to 0"));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "java").param("page", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when size is zero")
        void shouldReturnBadRequestWhenSizeIsZero() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenThrow(new ValidationException("size: must be greater than or equal to 1"));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "java").param("size", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when size exceeds maximum")
        void shouldReturnBadRequestWhenSizeExceedsMaximum() throws Exception {
            when(mediator.send(any(SearchProjectsQuery.class)))
                    .thenThrow(new ValidationException("size: must be less than or equal to 100"));

            mockMvc.perform(get("/api/v1/search/projects").param("q", "java").param("size", "101"))
                    .andExpect(status().isBadRequest());
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
            when(mediator.send(any(SuggestProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(suggestions, "Suggestions retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", "java"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("Suggestions retrieved successfully"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0]").value("Java Spring Boot Project"))
                    .andExpect(jsonPath("$.data[1]").value("Java Backend Application"));

            verify(mediator).send(any(SuggestProjectsQuery.class));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return empty list when no suggestions found")
        void shouldReturnEmptyListWhenNoSuggestionsFound() throws Exception {
            when(mediator.send(any(SuggestProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    Collections.emptyList(), "Suggestions retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @WithMockUser
        @DisplayName("Should accept single character query")
        void shouldAcceptSingleCharacterQuery() throws Exception {
            when(mediator.send(any(SuggestProjectsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    List.of("Java Project"), "Suggestions retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", "j"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0]").value("Java Project"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return bad request when query is blank")
        void shouldReturnBadRequestWhenQueryIsBlank() throws Exception {
            when(mediator.send(any(SuggestProjectsQuery.class)))
                    .thenThrow(new ValidationException("q: must not be blank"));

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
            when(mediator.send(any(SuggestProjectsQuery.class)))
                    .thenThrow(new ValidationException("q: size must be between 1 and 100"));

            mockMvc.perform(get("/api/v1/search/projects/suggest").param("q", longQuery))
                    .andExpect(status().isBadRequest());
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
            when(mediator.send(any(GetProjectStatisticsQuery.class)))
                    .thenReturn(ApiResponse.success(stats, "Statistics retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.message").value("Statistics retrieved successfully"))
                    .andExpect(jsonPath("$.data.ACTIVE").value(10))
                    .andExpect(jsonPath("$.data.COMPLETED").value(5))
                    .andExpect(jsonPath("$.data.DRAFT").value(3));

            verify(mediator).send(any(GetProjectStatisticsQuery.class));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return empty statistics when no data")
        void shouldReturnEmptyStatisticsWhenNoData() throws Exception {
            when(mediator.send(any(GetProjectStatisticsQuery.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    Collections.emptyMap(), "Statistics retrieved successfully"));

            mockMvc.perform(get("/api/v1/search/projects/statistics"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}
