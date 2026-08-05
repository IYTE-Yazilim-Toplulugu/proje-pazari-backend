package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
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
    private static final String AVATAR_CONTENT = "fake-jpeg-bytes";

    @TempDir static Path tempStorageDir;

    @DynamicPropertySource
    static void overrideStorageProperties(DynamicPropertyRegistry registry) {
        registry.add("storage.provider", () -> "local");
        registry.add("storage.local.path", () -> tempStorageDir.toString());
    }

    @Autowired private IFileStorageAdapter storageAdapter;

    /**
     * Seeded through the real adapter rather than written to disk, so the served content type comes
     * from the type recorded at store time — the path a genuine upload takes — instead of from the
     * filename extension. The temp directory is shared by every test in the class (it has to be
     * static to feed the property override), so the fixture is rewritten before each test.
     */
    @BeforeEach
    void seedFile() throws Exception {
        Files.createDirectories(tempStorageDir.resolve("profiles"));
        byte[] content = AVATAR_CONTENT.getBytes(StandardCharsets.UTF_8);
        storageAdapter.store(
                new FileUpload("avatar.jpg", "image/jpeg", content, content.length),
                "profiles/avatar.jpg");
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
                .andExpect(content().bytes(AVATAR_CONTENT.getBytes(StandardCharsets.UTF_8)))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(
                        header().string(
                                        HttpHeaders.CONTENT_LENGTH,
                                        String.valueOf(AVATAR_CONTENT.length())))
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

    @Test
    @DisplayName("Served content carries nosniff and a locked-down CSP")
    void downloadFile_localProvider_setsContentSecurityHeaders() throws Exception {
        String token =
                createVerifiedUserAndGetToken(
                        VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        mockMvc.perform(
                        get(FILES_URL + "/profiles/avatar.jpg")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(
                        header().string(
                                        "Content-Security-Policy",
                                        org.hamcrest.Matchers.containsString(
                                                "default-src 'none'")));
    }

    /**
     * The end-to-end form of the reviewed finding: an upload declaring an allowed content type
     * while carrying an active extension must not come back as renderable HTML. Goes through the
     * real upload endpoint, so it covers the stored extension as well as the served headers.
     */
    @Test
    @DisplayName(
            "Upload declaring an allowed type with an active extension is never served as HTML")
    void uploadThenDownload_mismatchedActiveExtension_isNotServedAsHtml() throws Exception {
        String token =
                createVerifiedUserAndGetToken(
                        VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
        MockMultipartFile upload =
                new MockMultipartFile(
                        "file",
                        "avatar.html",
                        "image/jpeg",
                        "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(
                        multipart(FILES_URL)
                                .file(upload)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String storedName = onlyStoredFileName(tempStorageDir.resolve("uploads"));
        assertTrue(storedName.endsWith(".jpg"), "stored under an active extension: " + storedName);

        mockMvc.perform(
                        get(FILES_URL + "/uploads/" + storedName)
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    /**
     * Files already on disk under an active extension predate the upload-side fix, so the HTTP
     * boundary must neutralize them independently.
     */
    @Test
    @DisplayName("Pre-existing file with an active extension is served as an opaque attachment")
    void downloadFile_localProvider_activeExtensionOnDisk_isNotRenderable() throws Exception {
        Files.write(
                tempStorageDir.resolve("profiles").resolve("legacy.html"),
                "<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8));

        String token =
                createVerifiedUserAndGetToken(
                        VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        mockMvc.perform(
                        get(FILES_URL + "/profiles/legacy.html")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/octet-stream"))
                .andExpect(
                        header().string(
                                        HttpHeaders.CONTENT_DISPOSITION,
                                        org.hamcrest.Matchers.startsWith("attachment")))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("Link inside the storage root pointing outside it is not downloadable")
    void downloadFile_localProvider_symlinkEscapingRoot_isRejected() throws Exception {
        Path outsideFile = tempStorageDir.getParent().resolve("outside-secret.txt");
        Files.write(outsideFile, "host secret".getBytes(StandardCharsets.UTF_8));
        try {
            Files.createSymbolicLink(tempStorageDir.resolve("public-link"), outsideFile);
        } catch (IOException | UnsupportedOperationException e) {
            org.junit.jupiter.api.Assumptions.assumeTrue(
                    false, "symbolic links unsupported here: " + e.getMessage());
        }

        String token =
                createVerifiedUserAndGetToken(
                        VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);

        // Reported as missing rather than invalid: the existence check ahead of the read applies
        // the same containment rule, and answers before the path is resolved for reading.
        mockMvc.perform(get(FILES_URL + "/public-link").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Content type metadata sidecar is not downloadable")
    void downloadFile_localProvider_metadataSidecar_returns404() throws Exception {
        String token =
                createVerifiedUserAndGetToken(
                        VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
        // Named for a file of its own, so requesting it cannot be satisfied by the shared fixture.
        Files.write(
                tempStorageDir.resolve("profiles").resolve("sidecar-probe.jpg.meta"),
                "contentType=text/html".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(
                        get(FILES_URL + "/profiles/sidecar-probe.jpg.meta")
                                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    /** Uploads use a generated name, so the test reads it back from the storage directory. */
    private String onlyStoredFileName(Path directory) throws IOException {
        try (var entries = Files.list(directory)) {
            return entries.map(path -> path.getFileName().toString())
                    .filter(name -> !name.endsWith(".meta"))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("no file stored in " + directory));
        }
    }
}
