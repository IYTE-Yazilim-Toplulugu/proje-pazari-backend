package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    private String testUserId;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        // Create a test user directly in the DB
        UserEntity user = new UserEntity();
        user.setEmail("usercontroller-" + System.nanoTime() + "@std.iyte.edu.tr");
        user.setPassword(passwordEncoder.encode("TestPassword123!"));
        user.setFirstName("Test");
        user.setLastName("User");
        user.setIsActive(true);
        UserEntity saved = userRepository.save(user);
        testUserId = saved.getId();
        jwtToken = jwtUtil.generateToken(saved.getEmail());
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} - should return user profile (public)")
    void shouldReturnUserProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}", testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUserId))
                .andExpect(jsonPath("$.data.firstName").value("Test"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{userId} - should return 404 for unknown user")
    void shouldReturn404ForUnknownUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}", "nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/users - should require authentication")
    void shouldRequireAuthForGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/users - should return users when authenticated")
    void shouldReturnUsersWhenAuthenticated() throws Exception {
        mockMvc.perform(
                get("/api/v1/users")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/users/me - should return current user profile")
    void shouldReturnCurrentUserProfile() throws Exception {
        mockMvc.perform(
                get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUserId));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me - should update profile when authenticated")
    void shouldUpdateProfile() throws Exception {
        Map<String, String> updateRequest = Map.of(
                "firstName", "Updated",
                "lastName", "Name",
                "description", "New bio description");

        mockMvc.perform(
                put("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.lastName").value("Name"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/me - should require authentication")
    void shouldRequireAuthForUpdateProfile() throws Exception {
        Map<String, String> request = Map.of("firstName", "Hacker");

        mockMvc.perform(
                put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
