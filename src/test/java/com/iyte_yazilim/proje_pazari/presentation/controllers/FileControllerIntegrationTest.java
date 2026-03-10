package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

class FileControllerIntegrationTest extends IntegrationTestBase {

    private static final String FILES_URL = "/api/v1/files";
    private static final String USERS_URL = "/api/v1/users";
    private static final String VALID_EMAIL = "filetest@std.iyte.edu.tr";
    private static final String VALID_PASSWORD = "SecurePass123!";
    private static final String VALID_FIRST_NAME = "File";
    private static final String VALID_LAST_NAME = "Tester";

    @Autowired private UserRepository userRepository;

    @Autowired private EmailVerificationRepository emailVerificationRepository;

    @Autowired private JwtUtil jwtUtil;

    @MockitoBean private FileStorageService fileStorageService;

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

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder post(
            String url) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url);
    }

    // ── 1. File Download Tests ──────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/files/{path} - Download File")
    class DownloadFileTests {

        @Test
        @DisplayName("1. Download existing file redirects to presigned URL")
        void downloadFile_existingFile_redirects() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String presignedUrl =
                    "https://minio.example.com/bucket/profiles/test.jpg?signed=true";
            when(fileStorageService.fileExists(anyString())).thenReturn(true);
            when(fileStorageService.getFileUrl(anyString(), any(Integer.class)))
                    .thenReturn(presignedUrl);

            mockMvc.perform(
                            get(FILES_URL + "/profiles/test.jpg")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION, presignedUrl));
        }

        @Test
        @DisplayName("2. Download non-existent file returns 404")
        void downloadFile_nonExistent_returns404() throws Exception {
            String token = createVerifiedUserAndGetToken();
            when(fileStorageService.fileExists(anyString())).thenReturn(false);

            mockMvc.perform(
                            get(FILES_URL + "/profiles/nonexistent.jpg")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("File not found"));
        }

        @Test
        @DisplayName("3. Download with directory traversal returns 400")
        void downloadFile_directoryTraversal_returns400() throws Exception {
            String token = createVerifiedUserAndGetToken();

            mockMvc.perform(
                            get(FILES_URL + "/../../etc/passwd")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("4. Download without authentication returns 403")
        void downloadFile_noAuth_returns403() throws Exception {
            mockMvc.perform(get(FILES_URL + "/profiles/photo.png"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("5. Download PDF file redirects correctly")
        void downloadFile_pdf_redirects() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String presignedUrl =
                    "https://minio.example.com/bucket/docs/report.pdf?signed=true";
            when(fileStorageService.fileExists(anyString())).thenReturn(true);
            when(fileStorageService.getFileUrl(anyString(), any(Integer.class)))
                    .thenReturn(presignedUrl);

            mockMvc.perform(
                            get(FILES_URL + "/docs/report.pdf")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION, presignedUrl));
        }

        @Test
        @DisplayName("6. Download image file redirects correctly")
        void downloadFile_image_redirects() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String presignedUrl =
                    "https://minio.example.com/bucket/profiles/avatar.png?signed=true";
            when(fileStorageService.fileExists(anyString())).thenReturn(true);
            when(fileStorageService.getFileUrl(anyString(), any(Integer.class)))
                    .thenReturn(presignedUrl);

            mockMvc.perform(
                            get(FILES_URL + "/profiles/avatar.png")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isFound())
                    .andExpect(header().string(HttpHeaders.LOCATION, presignedUrl));
        }
    }

    // ── 2. File Path Organization Tests ─────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/files/{path} - File Path Organization")
    class FilePathOrganizationTests {

        @Test
        @DisplayName("1. User-scoped file path (profiles/) resolves correctly")
        void downloadFile_userScopedPath_resolves() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String presignedUrl =
                    "https://minio.example.com/bucket/profiles/ulid123.jpg?s=true";
            when(fileStorageService.fileExists(anyString())).thenReturn(true);
            when(fileStorageService.getFileUrl(anyString(), any(Integer.class)))
                    .thenReturn(presignedUrl);

            mockMvc.perform(
                            get(FILES_URL + "/profiles/ulid123.jpg")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isFound());
        }

        @Test
        @DisplayName("2. Project-scoped file path (projects/) resolves correctly")
        void downloadFile_projectScopedPath_resolves() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String presignedUrl =
                    "https://minio.example.com/bucket/projects/proj1/doc.pdf?s=true";
            when(fileStorageService.fileExists(anyString())).thenReturn(true);
            when(fileStorageService.getFileUrl(anyString(), any(Integer.class)))
                    .thenReturn(presignedUrl);

            mockMvc.perform(
                            get(FILES_URL + "/projects/proj1/doc.pdf")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isFound());
        }

        @Test
        @DisplayName("3. Nested directory path resolves correctly")
        void downloadFile_nestedPath_resolves() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String presignedUrl =
                    "https://minio.example.com/bucket/projects/user1/proj2/att.pdf?s=true";
            when(fileStorageService.fileExists(anyString())).thenReturn(true);
            when(fileStorageService.getFileUrl(anyString(), any(Integer.class)))
                    .thenReturn(presignedUrl);

            mockMvc.perform(
                            get(FILES_URL + "/projects/user1/proj2/att.pdf")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isFound());
        }
    }

    // ── 3. Profile Picture Upload Tests (via UserController) ────────────

    @Nested
    @DisplayName("POST /api/v1/users/me/profile-picture - Upload Integration")
    class UploadProfilePictureIntegrationTests {

        @Test
        @DisplayName("1. Upload valid JPEG image returns 200 with URL")
        void uploadProfilePicture_validJpeg_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String expectedUrl = "/api/v1/files/profiles/ulid123.jpg";
            when(fileStorageService.storeFile(any(), anyString())).thenReturn(expectedUrl);

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "photo.jpg", "image/jpeg", "fake-image-data".getBytes());

            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isNotEmpty());
        }

        @Test
        @DisplayName("2. Upload valid PNG image returns 200")
        void uploadProfilePicture_validPng_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();
            when(fileStorageService.storeFile(any(), anyString()))
                    .thenReturn("/api/v1/files/profiles/ulid123.png");

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "photo.png", "image/png", "fake-png-data".getBytes());

            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("3. Upload valid GIF image returns 200")
        void uploadProfilePicture_validGif_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();
            when(fileStorageService.storeFile(any(), anyString()))
                    .thenReturn("/api/v1/files/profiles/ulid123.gif");

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "animated.gif", "image/gif", "fake-gif-data".getBytes());

            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("4. Upload valid WebP image returns 200")
        void uploadProfilePicture_validWebp_returns200() throws Exception {
            String token = createVerifiedUserAndGetToken();
            when(fileStorageService.storeFile(any(), anyString()))
                    .thenReturn("/api/v1/files/profiles/ulid123.webp");

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "photo.webp", "image/webp", "fake-webp-data".getBytes());

            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("5. Upload without authentication returns 403")
        void uploadProfilePicture_noAuth_returns403() throws Exception {
            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "photo.jpg", "image/jpeg", "fake-image-data".getBytes());

            mockMvc.perform(multipart(USERS_URL + "/me/profile-picture").file(file))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("6. Upload executable file (.exe) is rejected")
        void uploadProfilePicture_executableFile_isRejected() throws Exception {
            String token = createVerifiedUserAndGetToken();
            when(fileStorageService.storeFile(any(), anyString()))
                    .thenThrow(new FileStorageException("File type not allowed"));

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "malware.exe",
                            "application/x-msdownload",
                            "fake-exe-content".getBytes());

            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("7. Upload shell script (.sh) is rejected")
        void uploadProfilePicture_shellScript_isRejected() throws Exception {
            String token = createVerifiedUserAndGetToken();
            when(fileStorageService.storeFile(any(), anyString()))
                    .thenThrow(new FileStorageException("File type not allowed"));

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "script.sh",
                            "application/x-sh",
                            "#!/bin/bash\necho hello".getBytes());

            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("8. Upload non-image file (ZIP) is rejected")
        void uploadProfilePicture_zipFile_isRejected() throws Exception {
            String token = createVerifiedUserAndGetToken();
            when(fileStorageService.storeFile(any(), anyString()))
                    .thenThrow(new FileStorageException("File type not allowed"));

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file",
                            "archive.zip",
                            "application/zip",
                            "fake-zip-content".getBytes());

            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("9. Upload oversized file is rejected")
        void uploadProfilePicture_oversizedFile_isRejected() throws Exception {
            String token = createVerifiedUserAndGetToken();
            when(fileStorageService.storeFile(any(), anyString()))
                    .thenThrow(
                            new FileStorageException(
                                    "File size exceeds the maximum allowed size"));

            byte[] oversizedContent = new byte[1024];
            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "large-photo.jpg", "image/jpeg", oversizedContent);

            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("10. Upload empty file is rejected")
        void uploadProfilePicture_emptyFile_isRejected() throws Exception {
            String token = createVerifiedUserAndGetToken();
            when(fileStorageService.storeFile(any(), anyString()))
                    .thenThrow(new FileStorageException("File is empty"));

            MockMultipartFile file =
                    new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("11. Upload replaces old profile picture URL in database")
        void uploadProfilePicture_replacesOldUrl() throws Exception {
            String token = createVerifiedUserAndGetToken();
            String firstUrl = "/api/v1/files/profiles/first.jpg";
            String secondUrl = "/api/v1/files/profiles/second.jpg";

            when(fileStorageService.storeFile(any(), anyString()))
                    .thenReturn(firstUrl)
                    .thenReturn(secondUrl);

            MockMultipartFile file1 =
                    new MockMultipartFile(
                            "file", "first.jpg", "image/jpeg", "first-image".getBytes());
            MockMultipartFile file2 =
                    new MockMultipartFile(
                            "file", "second.jpg", "image/jpeg", "second-image".getBytes());

            // First upload
            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file1)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            // Second upload replaces the first
            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file2)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(secondUrl));
        }

        @Test
        @DisplayName("12. Upload stores file in profiles folder")
        void uploadProfilePicture_storesInProfilesFolder() throws Exception {
            String token = createVerifiedUserAndGetToken();
            when(fileStorageService.storeFile(any(), anyString()))
                    .thenReturn("/api/v1/files/profiles/ulid.jpg");

            MockMultipartFile file =
                    new MockMultipartFile(
                            "file", "photo.jpg", "image/jpeg", "image-data".getBytes());

            mockMvc.perform(
                            multipart(USERS_URL + "/me/profile-picture")
                                    .file(file)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            verify(fileStorageService)
                    .storeFile(any(), org.mockito.ArgumentMatchers.eq("profiles"));
        }
    }
}
