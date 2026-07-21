package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.TestRateLimitConfig;
import com.iyte_yazilim.proje_pazari.TestRedisConfig;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.PasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PasswordResetTokenEntity;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestRedisConfig.class, TestRateLimitConfig.class})
class ResetPasswordIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EmailVerificationRepository emailVerificationRepository;
    @Autowired private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired private UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Forgot Password Tests ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password - should return 200 for existing user")
    void shouldReturnSuccess_WhenEmailExists() throws Exception {
        registerAndVerifyUser("forgot-existing@std.iyte.edu.tr");

        mockMvc.perform(
                        post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "email",
                                                        "forgot-existing@std.iyte.edu.tr"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName(
            "POST /api/v1/auth/forgot-password - should return 200 for non-existent email (no"
                    + " enumeration)")
    void shouldReturnSuccess_WhenEmailDoesNotExist() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("email", "nonexistent@std.iyte.edu.tr"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/auth/forgot-password - should return 400 for invalid email format")
    void shouldReturn400_WhenEmailIsInvalid() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("email", "not-an-email"))))
                .andExpect(status().isBadRequest());
    }

    // ── Reset Password Tests ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - should reset password with valid token")
    void shouldResetPassword_WhenTokenIsValid() throws Exception {
        String email = "reset-valid@std.iyte.edu.tr";
        registerAndVerifyUser(email);

        // Trigger forgot-password to create a token
        mockMvc.perform(
                post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))));

        // Retrieve token from DB
        String userId = userRepository.findByEmail(email).orElseThrow().getId();
        PasswordResetTokenEntity tokenEntity =
                passwordResetTokenRepository.findAll().stream()
                        .filter(t -> t.getUserId().equals(userId))
                        .findFirst()
                        .orElseThrow();

        // Reset password
        mockMvc.perform(
                        post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "token",
                                                        tokenEntity.getToken(),
                                                        "newPassword",
                                                        "NewSecurePassword123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Verify login with new password works
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "email",
                                                        email,
                                                        "password",
                                                        "NewSecurePassword123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists());

        // Verify login with old password fails
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "email",
                                                        email,
                                                        "password",
                                                        "SecurePassword123!"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - should fail with invalid token")
    void shouldReturn400_WhenTokenIsInvalid() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "token",
                                                        "invalid-token",
                                                        "newPassword",
                                                        "NewSecurePassword123!"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - should fail with already used token")
    void shouldReturn400_WhenTokenAlreadyUsed() throws Exception {
        String email = "reset-used@std.iyte.edu.tr";
        registerAndVerifyUser(email);

        // Trigger forgot-password
        mockMvc.perform(
                post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))));

        // Retrieve token
        String userId = userRepository.findByEmail(email).orElseThrow().getId();
        PasswordResetTokenEntity tokenEntity =
                passwordResetTokenRepository.findAll().stream()
                        .filter(t -> t.getUserId().equals(userId))
                        .findFirst()
                        .orElseThrow();

        Map<String, String> resetRequest =
                Map.of("token", tokenEntity.getToken(), "newPassword", "NewSecurePassword123!");

        // First reset — should succeed
        mockMvc.perform(
                        post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk());

        // Second reset with same token — should fail
        mockMvc.perform(
                        post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - should fail with expired token")
    void shouldReturn400_WhenTokenExpired() throws Exception {
        String email = "reset-expired@std.iyte.edu.tr";
        registerAndVerifyUser(email);

        // Trigger forgot-password
        mockMvc.perform(
                post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))));

        // Retrieve token and manually expire it
        String userId = userRepository.findByEmail(email).orElseThrow().getId();
        PasswordResetTokenEntity tokenEntity =
                passwordResetTokenRepository.findAll().stream()
                        .filter(t -> t.getUserId().equals(userId))
                        .findFirst()
                        .orElseThrow();
        tokenEntity.setExpiresAt(LocalDateTime.now().minusHours(2));
        passwordResetTokenRepository.save(tokenEntity);

        mockMvc.perform(
                        post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "token",
                                                        tokenEntity.getToken(),
                                                        "newPassword",
                                                        "NewSecurePassword123!"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/reset-password - should fail with weak password")
    void shouldReturn400_WhenPasswordIsWeak() throws Exception {
        String email = "reset-weak@std.iyte.edu.tr";
        registerAndVerifyUser(email);

        // Trigger forgot-password
        mockMvc.perform(
                post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email))));

        // Retrieve token
        String userId = userRepository.findByEmail(email).orElseThrow().getId();
        PasswordResetTokenEntity tokenEntity =
                passwordResetTokenRepository.findAll().stream()
                        .filter(t -> t.getUserId().equals(userId))
                        .findFirst()
                        .orElseThrow();

        mockMvc.perform(
                        post("/api/v1/auth/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of(
                                                        "token",
                                                        tokenEntity.getToken(),
                                                        "newPassword",
                                                        "weak"))))
                .andExpect(status().isBadRequest());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void registerAndVerifyUser(String email) throws Exception {
        Map<String, String> registerRequest =
                Map.of(
                        "email", email,
                        "password", "SecurePassword123!",
                        "firstName", "Test",
                        "lastName", "User");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        EmailVerificationEntity verification =
                emailVerificationRepository.findByEmailAndVerifiedAtIsNull(email).orElseThrow();
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);
    }
}
