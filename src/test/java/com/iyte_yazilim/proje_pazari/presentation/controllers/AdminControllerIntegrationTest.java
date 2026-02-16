package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "spring.data.elasticsearch.enabled=false",
            "spring.data.elasticsearch.repositories.enabled=false",
            "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration,org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration,org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration"
        })
class AdminControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    @DisplayName("Should return 200 OK for list users endpoint when authenticated as ADMIN")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldReturn200ForListUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    @Test
    @DisplayName("Should return 403 Forbidden for list users endpoint when authenticated as USER")
    @WithMockUser(username = "user", roles = "USER")
    void shouldReturn403ForListUsersAsUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
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
