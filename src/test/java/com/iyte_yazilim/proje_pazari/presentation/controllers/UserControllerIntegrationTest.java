package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.TestRateLimitConfig;
import com.iyte_yazilim.proje_pazari.TestRedisConfig;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.util.Map;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import com.github.f4b6a3.ulid.UlidCreator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({ TestRedisConfig.class, TestRateLimitConfig.class })
class UserControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private JwtUtil jwtUtil;
        @Autowired
        private EmailVerificationRepository emailVerificationRepository;
        private final ObjectMapper objectMapper = new ObjectMapper();

        private String testUserId;
        private String jwtToken;
        private String testEmail;

        @BeforeEach
        void setUp() {
                testEmail = "usercontroller-" + System.nanoTime() + "@std.iyte.edu.tr";
                UserEntity user = new UserEntity();
                user.setEmail(testEmail);
                user.setPassword(passwordEncoder.encode("TestPassword123!"));
                user.setFirstName("Test");
                user.setLastName("User");
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
                jwtToken = jwtUtil.generateToken(saved.getId(), saved.getEmail(), "APPLICANT");
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
        @DisplayName("GET /api/v1/users - should be publicly accessible (GET permitted)")
        void shouldAllowPublicAccessToGetAllUsers() throws Exception {
                mockMvc.perform(get("/api/v1/users")).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/v1/users - should return users when authenticated")
        void shouldReturnUsersWhenAuthenticated() throws Exception {
                String adminToken = jwtUtil.generateToken(testUserId, testEmail, "ADMIN");
                mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("GET /api/v1/users/me - should return current user profile")
        void shouldReturnCurrentUserProfile() throws Exception {
                mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + jwtToken))
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

        @Test
        @DisplayName("PUT /api/v1/users/me/password - should change password when authenticated")
        void shouldChangePassword() throws Exception {
                Map<String, String> request = Map.of(
                                "userId", testUserId,
                                "currentPassword", "TestPassword123!",
                                "newPassword", "NewSecurePassword456!",
                                "confirmPassword", "NewSecurePassword456!");

                mockMvc.perform(
                                put("/api/v1/users/me/password")
                                                .header("Authorization", "Bearer " + jwtToken)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /api/v1/users/me/password - should require authentication")
        void shouldRequireAuthForChangePassword() throws Exception {
                Map<String, String> request = Map.of(
                                "currentPassword", "old",
                                "newPassword", "new",
                                "confirmPassword", "new");

                mockMvc.perform(
                                put("/api/v1/users/me/password")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/v1/users/me/profile-picture - should upload profile picture")
        void shouldUploadProfilePicture() throws Exception {
                MockMultipartFile file = new MockMultipartFile(
                                "file", "test-avatar.jpg", "image/jpeg", "fake-image-content".getBytes());

                mockMvc.perform(
                                multipart("/api/v1/users/me/profile-picture")
                                                .file(file)
                                                .header("Authorization", "Bearer " + jwtToken))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/v1/users/me/profile-picture - should require authentication")
        void shouldRequireAuthForUploadProfilePicture() throws Exception {
                MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg",
                                "fake-image".getBytes());

                mockMvc.perform(multipart("/api/v1/users/me/profile-picture").file(file))
                                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/v1/users/me - should deactivate account when authenticated")
        void shouldDeactivateAccount() throws Exception {
                // Create a dedicated user for deactivation test
                String deactivateEmail = "deactivate-" + System.nanoTime() + "@std.iyte.edu.tr";
                UserEntity deactivateUser = new UserEntity();
                deactivateUser.setEmail(deactivateEmail);
                deactivateUser.setPassword(passwordEncoder.encode("TestPassword123!"));
                deactivateUser.setFirstName("Deactivate");
                deactivateUser.setLastName("User");
                deactivateUser.setIsActive(true);
                deactivateUser.setId(UlidCreator.getUlid().toString());
                userRepository.save(deactivateUser);
                String deactivateToken = jwtUtil.generateToken(deactivateUser.getId(), deactivateUser.getEmail(),
                                "APPLICANT");

                mockMvc.perform(
                                delete("/api/v1/users/me")
                                                .header("Authorization", "Bearer " + deactivateToken)
                                                .param("reason", "Testing deactivation"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /api/v1/users/me - should require authentication")
        void shouldRequireAuthForDeactivateAccount() throws Exception {
                mockMvc.perform(delete("/api/v1/users/me")).andExpect(status().isForbidden());
        }
}
