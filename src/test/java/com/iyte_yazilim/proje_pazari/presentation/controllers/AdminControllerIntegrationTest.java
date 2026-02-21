package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
                "spring.data.elasticsearch.enabled=false",
                "spring.data.elasticsearch.repositories.enabled=false",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration"
})
class AdminControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        // --- Access Control Tests ---

        @Nested
        @DisplayName("Access Control")
        class AccessControlTests {

                @Test
                @DisplayName("Should return 403 Forbidden for list users endpoint when authenticated as USER")
                @WithMockUser(username = "user", roles = "USER")
                void shouldReturn403ForListUsersAsUser() throws Exception {
                        mockMvc.perform(get("/api/v1/admin/users").contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("Should return 403 for APPLICANT role")
                @WithMockUser(username = "applicant", roles = "APPLICANT")
                void shouldReturn403ForApplicantRole() throws Exception {
                        mockMvc.perform(get("/api/v1/admin/users").contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isForbidden());
                }

                @Test
                @DisplayName("Should return 403 for PROJECT_OWNER role")
                @WithMockUser(username = "owner", roles = "PROJECT_OWNER")
                void shouldReturn403ForProjectOwnerRole() throws Exception {
                        mockMvc.perform(get("/api/v1/admin/users").contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isForbidden());
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
                @WithMockUser(username = "admin", roles = "ADMIN")
                void shouldReturn200ForListUsers() throws Exception {
                        mockMvc.perform(get("/api/v1/admin/users").contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.code").value(0)); // ResponseCode.SUCCESS = 0
                }

                @Test
                @DisplayName("Should return 200 OK for system health endpoint")
                @WithMockUser(username = "admin", roles = "ADMIN")
                void shouldReturn200ForSystemHealth() throws Exception {
                        mockMvc.perform(
                                        get("/api/v1/admin/health/services")
                                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.status").exists());
                }

                @Test
                @DisplayName("Should return 200 OK for storage health endpoint")
                @WithMockUser(username = "admin", roles = "ADMIN")
                void shouldReturn200ForStorageHealth() throws Exception {
                        mockMvc.perform(
                                        get("/api/v1/admin/storage/health")
                                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.available").exists());
                }

                @Test
                @DisplayName("Should return 200 OK for system overview stats endpoint")
                @WithMockUser(username = "admin", roles = "ADMIN")
                void shouldReturn200ForSystemOverview() throws Exception {
                        mockMvc.perform(get("/api/v1/admin/stats/overview").contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.data.totalUsers").exists());
                }

                @Test
                @DisplayName("Should return CSV content for export users endpoint")
                @WithMockUser(username = "admin", roles = "ADMIN")
                void shouldReturnCsvForExportUsers() throws Exception {
                        mockMvc.perform(get("/api/v1/admin/export/users"))
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
                @WithMockUser(username = "admin", roles = "ADMIN")
                void shouldReturn200ForGetFeatureFlags() throws Exception {
                        mockMvc.perform(get("/api/v1/admin/feature-flags")
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
                @WithMockUser(username = "admin", roles = "ADMIN")
                void shouldReturn200ForGetBannedIps() throws Exception {
                        mockMvc.perform(get("/api/v1/admin/ip-bans")
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
                @WithMockUser(username = "admin", roles = "ADMIN")
                void shouldReturn200ForAnalyticsTrends() throws Exception {
                        mockMvc.perform(get("/api/v1/admin/analytics/trends")
                                        .param("days", "7")
                                        .contentType(MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$.code").value(0));
                }
        }
}
