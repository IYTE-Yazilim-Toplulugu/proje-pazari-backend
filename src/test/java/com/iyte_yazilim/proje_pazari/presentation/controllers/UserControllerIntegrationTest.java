package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

class UserControllerIntegrationTest extends IntegrationTestBase {

    private static final String BASE_URL = "/api/v1/users";
    private static final String VALID_EMAIL = "testuser@std.iyte.edu.tr";
    private static final String VALID_PASSWORD = "SecurePass123!";
    private static final String VALID_FIRST_NAME = "John";
    private static final String VALID_LAST_NAME = "Doe";

    @Autowired private UserRepository userRepository;

    @Autowired private EmailVerificationRepository emailVerificationRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private JwtUtil jwtUtil;

    // ── Helper Methods ──────────────────────────────────────────────────

    private void registerUser(String email, String password, String firstName, String lastName)
            throws Exception {
        var command = new RegisterUserCommand(email, password, firstName, lastName);
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated());
    }

    private void registerAndVerifyUser(
            String email, String password, String firstName, String lastName) throws Exception {
        registerUser(email, password, firstName, lastName);

        var user = userRepository.findByEmail(email).orElseThrow();
        var verification =
                emailVerificationRepository
                        .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                        .orElseThrow();
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        var command = new LoginUserCommand(email, password);
        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(command)))
                        .andExpect(status().isOk())
                        .andReturn();

        var jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("data").get("accessToken").asText();
    }

    private String createVerifiedUserAndGetToken() throws Exception {
        registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
        return loginAndGetToken(VALID_EMAIL, VALID_PASSWORD);
    }

    private String getUserId(String email) {
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    // ── 1. Get User Profile Tests ───────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/users/{userId}")
    class GetUserProfileTests {

        @Test
        @DisplayName("1. Get existing user profile returns 200 with profile data")
        void getUserProfile_existingUser_returns200() throws Exception {
            registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
            String userId = getUserId(VALID_EMAIL);

            mockMvc.perform(get(BASE_URL + "/" + userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(userId))
                    .andExpect(jsonPath("$.data.email").value(VALID_EMAIL))
                    .andExpect(jsonPath("$.data.firstName").value(VALID_FIRST_NAME))
                    .andExpect(jsonPath("$.data.lastName").value(VALID_LAST_NAME))
                    .andExpect(
                            jsonPath("$.data.fullName")
                                    .value(VALID_FIRST_NAME + " " + VALID_LAST_NAME))
                    .andExpect(jsonPath("$.data.projectsCreated").value(0))
                    .andExpect(jsonPath("$.data.applicationsSubmitted").value(0));
        }

        @Test
        @DisplayName("2. Get non-existent user profile returns 404 NOT_FOUND")
        void getUserProfile_nonExistentUser_returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/non-existent-user-id"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("3. Profile does not expose sensitive data (password)")
        void getUserProfile_doesNotExposeSensitiveData() throws Exception {
            registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
            String userId = getUserId(VALID_EMAIL);

            mockMvc.perform(get(BASE_URL + "/" + userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.password").doesNotExist());
        }
    }

    // ── 2. Update Profile Tests ─────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/users")
    class UpdateProfileTests {

        @Test
        @DisplayName("1. Update profile with valid data returns 200")
        void updateProfile_validData_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();

            Map<String, String> updateData = new HashMap<>();
            updateData.put("firstName", "Jane");
            updateData.put("lastName", "Smith");
            updateData.put("description", "Updated bio");

            mockMvc.perform(
                            put(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.firstName").value("Jane"))
                    .andExpect(jsonPath("$.data.lastName").value("Smith"))
                    .andExpect(jsonPath("$.data.description").value("Updated bio"));
        }

        @Test
        @DisplayName("2. Update profile without authentication returns 403")
        void updateProfile_noAuth_returns403() throws Exception {
            Map<String, String> updateData = new HashMap<>();
            updateData.put("firstName", "Jane");

            mockMvc.perform(
                            put(BASE_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("3. Update profile with valid LinkedIn URL returns 200")
        void updateProfile_validLinkedinUrl_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();

            Map<String, String> updateData = new HashMap<>();
            updateData.put("linkedinUrl", "https://www.linkedin.com/in/johndoe");

            mockMvc.perform(
                            put(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.linkedinUrl")
                                    .value("https://www.linkedin.com/in/johndoe"));
        }

        @Test
        @DisplayName("4. Update profile with invalid LinkedIn URL returns 400")
        void updateProfile_invalidLinkedinUrl_returns400() throws Exception {
            String token = createVerifiedUserAndGetToken();

            Map<String, String> updateData = new HashMap<>();
            updateData.put("linkedinUrl", "https://not-linkedin.com/profile");

            mockMvc.perform(
                            put(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("5. Update profile with valid GitHub URL returns 200")
        void updateProfile_validGithubUrl_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();

            Map<String, String> updateData = new HashMap<>();
            updateData.put("githubUrl", "https://github.com/johndoe");

            mockMvc.perform(
                            put(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.githubUrl").value("https://github.com/johndoe"));
        }

        @Test
        @DisplayName("6. Update profile with invalid GitHub URL returns 400")
        void updateProfile_invalidGithubUrl_returns400() throws Exception {
            String token = createVerifiedUserAndGetToken();

            Map<String, String> updateData = new HashMap<>();
            updateData.put("githubUrl", "https://not-github.com/user");

            mockMvc.perform(
                            put(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("7. Update persists changes in database")
        void updateProfile_changesPersisted() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String userId = getUserId(VALID_EMAIL);

            Map<String, String> updateData = new HashMap<>();
            updateData.put("firstName", "Updated");
            updateData.put("description", "New description");

            mockMvc.perform(
                            put(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(updateData)))
                    .andExpect(status().isOk());

            UserEntity updatedUser = userRepository.findById(userId).orElseThrow();
            assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
            assertThat(updatedUser.getDescription()).isEqualTo("New description");
        }
    }

    // ── 3. Change Password Tests ────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/users/me/password")
    class ChangePasswordTests {

        @Test
        @DisplayName("1. Change password with correct current password returns 200")
        void changePassword_validData_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String userId = getUserId(VALID_EMAIL);

            Map<String, String> passwordData = new HashMap<>();
            passwordData.put("userId", userId);
            passwordData.put("currentPassword", VALID_PASSWORD);
            passwordData.put("newPassword", "NewSecurePass456!");
            passwordData.put("confirmPassword", "NewSecurePass456!");

            mockMvc.perform(
                            put(BASE_URL + "/me/password")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(passwordData)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("2. Change password with incorrect current password returns 400")
        void changePassword_wrongCurrentPassword_returns400() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String userId = getUserId(VALID_EMAIL);

            Map<String, String> passwordData = new HashMap<>();
            passwordData.put("userId", userId);
            passwordData.put("currentPassword", "WrongPassword123!");
            passwordData.put("newPassword", "NewSecurePass456!");
            passwordData.put("confirmPassword", "NewSecurePass456!");

            mockMvc.perform(
                            put(BASE_URL + "/me/password")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(passwordData)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("3. Change password with mismatched confirmation returns 400")
        void changePassword_mismatchedConfirmation_returns400() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String userId = getUserId(VALID_EMAIL);

            Map<String, String> passwordData = new HashMap<>();
            passwordData.put("userId", userId);
            passwordData.put("currentPassword", VALID_PASSWORD);
            passwordData.put("newPassword", "NewSecurePass456!");
            passwordData.put("confirmPassword", "DifferentPass789!");

            mockMvc.perform(
                            put(BASE_URL + "/me/password")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(passwordData)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("4. Change password with weak new password returns 400")
        void changePassword_weakNewPassword_returns400() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String userId = getUserId(VALID_EMAIL);

            Map<String, String> passwordData = new HashMap<>();
            passwordData.put("userId", userId);
            passwordData.put("currentPassword", VALID_PASSWORD);
            passwordData.put("newPassword", "weak");
            passwordData.put("confirmPassword", "weak");

            mockMvc.perform(
                            put(BASE_URL + "/me/password")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(passwordData)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("5. Change password without authentication returns 403")
        void changePassword_noAuth_returns403() throws Exception {
            Map<String, String> passwordData = new HashMap<>();
            passwordData.put("userId", "placeholder");
            passwordData.put("currentPassword", VALID_PASSWORD);
            passwordData.put("newPassword", "NewSecurePass456!");
            passwordData.put("confirmPassword", "NewSecurePass456!");

            mockMvc.perform(
                            put(BASE_URL + "/me/password")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(passwordData)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("6. New password same as current returns 400")
        void changePassword_sameAsCurrent_returns400() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String userId = getUserId(VALID_EMAIL);

            Map<String, String> passwordData = new HashMap<>();
            passwordData.put("userId", userId);
            passwordData.put("currentPassword", VALID_PASSWORD);
            passwordData.put("newPassword", VALID_PASSWORD);
            passwordData.put("confirmPassword", VALID_PASSWORD);

            mockMvc.perform(
                            put(BASE_URL + "/me/password")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(passwordData)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("7. Can login with new password after change")
        void changePassword_canLoginWithNewPassword() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String userId = getUserId(VALID_EMAIL);
            String newPassword = "NewSecurePass456!";

            Map<String, String> passwordData = new HashMap<>();
            passwordData.put("userId", userId);
            passwordData.put("currentPassword", VALID_PASSWORD);
            passwordData.put("newPassword", newPassword);
            passwordData.put("confirmPassword", newPassword);

            mockMvc.perform(
                            put(BASE_URL + "/me/password")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(passwordData)))
                    .andExpect(status().isOk());

            // Verify login with new password works
            var loginCommand = new LoginUserCommand(VALID_EMAIL, newPassword);
            mockMvc.perform(
                            post("/api/v1/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(loginCommand)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
        }
    }

    // ── 4. Upload Profile Picture Tests ─────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/users/me/profile-picture")
    class UploadProfilePictureTests {

        @Test
        @DisplayName("1. Upload valid JPEG image returns 200")
        void uploadProfilePicture_validJpeg_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "profile.jpg", "image/jpeg", "fake-image-content".getBytes());

            mockMvc.perform(
                            multipart(BASE_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("2. Upload valid PNG image returns 200")
        void uploadProfilePicture_validPng_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "profile.png", "image/png", "fake-image-content".getBytes());

            mockMvc.perform(
                            multipart(BASE_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("3. Upload without authentication returns 403")
        void uploadProfilePicture_noAuth_returns403() throws Exception {
            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "profile.jpg", "image/jpeg", "fake-image-content".getBytes());

            mockMvc.perform(multipart(BASE_URL + "/me/profile-picture").file(file))
                    .andExpect(status().isForbidden());
        }
    }

    // ── 5. Deactivate Account Tests ─────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/v1/users/me")
    class DeactivateAccountTests {

        @Test
        @DisplayName("1. Deactivate own account returns 200")
        void deactivateAccount_authenticated_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();

            mockMvc.perform(delete(BASE_URL + "/me").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("2. Deactivate account with reason returns 200")
        void deactivateAccount_withReason_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();

            mockMvc.perform(
                            delete(BASE_URL + "/me")
                                    .header("Authorization", "Bearer " + token)
                                    .param("reason", "No longer need the account"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("3. Deactivated account is marked as inactive in database")
        void deactivateAccount_markedInactive() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String userId = getUserId(VALID_EMAIL);

            mockMvc.perform(delete(BASE_URL + "/me").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            UserEntity user = userRepository.findById(userId).orElseThrow();
            assertThat(user.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("4. Deactivate without authentication returns 403")
        void deactivateAccount_noAuth_returns403() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/me")).andExpect(status().isForbidden());
        }
    }

    // ── 6. Get All Users Tests (Admin Only) ─────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/users")
    class GetAllUsersTests {

        @Test
        @DisplayName("1. Get all users as non-admin (APPLICANT) returns 403")
        void getAllUsers_asApplicant_returns403() throws Exception {
            String token = createVerifiedUserAndGetToken();

            mockMvc.perform(get(BASE_URL).header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("2. Get all users without authentication returns 403")
        void getAllUsers_noAuth_returns403() throws Exception {
            mockMvc.perform(get(BASE_URL)).andExpect(status().isForbidden());
        }
    }
}
