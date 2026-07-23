package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Regression tests for the "/{*path}" leading-slash normalization bug.
 *
 * <p>Unlike {@link FileControllerIntegrationTest}, this suite does NOT mock
 * {@link FileStorageService}. It mocks only the underlying {@link IFileStorageAdapter}, so
 * requests go through Spring's real "/{*path}" binding, the real {@link
 * com.iyte_yazilim.proje_pazari.application.queries.downloadFile.DownloadFileHandler}
 * normalization logic, and FileStorageService's real (unmodified) validatePath().
 */
class DownloadFilePathNormalizationIntegrationTest extends IntegrationTestBase {

    private static final String FILES_URL = "/api/v1/files";

    @MockitoBean private IFileStorageAdapter fileStorageAdapter;

    @Autowired private FileStorageService fileStorageService;

    @Nested
    @DisplayName("GET /api/v1/files/{*path} - leading slash normalization")
    class LeadingSlashNormalizationTests {

        @Test
        @DisplayName("1. Normal relative path with existing object redirects with 302")
        void normalPath_existingObject_returns302() throws Exception {
            String presignedUrl = "https://minio.example.com/bucket/profiles/test.jpg?signed=true";
            when(fileStorageAdapter.exists(eq("profiles/test.jpg"))).thenReturn(true);
            when(fileStorageAdapter.generatePresignedUrl(eq("profiles/test.jpg"), any(Integer.class)))
                    .thenReturn(presignedUrl);

            mockMvc.perform(get(FILES_URL + "/profiles/test.jpg"))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", presignedUrl));
        }

        @Test
        @DisplayName("2. Normal relative path with missing object returns 404")
        void normalPath_missingObject_returns404() throws Exception {
            when(fileStorageAdapter.exists(eq("profiles/missing.jpg"))).thenReturn(false);

            mockMvc.perform(get(FILES_URL + "/profiles/missing.jpg")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("3. Directory traversal (..) returns 400 without hitting storage adapter")
        void traversalPath_returns400() throws Exception {
            mockMvc.perform(get(FILES_URL + "/profiles/../../etc/passwd"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(fileStorageAdapter);
        }

        @Test
        @DisplayName("4. Double leading slash returns 400 without hitting storage adapter")
        void doubleLeadingSlash_returns400() throws Exception {
            mockMvc.perform(get(FILES_URL + "//profiles/test.jpg")).andExpect(status().isBadRequest());

            verifyNoInteractions(fileStorageAdapter);
        }

        @Test
        @DisplayName("5. Encoded leading slash returns 400 without hitting storage adapter")
        void encodedLeadingSlash_returns400() throws Exception {
            mockMvc.perform(get(FILES_URL + "/%2Fprofiles/test.jpg")).andExpect(status().isBadRequest());

            verifyNoInteractions(fileStorageAdapter);
        }

        @Test
        @DisplayName("6. Nested relative path with existing object redirects with 302")
        void nestedPath_existingObject_returns302() throws Exception {
            String presignedUrl = "https://minio.example.com/bucket/projects/p1/doc.pdf?signed=true";
            when(fileStorageAdapter.exists(eq("projects/p1/doc.pdf"))).thenReturn(true);
            when(fileStorageAdapter.generatePresignedUrl(
                    eq("projects/p1/doc.pdf"), any(Integer.class)))
                    .thenReturn(presignedUrl);

            mockMvc.perform(get(FILES_URL + "/projects/p1/doc.pdf"))
                    .andExpect(status().isFound())
                    .andExpect(header().string("Location", presignedUrl));
        }
    }

    @Nested
    @DisplayName("FileStorageService.validatePath() - unchanged behavior")
    class DirectServiceValidationTests {

        @Test
        @DisplayName("1. Direct call with absolute path still fails validation")
        void directCall_absolutePath_throws() {
            Assertions.assertThatThrownBy(() -> fileStorageService.fileExists("/profiles/test.jpg"))
                    .isInstanceOf(FileValidationException.class);
        }

        @Test
        @DisplayName("2. Direct call with traversal path still fails validation")
        void directCall_traversalPath_throws() {
            Assertions.assertThatThrownBy(() -> fileStorageService.fileExists("../etc/passwd"))
                    .isInstanceOf(FileValidationException.class);
        }
    }
}