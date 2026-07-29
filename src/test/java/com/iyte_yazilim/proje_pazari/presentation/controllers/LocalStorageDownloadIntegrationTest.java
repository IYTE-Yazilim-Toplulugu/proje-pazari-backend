package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Verifies GET /api/v1/files/{path} against a real LocalStorageAdapter (storage.provider=local),
 * with no mocked FileStorageService. Regression coverage for the infinite-redirect bug: local
 * downloads must return 200 with inline content, never a 302 pointing back at the same API path.
 */
class LocalStorageDownloadIntegrationTest extends IntegrationTestBase {

    private static final String FILES_URL = "/api/v1/files";
    private static final String VALID_EMAIL = "localfiletest@std.iyte.edu.tr";
    private static final String VALID_PASSWORD = "SecurePass123!";
    private static final String VALID_FIRST_NAME = "Local";
    private static final String VALID_LAST_NAME = "Tester";

    @TempDir static Path tempStorageDir;

    @DynamicPropertySource
    static void overrideStorageProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.provider", () -> "local");
        registry.add("storage.local.path", () -> tempStorageDir.toString());
    }

    @BeforeEach
    void seedFile() throws Exception {
        Path profilesDir = tempStorageDir.resolve("profiles");
        Files.createDirectories(profilesDir);
        Files.write(
                profilesDir.resolve("avatar.jpg"),
                "fake-jpeg-bytes".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Download existing local file returns 200 with inline content, not a redirect")
    void downloadFile_localProvider_returnsInlineContent() throws Exception {
        String token =
                createVerifiedUserAndGetToken(
                        VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        mockMvc.perform(
                        get(FILES_URL + "/profiles/avatar.jpg")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().bytes("fake-jpeg-bytes".getBytes(StandardCharsets.UTF_8)))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(
                        header().string(
                                        HttpHeaders.CONTENT_DISPOSITION,
                                        "inline; filename=\"avatar.jpg\""));
    }

    @Test
    @DisplayName("Non-ASCII filename is RFC 5987 encoded instead of mangled")
    void downloadFile_localProvider_nonAsciiFilename_isEncoded() throws Exception {
        Files.write(
                tempStorageDir.resolve("profiles").resolve("özgeçmiş.pdf"),
                "cv-bytes".getBytes(StandardCharsets.UTF_8));

        String token =
                createVerifiedUserAndGetToken(
                        VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        mockMvc.perform(
                        get(FILES_URL + "/profiles/özgeçmiş.pdf")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                        HttpHeaders.CONTENT_DISPOSITION,
                                        org.hamcrest.Matchers.containsString("filename*=UTF-8''")))
                .andExpect(
                        header().string(
                                        HttpHeaders.CONTENT_DISPOSITION,
                                        org.hamcrest.Matchers.containsString("%C3%B6zge")));
    }

    @Test
    @DisplayName("Download missing local file returns 404")
    void downloadFile_localProvider_missingFile_returns404() throws Exception {
        String token =
                createVerifiedUserAndGetToken(
                        VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        mockMvc.perform(
                        get(FILES_URL + "/profiles/does-not-exist.jpg")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Download with directory traversal returns 400 for local provider")
    void downloadFile_localProvider_traversal_returns400() throws Exception {
        String token =
                createVerifiedUserAndGetToken(
                        VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        mockMvc.perform(
                        get(FILES_URL + "/../../etc/passwd")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
