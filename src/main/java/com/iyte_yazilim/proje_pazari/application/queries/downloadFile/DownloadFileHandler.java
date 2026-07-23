package com.iyte_yazilim.proje_pazari.application.queries.downloadFile;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DownloadFileHandler
        implements IRequestHandler<DownloadFileQuery, ApiResponse<String>> {

    private static final int DEFAULT_EXPIRY_MINUTES = 60;

    private final FileStorageService fileStorageService;

    @Override
    public ApiResponse<String> handle(DownloadFileQuery query) {
        String rawPath = query.path();

        if (rawPath == null || rawPath.isBlank()) {
            return ApiResponse.badRequest("Invalid file path");
        }

        // Defense in depth: reject traversal patterns before decoding too,
        // since decoding can only ever reveal MORE ".." sequences, never hide them.
        if (rawPath.contains("..")) {
            return ApiResponse.badRequest("Invalid file path");
        }

        String decodedPath;
        try {
            decodedPath = URLDecoder.decode(rawPath, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return ApiResponse.badRequest("Invalid file path encoding");
        }

        // Spring's `/{*path}` binding always prefixes the captured value with a
        // single leading slash (e.g. "/bucket/file.png"). That slash is an artifact
        // of the routing mechanism, not part of the logical storage key, so we strip
        // EXACTLY one leading slash here, at the HTTP boundary, before the path is
        // handed to FileStorageService. FileStorageService.validatePath() is left
        // untouched and continues to correctly reject any path starting with "/".
        String normalizedPath = stripSingleLeadingSlash(decodedPath);

        if (!isValidRelativePath(normalizedPath)) {
            return ApiResponse.badRequest("Invalid file path");
        }

        if (!fileStorageService.fileExists(normalizedPath)) {
            return ApiResponse.notFound("File not found");
        }

        String presignedUrl = fileStorageService.getFileUrl(normalizedPath, DEFAULT_EXPIRY_MINUTES);
        return ApiResponse.success(presignedUrl, "File URL generated successfully");
    }

    private String stripSingleLeadingSlash(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }

    /**
     * Re-validates the path after normalization.
     *
     * <p>This duplicates the critical parts of FileStorageService.validatePath() and adds extra
     * HTTP-boundary checks (e.g. rejecting double slashes) so we can fail fast with a clear 400.
     */
    private boolean isValidRelativePath(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        if (path.contains("..")) {
            return false;
        }
        // A remaining leading slash means the original path had a double leading
        // slash ("//...") — only one slash is ever the routing artifact.
        if (path.startsWith("/") || path.startsWith("\\")) {
            return false;
        }
        if (path.contains("//") || path.contains("\\\\")) {
            return false;
        }
        return true;
    }
}
