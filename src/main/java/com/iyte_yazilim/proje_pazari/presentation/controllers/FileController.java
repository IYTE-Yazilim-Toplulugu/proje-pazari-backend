package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File serving endpoints")
@Slf4j
public class FileController {

    private static final int DEFAULT_EXPIRY_MINUTES = 60;

    private final FileStorageService fileStorageService;

    @GetMapping("/{*path}")
    @Operation(
            summary = "Download file",
            description =
                    "Redirects to presigned URL for file access. "
                            + "Supports images, PDFs, and documents.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "302",
                        description = "Redirect to presigned URL"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "File not found")
            })
    public ResponseEntity<?> downloadFile(@PathVariable String path) {
        try {
            // Validate path
            if (path == null || path.isBlank() || path.contains("..")) {
                return ResponseEntity.badRequest().body("Invalid file path");
            }

            // Check if file exists
            if (!fileStorageService.fileExists(path)) {
                return ResponseEntity.notFound().build();
            }

            // Generate presigned URL and redirect
            String presignedUrl = fileStorageService.getFileUrl(path, DEFAULT_EXPIRY_MINUTES);

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, presignedUrl)
                    .build();

        } catch (FileStorageException e) {
            log.debug("File not found: {}", path);
            return ResponseEntity.notFound().build();
        }
    }
}
