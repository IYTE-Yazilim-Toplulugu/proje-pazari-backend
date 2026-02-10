package com.iyte_yazilim.proje_pazari.e2e;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserJourneyE2ETest {

        @Autowired
        private MockMvc mockMvc;
        private final ObjectMapper objectMapper = new ObjectMapper();

        private static String jwtToken;
        private static String userId;

        @Test
        @Order(1)
        @DisplayName("E2E: User registers successfully")
        void step1_register() throws Exception {
                Map<String, String> request = Map.of(
                                "email", "e2e-journey@std.iyte.edu.tr",
                                "password", "SecureE2EPassword123!",
                                "firstName", "E2E",
                                "lastName", "Tester");

                mockMvc.perform(
                                post("/api/v1/auth/register")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data").exists());
        }

        @Test
        @Order(2)
        @DisplayName("E2E: User logs in and receives JWT token")
        void step2_login() throws Exception {
                Map<String, String> request = Map.of(
                                "email", "e2e-journey@std.iyte.edu.tr",
                                "password", "SecureE2EPassword123!");

                MvcResult result = mockMvc.perform(
                                post("/api/v1/auth/login")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.token").exists())
                                .andReturn();

                String responseBody = result.getResponse().getContentAsString();
                JsonNode body = objectMapper.readTree(responseBody);
                jwtToken = body.get("data").get("token").asText();
                userId = body.get("data").get("userId").asText();
        }

        @Test
        @Order(3)
        @DisplayName("E2E: User views own profile")
        void step3_viewProfile() throws Exception {
                mockMvc.perform(
                                get("/api/v1/users/me")
                                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(userId))
                                .andExpect(jsonPath("$.data.firstName").value("E2E"))
                                .andExpect(jsonPath("$.data.lastName").value("Tester"));
        }

        @Test
        @Order(4)
        @DisplayName("E2E: User updates profile")
        void step4_updateProfile() throws Exception {
                Map<String, String> updateRequest = Map.of(
                                "firstName", "Updated",
                                "lastName", "E2ETester",
                                "description", "An end-to-end test user profile");

                mockMvc.perform(
                                put("/api/v1/users/me")
                                                .header("Authorization", "Bearer " + jwtToken)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                                .andExpect(jsonPath("$.data.lastName").value("E2ETester"));
        }

        @Test
        @Order(5)
        @DisplayName("E2E: User views public profile by ID")
        void step5_viewPublicProfile() throws Exception {
                mockMvc.perform(get("/api/v1/users/{userId}", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.id").value(userId))
                                .andExpect(jsonPath("$.data.firstName").value("Updated"));
        }

        @Test
        @Order(6)
        @DisplayName("E2E: User creates a project")
        void step6_createProject() throws Exception {
                Map<String, Object> projectRequest = Map.of(
                                "projectName", "E2E Test Project",
                                "description",
                                "A project created during the E2E user journey test to verify full flow",
                                "ownerId", userId);

                mockMvc.perform(
                                post("/api/v1/projects")
                                                .header("Authorization", "Bearer " + jwtToken)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(projectRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.data.projectName").value("E2E Test Project"));
        }

        @Test
        @Order(7)
        @DisplayName("E2E: Health check endpoint is accessible")
        void step7_healthCheck() throws Exception {
                mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
        }
}
