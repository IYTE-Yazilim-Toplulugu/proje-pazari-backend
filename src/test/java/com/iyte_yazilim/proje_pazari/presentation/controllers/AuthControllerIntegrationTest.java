package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.TestRateLimitConfig;
import com.iyte_yazilim.proje_pazari.TestRedisConfig;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
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
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestRedisConfig.class, TestRateLimitConfig.class})
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EmailVerificationRepository emailVerificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/v1/auth/register - should register user successfully")
    void shouldRegisterUser() throws Exception {
        Map<String, String> request =
                Map.of(
                        "email", "newuser@std.iyte.edu.tr",
                        "password", "SecurePassword123!",
                        "firstName", "John",
                        "lastName", "Doe");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - should fail for duplicate email")
    void shouldFailForDuplicateEmail() throws Exception {
        Map<String, String> request =
                Map.of(
                        "email", "duplicate@std.iyte.edu.tr",
                        "password", "SecurePassword123!",
                        "firstName", "Jane",
                        "lastName", "Smith");

        // First registration
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same email - handler returns badRequest
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - should login successfully after registration")
    void shouldLoginSuccessfully() throws Exception {
        Map<String, String> registerRequest =
                Map.of(
                        "email", "logintest@std.iyte.edu.tr",
                        "password", "SecurePassword123!",
                        "firstName", "Login",
                        "lastName", "User");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        EmailVerificationEntity verification =
                emailVerificationRepository
                        .findByEmailAndVerifiedAtIsNull("logintest@std.iyte.edu.tr")
                        .orElseThrow();
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        Map<String, String> loginRequest =
                Map.of(
                        "email", "logintest@std.iyte.edu.tr",
                        "password", "SecurePassword123!");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.email").value("logintest@std.iyte.edu.tr"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - should fail with wrong password")
    void shouldFailWithWrongPassword() throws Exception {
        Map<String, String> registerRequest =
                Map.of(
                        "email", "wrongpw@std.iyte.edu.tr",
                        "password", "SecurePassword123!",
                        "firstName", "Wrong",
                        "lastName", "Password");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        EmailVerificationEntity verification =
                emailVerificationRepository
                        .findByEmailAndVerifiedAtIsNull("wrongpw@std.iyte.edu.tr")
                        .orElseThrow();
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        Map<String, String> loginRequest =
                Map.of(
                        "email", "wrongpw@std.iyte.edu.tr",
                        "password", "WrongPassword!");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - should fail with invalid data")
    void shouldFailWithInvalidData() throws Exception {
        Map<String, String> request =
                Map.of(
                        "email", "not-an-email",
                        "password", "short",
                        "firstName", "",
                        "lastName", "");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── Logout helpers ────────────────────────────────────────────────────────

    private String registerVerifyAndLogin(String email) throws Exception {
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

        MvcResult loginResult =
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
                        .andExpect(status().isOk())
                        .andReturn();

        return loginResult.getResponse().getContentAsString();
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - should logout successfully with valid tokens")
    void shouldLogoutSuccessfully() throws Exception {
        String loginJson = registerVerifyAndLogin("logout-success@std.iyte.edu.tr");

        String accessToken = objectMapper.readTree(loginJson).at("/data/accessToken").asText();
        String refreshToken = objectMapper.readTree(loginJson).at("/data/refreshToken").asText();

        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        // Confirm the blacklisted access token is rejected on subsequent requests.
        // GlobalExceptionHandler maps AccessDeniedException to 403 for anonymous users
        // (token blacklisted → no auth set → @PreAuthorize("isAuthenticated()") throws
        // AccessDeniedException → handled as 403 Forbidden).
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - should fail when refresh token is invalid")
    void shouldFailLogout_WhenRefreshTokenIsInvalid() throws Exception {
        String loginJson = registerVerifyAndLogin("logout-invalid-rt@std.iyte.edu.tr");

        String accessToken = objectMapper.readTree(loginJson).at("/data/accessToken").asText();

        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("refreshToken", "not-a-real-token"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - should fail when request body is missing")
    void shouldFailLogout_WhenBodyIsMissing() throws Exception {
        String loginJson = registerVerifyAndLogin("logout-nobody@std.iyte.edu.tr");
        String accessToken = objectMapper.readTree(loginJson).at("/data/accessToken").asText();

        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - should fail when called without authentication")
    void shouldFailLogout_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                Map.of("refreshToken", "some-token"))))
                .andExpect(status().isForbidden());
    }
}
