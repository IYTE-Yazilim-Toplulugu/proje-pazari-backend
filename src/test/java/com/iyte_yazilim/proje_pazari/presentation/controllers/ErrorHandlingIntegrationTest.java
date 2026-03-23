package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ErrorHandlingIntegrationTest extends IntegrationTestBase {

    private static final String VALID_EMAIL = "errortest@std.iyte.edu.tr";
    private static final String VALID_PASSWORD = "SecurePass123!";
    private static final String VALID_FIRST_NAME = "Error";
    private static final String VALID_LAST_NAME = "Tester";

    @Autowired private UserRepository userRepository;

    @Autowired private JwtUtil jwtUtil;

    // ── Helper Methods ──────────────────────────────────────────────────

    private String createVerifiedUserAndGetToken() throws Exception {
        return createVerifiedUserAndGetToken(
                VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
    }

    private String createProjectOwnerAndGetToken() throws Exception {
        registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
        UserEntity user = userRepository.findByEmail(VALID_EMAIL).orElseThrow();
        user.setRole(RoleType.PROJECT_OWNER);
        userRepository.save(user);
        return loginAndGetToken(VALID_EMAIL, VALID_PASSWORD);
    }

    // ── 1. Response Format Verification ─────────────────────────────────

    @Nested
    @DisplayName("Error Response Format")
    class ResponseFormatTests {

        @Test
        @DisplayName("1. Error response contains code, message, and timestamp fields")
        void errorResponse_containsRequiredFields() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("2. Error response does not include data field when null")
        void errorResponse_noDataFieldWhenNull() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    new LoginUserCommand(
                                                            "nonexistent@std.iyte.edu.tr",
                                                            "SomePass123!"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("3. Success response includes data field")
        void successResponse_includesDataField() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    new RegisterUserCommand(
                                                            VALID_EMAIL,
                                                            VALID_PASSWORD,
                                                            VALID_FIRST_NAME,
                                                            VALID_LAST_NAME))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(2))
                    .andExpect(jsonPath("$.data").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    // ── 2. 400 Bad Request (Validation Errors) ─────────────────────────

    @Nested
    @DisplayName("400 Bad Request - Validation Errors")
    class BadRequestTests {

        @Test
        @DisplayName("1. Empty registration body returns 400 with VALIDATION_ERROR code")
        void register_emptyBody_returns400() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(9));
        }

        @Test
        @DisplayName("2. Invalid email format returns 400")
        void register_invalidEmail_returns400() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    new RegisterUserCommand(
                                                            "not-an-email",
                                                            VALID_PASSWORD,
                                                            VALID_FIRST_NAME,
                                                            VALID_LAST_NAME))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(9));
        }

        @Test
        @DisplayName("3. Weak password returns 400")
        void register_weakPassword_returns400() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    new RegisterUserCommand(
                                                            VALID_EMAIL,
                                                            "weak",
                                                            VALID_FIRST_NAME,
                                                            VALID_LAST_NAME))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(9));
        }

        @Test
        @DisplayName("4. Missing required project fields returns 400")
        void createProject_missingFields_returns400() throws Exception {
            String token = createProjectOwnerAndGetToken();

            mockMvc.perform(
                            post("/api/v1/projects")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(9));
        }

        @Test
        @DisplayName("5. Missing request parameter returns 400")
        void refresh_missingParam_returns400() throws Exception {
            mockMvc.perform(post("/api/v1/auth/refresh"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(9));
        }

        @Test
        @DisplayName("6. Invalid login credentials returns 400 with BAD_REQUEST code")
        void login_invalidCredentials_returns400() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    new LoginUserCommand(
                                                            "nobody@std.iyte.edu.tr",
                                                            "WrongPass123!"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(4));
        }
    }

    // ── 3. 403 Forbidden (Insufficient Permissions) ─────────────────────

    @Nested
    @DisplayName("403 Forbidden - Insufficient Permissions")
    class ForbiddenTests {

        @Test
        @DisplayName("1. Unauthenticated POST request returns 403")
        void createProject_noAuth_returns403() throws Exception {
            mockMvc.perform(
                            post("/api/v1/projects")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("2. APPLICANT creating project returns 403")
        void createProject_asApplicant_returns403() throws Exception {
            String token = createVerifiedUserAndGetToken();

            Map<String, Object> data = new HashMap<>();
            data.put("projectName", "Test Project");
            data.put("description", "A test project description that is long enough to pass.");

            mockMvc.perform(
                            post("/api/v1/projects")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(data)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("3. Unauthenticated profile update returns 403")
        void updateProfile_noAuth_returns403() throws Exception {
            mockMvc.perform(
                            put("/api/v1/users/me")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"firstName\":\"Jane\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("4. Unauthenticated password change returns 403")
        void changePassword_noAuth_returns403() throws Exception {
            mockMvc.perform(
                            put("/api/v1/users/me/password")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("5. Unauthenticated account deactivation returns 403")
        void deactivateAccount_noAuth_returns403() throws Exception {
            mockMvc.perform(delete("/api/v1/users/me")).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("6. Non-admin accessing admin endpoints returns 403")
        void adminEndpoint_asApplicant_returns403() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String userId = userRepository.findByEmail(VALID_EMAIL).orElseThrow().getId();

            mockMvc.perform(
                            post("/api/v1/admin/users/" + userId + "/promote-to-project-owner")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
    }

    // ── 4. 404 Not Found (Resource Doesn't Exist) ──────────────────────

    @Nested
    @DisplayName("404 Not Found - Resource Doesn't Exist")
    class NotFoundTests {

        @Test
        @DisplayName("1. Non-existent user profile returns 404 with NOT_FOUND code")
        void getUserProfile_nonExistent_returns404() throws Exception {
            mockMvc.perform(get("/api/v1/users/non-existent-user-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(7));
        }

        @Test
        @DisplayName("2. Non-existent user returns 404 with message")
        void getUserProfile_nonExistent_returns404WithMessage() throws Exception {
            mockMvc.perform(get("/api/v1/users/another-non-existent-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(7))
                    .andExpect(jsonPath("$.message").isNotEmpty())
                    .andExpect(jsonPath("$.data").doesNotExist());
        }
    }

    // ── 5. 409 Conflict (Duplicate Resource) ────────────────────────────

    @Nested
    @DisplayName("409 Conflict - Duplicate Resource")
    class ConflictTests {

        @Test
        @DisplayName("1. Duplicate email registration returns 400 (handled as bad request)")
        void register_duplicateEmail_returnsBadRequest() throws Exception {
            var command =
                    new RegisterUserCommand(
                            VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
            String body = objectMapper.writeValueAsString(command);

            // First registration succeeds
            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                    .andExpect(status().isCreated());

            // Second registration fails with duplicate
            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
    }

    // ── 6. 500 Internal Server Error ────────────────────────────────────

    @Nested
    @DisplayName("500 Internal Server Error - Unexpected Errors")
    class InternalServerErrorTests {

        @Test
        @DisplayName("1. Login with unverified email returns 403")
        void login_unverifiedEmail_returns403() throws Exception {
            var registerCmd =
                    new RegisterUserCommand(
                            VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(registerCmd)))
                    .andExpect(status().isCreated());

            // Login without verifying email
            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    new LoginUserCommand(
                                                            VALID_EMAIL, VALID_PASSWORD))))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value(6));
        }

        @Test
        @DisplayName("2. Invalid verification token returns 400")
        void verifyEmail_invalidToken_returns400() throws Exception {
            mockMvc.perform(
                            get("/api/v1/auth/verify-email")
                                    .param("token", "completely-invalid-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(4));
        }
    }

    // ── 7. Response Code Mapping Verification ───────────────────────────

    @Nested
    @DisplayName("Response Code Mapping")
    class ResponseCodeMappingTests {

        @Test
        @DisplayName("1. SUCCESS code is 2")
        void successCode_is0() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    new RegisterUserCommand(
                                                            VALID_EMAIL,
                                                            VALID_PASSWORD,
                                                            VALID_FIRST_NAME,
                                                            VALID_LAST_NAME))))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(2));
        }

        @Test
        @DisplayName("2. VALIDATION_ERROR code is 9")
        void validationErrorCode_is9() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/register")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(9));
        }

        @Test
        @DisplayName("3. NOT_FOUND code is 7")
        void notFoundCode_is7() throws Exception {
            mockMvc.perform(get("/api/v1/users/non-existent-id"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(7));
        }

        @Test
        @DisplayName("4. BAD_REQUEST code is 4 for invalid verification token")
        void badRequestCode_is4_forInvalidToken() throws Exception {
            mockMvc.perform(get("/api/v1/auth/verify-email").param("token", "bad-token"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(4));
        }

        @Test
        @DisplayName("5. BAD_REQUEST code is 4")
        void badRequestCode_is4() throws Exception {
            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            objectMapper.writeValueAsString(
                                                    new LoginUserCommand(
                                                            "ghost@std.iyte.edu.tr",
                                                            "NoUser123!"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(4));
        }
    }
}
