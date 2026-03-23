package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.TestRateLimitConfig;
import com.iyte_yazilim.proje_pazari.TestRedisConfig;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestRedisConfig.class, TestRateLimitConfig.class})
class ProjectControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private EmailVerificationRepository emailVerificationRepository;
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

        EmailVerificationEntity verification = new EmailVerificationEntity();
        verification.setUserId(saved.getId());
        verification.setEmail(saved.getEmail());
        verification.setToken("dummy-token-" + System.nanoTime());
        verification.setExpiresAt(LocalDateTime.now().plusHours(24));
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        testUserId = saved.getId();
        jwtToken = jwtUtil.generateToken(saved.getId(), saved.getEmail(), "PROJECT_OWNER");
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
