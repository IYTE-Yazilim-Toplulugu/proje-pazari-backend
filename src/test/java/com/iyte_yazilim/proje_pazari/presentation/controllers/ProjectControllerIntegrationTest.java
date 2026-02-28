package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String testUserId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        UserEntity user = new UserEntity();
        user.setEmail("projcontroller-" + System.nanoTime() + "@std.iyte.edu.tr");
        user.setPassword(passwordEncoder.encode("TestPassword123!"));
        user.setFirstName("Project");
        user.setLastName("Owner");
        user.setIsActive(true);
        UserEntity saved = userRepository.save(user);
        testUserId = saved.getId();
        jwtToken = jwtUtil.generateToken(saved.getEmail());
    }

    @Test
    @DisplayName("POST /api/v1/projects - should create project when authenticated")
    void shouldCreateProject() throws Exception {
        Map<String, Object> request =
                Map.of(
                        "projectName",
                        "Integration Test Project",
                        "description",
                        "A project created during integration testing for verification",
                        "ownerId",
                        testUserId);

        mockMvc.perform(
                        post("/api/v1/projects")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.projectName").value("Integration Test Project"));
    }

    @Test
    @DisplayName("POST /api/v1/projects - should require authentication")
    void shouldRequireAuthForCreateProject() throws Exception {
        Map<String, Object> request =
                Map.of(
                        "projectName",
                        "Unauthorized Project",
                        "description",
                        "This should fail because no auth token is provided",
                        "ownerId",
                        "some-id");

        mockMvc.perform(
                        post("/api/v1/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/projects - should fail with validation errors")
    void shouldFailWithValidationErrors() throws Exception {
        Map<String, String> request = Map.of("projectName", "", "description", "", "ownerId", "");

        mockMvc.perform(
                        post("/api/v1/projects")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
