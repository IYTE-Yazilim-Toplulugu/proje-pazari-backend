package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File management endpoints. Download is public, upload requires authentication.")
@Slf4j
public class FileController {

    private static final int DEFAULT_EXPIRY_MINUTES = 60;

    private final FileStorageService fileStorageService;

    @GetMapping("/{*path}")
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Download file",
            description =
                    "Redirects to presigned URL for file access. "
                            + "Supports images, PDFs, and documents. Public access.")
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
            // Validate path - check for path traversal attacks including encoded variants
            if (path == null || path.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid file path");
            }

            // Decode URL-encoded characters and normalize path for security validation
            String decodedPath;
            try {
                decodedPath =
                        java.net.URLDecoder.decode(path, java.nio.charset.StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid file path encoding");
            }

            // Check for path traversal patterns (both encoded and decoded)
            if (decodedPath.contains("..") || path.contains("..")) {
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

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Upload file",
            description = "Uploads a new file. Requires authentication.",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            content =
                                    @Content(
                                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                            schema = @Schema(type = "object"),
                                            encoding =
                                                    @io.swagger.v3.oas.annotations.media.Encoding(
                                                            name = "file",
                                                            contentType = "*/*"))))
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "File uploaded successfully",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = ApiResponse.class),
                                        examples =
                                                @io.swagger.v3.oas.annotations.media.ExampleObject(
                                                        name = "Success Response",
                                                        value =
                                                                """
                                        {
                                            "code": "SUCCESS",
                                            "message": "File uploaded successfully",
                                            "data": {
                                                "filename": "document.pdf",
                                                "url": "/api/v1/files/document.pdf",
                                                "size": 1024567
                                            }
                                        }
                                        """))),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Invalid file or validation error"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - authentication required"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "500",
                        description = "Internal server error")
            })
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(
            @Parameter(description = "File to upload", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File is empty"));
            }

            // Validate file - check for null/empty filename
            String filename = file.getOriginalFilename();
            if (filename == null || filename.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid filename"));
            }

            // Check for path traversal attacks
            if (filename.contains("..")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid filename"));
            }

            // Store file and get the path
            String filePath = fileStorageService.storeFile(file);

            // Get file size
            long fileSize = file.getSize();

            // Build response
            Map<String, Object> fileData = Map.of(
                    "filename", filename,
                    "url", "/api/v1/files/" + filePath,
                    "size", fileSize
            );

            return ResponseEntity.ok(ApiResponse.success(fileData, "File uploaded successfully"));

        } catch (FileStorageException e) {
            log.error("File upload failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("File upload failed: " + e.getMessage()));
        }
    }
}
