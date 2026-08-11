package com.iyte_yazilim.proje_pazari.application.queries.downloadFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.models.StorageDownloadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link DownloadFileHandler}'s own path normalization/validation logic.
 *
 * <p>These call {@code handle()} directly, bypassing MockMvc and the servlet container entirely.
 * This matters because several malformed-path payloads (raw "..", "%2F", "//") are intercepted
 * upstream by Spring Security's default {@code StrictHttpFirewall} before a request ever reaches
 * this handler — an HTTP-level integration test asserting only on status code would pass
 * identically whether or not this handler's validation logic exists at all. These tests exercise
 * the handler's decode/normalize/validate logic in isolation so they actually fail if that logic is
 * weakened or removed.
 */
class DownloadFileHandlerTest {

    @Mock private FileStorageService fileStorageService;

    private DownloadFileHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new DownloadFileHandler(fileStorageService);
    }

    @Test
    @DisplayName("1. Leading slash from {*path} binding is stripped, valid relative path checked")
    void singleLeadingSlash_isStrippedAndPassedThrough() {
        StorageDownloadResult redirect =
                new StorageDownloadResult.RedirectResult("https://example.com/signed");
        org.mockito.Mockito.when(fileStorageService.fileExists("profiles/test.jpg"))
                .thenReturn(true);
        org.mockito.Mockito.when(fileStorageService.getDownloadResult("profiles/test.jpg", 60))
                .thenReturn(redirect);

        ApiResponse<StorageDownloadResult> response =
                handler.handle(new DownloadFileQuery("/profiles/test.jpg"));

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS);
        assertThat(response.getData()).isEqualTo(redirect);
    }

    @Test
    @DisplayName("1b. Local provider result is carried through as inline content, not a URL")
    void localProvider_returnsInlineResult() {
        byte[] bytes = "local-bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        StorageDownloadResult inline =
                new StorageDownloadResult.InlineResult(bytes, "image/jpeg", "test.jpg");
        org.mockito.Mockito.when(fileStorageService.fileExists("profiles/test.jpg"))
                .thenReturn(true);
        org.mockito.Mockito.when(fileStorageService.getDownloadResult("profiles/test.jpg", 60))
                .thenReturn(inline);

        ApiResponse<StorageDownloadResult> response =
                handler.handle(new DownloadFileQuery("/profiles/test.jpg"));

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS);
        assertThat(response.getData()).isInstanceOf(StorageDownloadResult.InlineResult.class);
    }

    @Test
    @DisplayName("2. Raw traversal sequence is rejected (exercises the defense-in-depth check)")
    void rawTraversal_isRejected() {
        // This is the one test in the suite where the handler's raw "rawPath.contains('..')"
        // guard is actually load-bearing: calling handle() directly bypasses
        // StrictHttpFirewall entirely, so this is the non-HTTP-entry-point scenario that guard
        // exists for. Over HTTP this exact payload never reaches the handler (verified locally).
        ApiResponse<StorageDownloadResult> response =
                handler.handle(new DownloadFileQuery("/profiles/../../etc/passwd"));

        assertThat(response.getCode()).isEqualTo(ResponseCode.BAD_REQUEST);
        assertThat(response.getMessage()).isEqualTo("Invalid file path");
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("3. Double leading slash (extra slash beyond the routing artifact) is rejected")
    void doubleLeadingSlash_isRejected() {
        ApiResponse<StorageDownloadResult> response =
                handler.handle(new DownloadFileQuery("//profiles/test.jpg"));

        assertThat(response.getCode()).isEqualTo(ResponseCode.BAD_REQUEST);
        assertThat(response.getMessage()).isEqualTo("Invalid file path");
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("4. Single-encoded leading slash (%2Fprofiles/test.jpg) is rejected after decode")
    void singleEncodedLeadingSlash_isRejected() {
        // After the mandatory /{*path} leading slash is stripped, decoding "%2Fprofiles/test.jpg"
        // yields "/profiles/test.jpg" — a second, real leading slash — which must be rejected.
        ApiResponse<StorageDownloadResult> response =
                handler.handle(new DownloadFileQuery("/%2Fprofiles/test.jpg"));

        assertThat(response.getCode()).isEqualTo(ResponseCode.BAD_REQUEST);
        assertThat(response.getMessage()).isEqualTo("Invalid file path");
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("5. Double-URL-encoded traversal (%252e%252e%252f) is fully decoded and rejected")
    void doubleEncodedTraversal_isRejected() {
        ApiResponse<StorageDownloadResult> response =
                handler.handle(new DownloadFileQuery("/profiles/%252e%252e%252fetc/passwd"));

        assertThat(response.getCode()).isEqualTo(ResponseCode.BAD_REQUEST);
        assertThat(response.getMessage()).isIn("Invalid file path", "Invalid file path encoding");
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("5b. Nested double-URL-encoded traversal is also rejected")
    void nestedDoubleEncodedTraversal_isRejected() {
        ApiResponse<StorageDownloadResult> response =
                handler.handle(new DownloadFileQuery("/%252e%252e%252f%252e%252e%252fetc/passwd"));

        assertThat(response.getCode()).isEqualTo(ResponseCode.BAD_REQUEST);
        assertThat(response.getMessage()).isIn("Invalid file path", "Invalid file path encoding");
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @DisplayName("6. Blank path returns 400 without touching storage")
    void blankPath_isRejected() {
        ApiResponse<StorageDownloadResult> response = handler.handle(new DownloadFileQuery("   "));

        assertThat(response.getCode()).isEqualTo(ResponseCode.BAD_REQUEST);
        verifyNoInteractions(fileStorageService);
    }
}
