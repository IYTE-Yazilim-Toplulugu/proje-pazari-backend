package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.TestRateLimitConfig;
import com.iyte_yazilim.proje_pazari.TestRedisConfig;
import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import com.iyte_yazilim.proje_pazari.infrastructure.security.config.RateLimitConfig;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestRedisConfig.class, TestRateLimitConfig.class})
@ActiveProfiles("test")
@TestPropertySource(
        properties = {
            "spring.data.elasticsearch.enabled=false",
            "spring.data.elasticsearch.repositories.enabled=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration"
        })
class AdminControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtUtil jwtUtil;

    private String adminToken;

    @MockitoBean private StringRedisTemplate stringRedisTemplate;

    @MockitoBean private TokenBlacklistService tokenBlacklistService;

    @MockitoBean private JavaMailSender javaMailSender;

    @MockitoBean private RateLimitConfig rateLimitConfig;

    @BeforeEach
    void setUp() {
        adminToken = jwtUtil.generateToken("admin-test-id", "admin@test.com", "ADMIN");
        Bucket permissiveBucket =
                Bucket.builder()
                        .addLimit(
                                Bandwidth.builder()
                                        .capacity(1000)
                                        .refillIntervally(1000, Duration.ofMinutes(1))
                                        .build())
                        .build();
        when(rateLimitConfig.resolveBucket(anyString())).thenReturn(permissiveBucket);
    }

    // --- Access Control Tests ---

    @Nested
    @DisplayName("Access Control")
    class AccessControlTests {

        @Test
        @DisplayName(
                "Should return 403 Forbidden for list users endpoint when authenticated as USER")
        @WithMockUser(username = "user", roles = "USER")
        void shouldReturn403ForListUsersAsUser() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 200 OK for storage health endpoint")
        void shouldReturn200ForStorageHealth() throws Exception {
            mockMvc.perform(
                            get("/api/v1/admin/storage/health")
                                    .header("Authorization", "Bearer " + adminToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.available").exists());
        }

        @Test
        @DisplayName("Should deny unauthenticated access to admin endpoints")
        void shouldDenyUnauthenticatedAccess() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users").contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }
    }

    // --- Admin Endpoint Tests ---

    @Nested
    @DisplayName("Admin Endpoints")
    class AdminEndpointTests {

        @Test
        @DisplayName("Should return 200 OK for list users endpoint when authenticated as ADMIN")
        void shouldReturn200ForListUsers() throws Exception {
            mockMvc.perform(
                            get("/api/v1/admin/users")
                                    .header("Authorization", "Bearer " + adminToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0)); // ResponseCode.SUCCESS = 0
        }

        @Test
        @DisplayName("Should return 200 OK for system health endpoint")
        void shouldReturn200ForSystemHealth() throws Exception {
            mockMvc.perform(
                            get("/api/v1/admin/health/services")
                                    .header("Authorization", "Bearer " + adminToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").exists());
        }

        @Test
        @DisplayName("Should return 200 OK for system overview stats endpoint")
        void shouldReturn200ForSystemOverview() throws Exception {
            mockMvc.perform(
                            get("/api/v1/admin/stats/overview")
                                    .header("Authorization", "Bearer " + adminToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalUsers").exists());
        }

        @Test
        @DisplayName("Should return CSV content for export users endpoint")
        void shouldReturnCsvForExportUsers() throws Exception {
            mockMvc.perform(
                            get("/api/v1/admin/export/users")
                                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/csv;charset=UTF-8"));
        }
    }

    // --- Nice to Have Feature Endpoint Tests ---

    @Nested
    @DisplayName("Feature Flags Endpoint")
    class FeatureFlagEndpointTests {

        @Test
        @DisplayName("Should return 200 OK for get feature flags")
        void shouldReturn200ForGetFeatureFlags() throws Exception {
            mockMvc.perform(
                            get("/api/v1/admin/feature-flags")
                                    .header("Authorization", "Bearer " + adminToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }
    }

    @Nested
    @DisplayName("Banned IPs Endpoint")
    class BannedIpsEndpointTests {

        @Test
        @DisplayName("Should return 200 OK for get banned IPs")
        void shouldReturn200ForGetBannedIps() throws Exception {
            mockMvc.perform(
                            get("/api/v1/admin/ip-bans")
                                    .header("Authorization", "Bearer " + adminToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }
    }

    @Nested
    @DisplayName("Analytics Endpoint")
    class AnalyticsEndpointTests {

        @Test
        @DisplayName("Should return 200 OK for analytics trends")
        void shouldReturn200ForAnalyticsTrends() throws Exception {
            mockMvc.perform(
                            get("/api/v1/admin/analytics/trends")
                                    .header("Authorization", "Bearer " + adminToken)
                                    .param("days", "7")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }
    }

    // --- Promote to Project Owner Access Control Tests ---

    @Nested
    @DisplayName("POST /api/v1/admin/users/{userId}/promote-to-project-owner - Access Control")
    class PromoteToProjectOwnerAccessControlTests {

        @Test
        @DisplayName("Should return 403 when promoting without ADMIN role (PROJECT_OWNER)")
        @WithMockUser(username = "owner", roles = "PROJECT_OWNER")
        void shouldReturn403_whenNotAdmin() throws Exception {
            mockMvc.perform(
                            post("/api/v1/admin/users/some-user-id/promote-to-project-owner")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 when promoting as APPLICANT role")
        @WithMockUser(username = "applicant", roles = "APPLICANT")
        void shouldReturn403_whenApplicant() throws Exception {
            mockMvc.perform(
                            post("/api/v1/admin/users/some-user-id/promote-to-project-owner")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 when promoting as USER role")
        @WithMockUser(username = "user", roles = "USER")
        void shouldReturn403_whenUser() throws Exception {
            mockMvc.perform(
                            post("/api/v1/admin/users/some-user-id/promote-to-project-owner")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should deny unauthenticated promote request")
        void shouldDenyUnauthenticatedPromote() throws Exception {
            mockMvc.perform(
                            post("/api/v1/admin/users/some-user-id/promote-to-project-owner")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        }
    }
}
