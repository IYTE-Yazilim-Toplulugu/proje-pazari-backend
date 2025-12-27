package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.infrastructure.services.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File serving endpoints")
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/{fileName:.+}")
    @Operation(
            summary = "Download file",
            description = "Serves uploaded files (e.g., profile pictures). " +
                    "Currently public for profile pictures. " +
                    "TODO: Add authentication/authorization if used for private files."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "File retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "File not found"
            )
    })
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);

            String contentType = "application/octet-stream";

            // Try to determine file's content type
            try {
                int lastDotIndex = fileName.lastIndexOf(".");
                if (lastDotIndex >= 0 && lastDotIndex < fileName.length() - 1) {
                    String fileExtension = fileName.substring(lastDotIndex + 1).toLowerCase();
                    contentType = switch (fileExtension) {
                        case "jpg", "jpeg" -> "image/jpeg";
                        case "png" -> "image/png";
                        case "gif" -> "image/gif";
                        case "webp" -> "image/webp";
                        default -> "application/octet-stream";
                    };
                }
            } catch (Exception e) {
                // Log the exception for debugging purposes
                log.debug("Failed to determine content type for file: {}", fileName, e);
                // Use default content type
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
