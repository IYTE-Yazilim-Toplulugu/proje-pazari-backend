package com.iyte_yazilim.proje_pazari.application.queries.downloadFile;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.models.StorageDownloadResult;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DownloadFileHandler
        implements IRequestHandler<DownloadFileQuery, ApiResponse<StorageDownloadResult>> {

    private static final int DEFAULT_EXPIRY_MINUTES = 60;
    private static final int MAX_DECODE_ITERATIONS = 5;

    private final FileStorageService fileStorageService;

    @Override
    public ApiResponse<StorageDownloadResult> handle(DownloadFileQuery query) {
        String rawPath = query.path();

        if (rawPath == null || rawPath.isBlank()) {
            return ApiResponse.badRequest("Invalid file path");
        }

        // Defense in depth, not a live path in practice: verified locally that raw ".."
        // sequences never reach this handler via the real HTTP route, since Spring Security's
        // default StrictHttpFirewall rejects them before dispatch. This check stays in place
        // only in case DownloadFileHandler is ever invoked from a non-HTTP entry point (e.g. a
        // future internal/batch caller, a different transport, or a test harness) that bypasses
        // the servlet filter chain entirely.
        if (rawPath.contains("..")) {
            return ApiResponse.badRequest("Invalid file path");
        }

        String decodedPath;
        try {
            decodedPath = fullyDecode(rawPath);
        } catch (IllegalArgumentException e) {
            return ApiResponse.badRequest("Invalid file path encoding");
        }

        String normalizedPath = stripSingleLeadingSlash(decodedPath);

        if (!isValidRelativePath(normalizedPath)) {
            return ApiResponse.badRequest("Invalid file path");
        }

        if (!fileStorageService.fileExists(normalizedPath)) {
            return ApiResponse.notFound("File not found");
        }

        // Left to propagate deliberately: DomainException carries its own ErrorCode, which
        // GlobalExceptionHandler turns into the right status and a localized, user-safe message.
        // Catching it here to rewrap e.getMessage() would leak the technical message — including
        // the storage path — into the response body, and would flatten a file that vanished
        // between the check above and this read (404) into a 400.
        StorageDownloadResult result =
                fileStorageService.getDownloadResult(normalizedPath, DEFAULT_EXPIRY_MINUTES);
        return ApiResponse.success(result, "File resolved successfully");
    }

    /**
     * Repeatedly URL-decodes the path until it stops changing (or a small iteration cap is hit), so
     * multi-layer encoding tricks such as "%252e%252e%252f" (which single-decode only turns into
     * the literal text "%2e%2e%2f") are fully unwrapped to their real, meaningful form ("..")
     * before validation runs. The iteration cap guards against pathological/malicious inputs
     * designed to force excessive decode loops.
     */
    private String fullyDecode(String path) {
        String current = path;
        for (int i = 0; i < MAX_DECODE_ITERATIONS; i++) {
            String decodedOnce = URLDecoder.decode(current, StandardCharsets.UTF_8);
            if (Objects.equals(decodedOnce, current)) {
                return current;
            }
            current = decodedOnce;
            if (current.contains("..")) {
                // Fail fast as soon as traversal surfaces at any decode depth.
                throw new IllegalArgumentException("Invalid file path");
            }
        }
        return current;
    }

    private String stripSingleLeadingSlash(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    private boolean isValidRelativePath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        if (path.contains("..")) {
            return false;
        }
        if (path.startsWith("/") || path.startsWith("\\")) {
            return false;
        }
        if (path.contains("//") || path.contains("\\\\")) {
            return false;
        }
        return true;
    }
}
