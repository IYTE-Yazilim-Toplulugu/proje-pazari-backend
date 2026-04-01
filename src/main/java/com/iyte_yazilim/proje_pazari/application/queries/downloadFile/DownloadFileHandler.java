package com.iyte_yazilim.proje_pazari.application.queries.downloadFile;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
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
        String path = query.path();

        if (path == null || path.isBlank()) {
            return ApiResponse.badRequest("Invalid file path");
        }

        String decodedPath;
        try {
            decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return ApiResponse.badRequest("Invalid file path encoding");
        }

        if (decodedPath.contains("..") || path.contains("..")) {
            return ApiResponse.badRequest("Invalid file path");
        }

        if (!fileStorageService.fileExists(decodedPath)) {
            return ApiResponse.notFound("File not found");
        }

        String presignedUrl = fileStorageService.getFileUrl(decodedPath, DEFAULT_EXPIRY_MINUTES);
        return ApiResponse.success(presignedUrl, "File URL generated successfully");
    }
}
