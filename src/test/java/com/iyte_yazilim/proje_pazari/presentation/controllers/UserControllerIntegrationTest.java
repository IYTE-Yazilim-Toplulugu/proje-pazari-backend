package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.f4b6a3.ulid.UlidCreator;
import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserControllerIntegrationTest extends IntegrationTestBase {

    private static final String BASE_URL = "/api/v1/users";
    private static final String VALID_EMAIL = "testuser@std.iyte.edu.tr";
    private static final String VALID_PASSWORD = "SecurePass123!";
    private static final String VALID_FIRST_NAME = "John";
    private static final String VALID_LAST_NAME = "Doe";

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private JwtUtil jwtUtil;

    private String testUserId;
    private String jwtToken;
    private String testEmail;

    // ── Helper Methods ──────────────────────────────────────────────────

    private String createVerifiedUserAndGetToken() throws Exception {
        return createVerifiedUserAndGetToken(
                VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
    }

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
        jwtToken = jwtUtil.generateToken(saved.getId(), saved.getEmail(), "USER");
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
    @DisplayName("PUT /api/v1/users/me")
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
                            put(BASE_URL + "/me")
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
                            put(BASE_URL + "/me")
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
                            put(BASE_URL + "/me")
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
                            put(BASE_URL + "/me")
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
                            put(BASE_URL + "/me")
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
                            put(BASE_URL + "/me")
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
                            put(BASE_URL + "/me")
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

        @Test
        @DisplayName("4. Upload executable file (.exe) is rejected")
        void uploadProfilePicture_executableFile_isRejected() throws Exception {
            String token = createVerifiedUserAndGetToken();

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "malware.exe",
                            "application/x-msdownload",
                            "fake-exe-content".getBytes());

            mockMvc.perform(
                            multipart(BASE_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("5. Upload shell script (.sh) is rejected")
        void uploadProfilePicture_shellScript_isRejected() throws Exception {
            String token = createVerifiedUserAndGetToken();

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "script.sh",
                            "application/x-sh",
                            "#!/bin/bash\necho hello".getBytes());

            mockMvc.perform(
                            multipart(BASE_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("6. Upload non-image file (ZIP) is rejected")
        void uploadProfilePicture_zipFile_isRejected() throws Exception {
            String token = createVerifiedUserAndGetToken();

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "archive.zip",
                            "application/zip",
                            "fake-zip-content".getBytes());

            mockMvc.perform(
                            multipart(BASE_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("7. Upload valid GIF image returns 200")
        void uploadProfilePicture_validGif_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "animated.gif", "image/gif", "fake-gif-content".getBytes());

            mockMvc.perform(
                            multipart(BASE_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("8. Upload valid WebP image returns 200")
        void uploadProfilePicture_validWebp_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "photo.webp", "image/webp", "fake-webp-content".getBytes());

            mockMvc.perform(
                            multipart(BASE_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
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

    @Test
    @DisplayName("PUT /api/v1/users/me/password - should require authentication")
    void shouldRequireAuthForChangePassword() throws Exception {
        Map<String, String> request =
                Map.of(
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
        MockMultipartFile file =
                new MockMultipartFile(
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
        MockMultipartFile file =
                new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake-image".getBytes());

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
        String deactivateToken =
                jwtUtil.generateToken(deactivateUser.getId(), deactivateUser.getEmail(), "USER");

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
