package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.resendVerificationEmail.ResendVerificationEmailCommand;
import com.iyte_yazilim.proje_pazari.application.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.RefreshTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.security.service.RefreshTokenService;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

class AuthControllerIntegrationTest extends IntegrationTestBase {

    private static final String BASE_URL = "/api/v1/auth";
    private static final String VALID_EMAIL = "testuser@std.iyte.edu.tr";
    private static final String VALID_PASSWORD = "SecurePass123!";
    private static final String VALID_FIRST_NAME = "John";
    private static final String VALID_LAST_NAME = "Doe";

    @Autowired private UserRepository userRepository;

    @Autowired private EmailVerificationRepository emailVerificationRepository;

    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private VerificationTokenService verificationTokenService;

    @Autowired private RefreshTokenService refreshTokenService;

    @Autowired private JwtUtil jwtUtil;

    // ── Helper Methods ──────────────────────────────────────────────────

    private ResultActions registerUser(
            String email, String password, String firstName, String lastName) throws Exception {
        var command = new RegisterUserCommand(email, password, firstName, lastName);
        return mockMvc.perform(
                post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)));
    }

    private void registerAndVerifyUser(
            String email, String password, String firstName, String lastName) throws Exception {
        registerUser(email, password, firstName, lastName).andExpect(status().isCreated());

        // Manually verify the email in the DB
        var user = userRepository.findByEmail(email).orElseThrow();
        var verification =
                emailVerificationRepository
                        .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                        .orElseThrow();
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);
    }

    private ResultActions loginUser(String email, String password) throws Exception {
        var command = new LoginUserCommand(email, password);
        return mockMvc.perform(
                post(BASE_URL + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)));
    }

    // ── 1. Register Tests ───────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("1. Valid IYTE email and strong password returns 201 CREATED")
        void register_validData_returns201() throws Exception {
            registerUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.userId").isNotEmpty())
                    .andExpect(jsonPath("$.data.email").value(VALID_EMAIL))
                    .andExpect(jsonPath("$.data.firstName").value(VALID_FIRST_NAME))
                    .andExpect(jsonPath("$.data.lastName").value(VALID_LAST_NAME));
        }

        @Test
        @DisplayName("2. Duplicate email returns 400 BAD_REQUEST")
        void register_duplicateEmail_returns400() throws Exception {
            registerUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isCreated());

            registerUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("3. Non-IYTE email returns 400 BAD_REQUEST")
        void register_nonIyteEmail_returns400() throws Exception {
            registerUser("test@gmail.com", VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("4. Weak password (no special char) returns 400 BAD_REQUEST")
        void register_weakPassword_returns400() throws Exception {
            registerUser(VALID_EMAIL, "WeakPass1", VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("5. Missing required fields (empty body) returns 400 BAD_REQUEST")
        void register_emptyBody_returns400() throws Exception {
            mockMvc.perform(
                            post(BASE_URL + "/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("6. Email verification record is created after registration")
        void register_createsEmailVerificationRecord() throws Exception {
            registerUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isCreated());

            var user = userRepository.findByEmail(VALID_EMAIL).orElseThrow();
            var verification =
                    emailVerificationRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId());

            assertThat(verification).isPresent();
            assertThat(verification.get().getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(verification.get().getToken()).isNotBlank();
            assertThat(verification.get().getVerifiedAt()).isNull();
        }
    }

    // ── 2. Login Tests ──────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginTests {

        @Test
        @DisplayName("1. Valid credentials (verified user) returns 200 with tokens")
        void login_validCredentials_returns200() throws Exception {
            registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

            loginUser(VALID_EMAIL, VALID_PASSWORD)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("2. Non-existent email returns 400 BAD_REQUEST")
        void login_nonExistentEmail_returns400() throws Exception {
            loginUser("nonexistent@std.iyte.edu.tr", VALID_PASSWORD)
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("3. Wrong password returns 400 BAD_REQUEST")
        void login_wrongPassword_returns400() throws Exception {
            registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

            loginUser(VALID_EMAIL, "WrongPass123!").andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("4. Unverified email returns 500 INTERNAL_SERVER_ERROR")
        void login_unverifiedEmail_returns500() throws Exception {
            registerUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isCreated());

            loginUser(VALID_EMAIL, VALID_PASSWORD).andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("5. JWT contains correct claims (userId, email, role)")
        void login_jwtContainsCorrectClaims() throws Exception {
            registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

            var result =
                    loginUser(VALID_EMAIL, VALID_PASSWORD).andExpect(status().isOk()).andReturn();

            var responseBody = result.getResponse().getContentAsString();
            var jsonNode = objectMapper.readTree(responseBody);
            var accessToken = jsonNode.get("data").get("accessToken").asText();

            assertThat(jwtUtil.extractEmail(accessToken)).isEqualTo(VALID_EMAIL);
            assertThat(jwtUtil.extractUserId(accessToken)).isNotBlank();
            assertThat(jwtUtil.extractRole(accessToken)).isEqualTo("APPLICANT");
        }
    }

    // ── 3. Verify Email Tests ───────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/auth/verify-email")
    class VerifyEmailTests {

        @Test
        @DisplayName("1. Valid token verifies email successfully")
        void verifyEmail_validToken_returns200() throws Exception {
            registerUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isCreated());

            var user = userRepository.findByEmail(VALID_EMAIL).orElseThrow();
            var verification =
                    emailVerificationRepository
                            .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                            .orElseThrow();

            mockMvc.perform(get(BASE_URL + "/verify-email").param("token", verification.getToken()))
                    .andExpect(status().isOk());

            // Assert email is now verified
            assertThat(
                            emailVerificationRepository.existsByUserIdAndVerifiedAtIsNotNull(
                                    user.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("2. Invalid token returns 500 INTERNAL_SERVER_ERROR")
        void verifyEmail_invalidToken_returns500() throws Exception {
            mockMvc.perform(
                            get(BASE_URL + "/verify-email")
                                    .param("token", "invalid-token-that-does-not-exist"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("3. Already verified email returns 500 INTERNAL_SERVER_ERROR")
        void verifyEmail_alreadyVerified_returns500() throws Exception {
            registerUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isCreated());

            var user = userRepository.findByEmail(VALID_EMAIL).orElseThrow();
            var verification =
                    emailVerificationRepository
                            .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                            .orElseThrow();
            String token = verification.getToken();

            // Verify once
            mockMvc.perform(get(BASE_URL + "/verify-email").param("token", token))
                    .andExpect(status().isOk());

            // Try to verify again
            mockMvc.perform(get(BASE_URL + "/verify-email").param("token", token))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("4. Expired token returns 500 INTERNAL_SERVER_ERROR")
        void verifyEmail_expiredToken_returns500() throws Exception {
            registerUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isCreated());

            var user = userRepository.findByEmail(VALID_EMAIL).orElseThrow();
            var verification =
                    emailVerificationRepository
                            .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                            .orElseThrow();

            // Set expiration to the past
            verification.setExpiresAt(LocalDateTime.now().minusHours(1));
            emailVerificationRepository.save(verification);

            mockMvc.perform(get(BASE_URL + "/verify-email").param("token", verification.getToken()))
                    .andExpect(status().isInternalServerError());
        }
    }

    // ── 4. Resend Verification Tests ────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/auth/resend-verification")
    class ResendVerificationTests {

        @Test
        @DisplayName("1. Valid unverified email returns 200 OK")
        void resendVerification_validUnverifiedEmail_returns200() throws Exception {
            registerUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME)
                    .andExpect(status().isCreated());

            var command = new ResendVerificationEmailCommand(VALID_EMAIL);
            mockMvc.perform(
                            post(BASE_URL + "/resend-verification")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("2. Already verified email returns 500 INTERNAL_SERVER_ERROR")
        void resendVerification_alreadyVerified_returns500() throws Exception {
            registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

            var command = new ResendVerificationEmailCommand(VALID_EMAIL);
            mockMvc.perform(
                            post(BASE_URL + "/resend-verification")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("3. Non-existent email returns 500 INTERNAL_SERVER_ERROR")
        void resendVerification_nonExistentEmail_returns500() throws Exception {
            var command = new ResendVerificationEmailCommand("nonexistent@std.iyte.edu.tr");
            mockMvc.perform(
                            post(BASE_URL + "/resend-verification")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(command)))
                    .andExpect(status().isInternalServerError());
        }
    }

    // ── 5. Refresh Token Tests ──────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class RefreshTokenTests {

        @Test
        @DisplayName("1. Valid refresh token returns 200 with new tokens")
        void refresh_validToken_returns200() throws Exception {
            registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

            var user = userRepository.findByEmail(VALID_EMAIL).orElseThrow();
            String refreshToken = refreshTokenService.createRefreshToken(user.getId());

            mockMvc.perform(post(BASE_URL + "/refresh").param("refreshToken", refreshToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
        }

        @Test
        @DisplayName("2. Invalid refresh token returns 400 BAD_REQUEST")
        void refresh_invalidToken_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL + "/refresh").param("refreshToken", "invalid-token"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("3. Missing refreshToken param returns 400 BAD_REQUEST")
        void refresh_missingParam_returns400() throws Exception {
            mockMvc.perform(post(BASE_URL + "/refresh")).andExpect(status().isBadRequest());
        }
    }
}
