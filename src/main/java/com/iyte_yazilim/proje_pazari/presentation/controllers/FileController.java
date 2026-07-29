package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.commands.uploadFile.UploadFileCommand;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.queries.downloadFile.DownloadFileQuery;
import com.iyte_yazilim.proje_pazari.domain.models.StorageDownloadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
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
@Tag(
        name = "Files",
        description =
                "File management endpoints. Download is public, upload requires authentication.")
@Slf4j
public class FileController extends BaseController {

    private static final int DEFAULT_EXPIRY_MINUTES = 60;

    @GetMapping("/{*path}")
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Download file",
            description =
                    "Redirects to a presigned URL when the storage provider supports it (e.g. "
                            + "MinIO/S3), or streams the file content inline for providers that "
                            + "don't (e.g. local disk). Public access.")
    @ApiResponses(
            value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "302",
                        description = "Redirect to presigned URL"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "File content streamed inline"),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "File not found")
            })
    public ResponseEntity<?> downloadFile(
            @Parameter(
                            description = "File path relative to storage root",
                            required = true,
                            example = "profile-pictures/avatar.png")
                    @PathVariable
                    String path) {
        ApiResponse<StorageDownloadResult> response = mediator.send(new DownloadFileQuery(path));
        HttpStatus status = resolveHttpStatus(response.getCode());

        if (!status.is2xxSuccessful()) {
            return ResponseEntity.status(status).body(response);
        }

        // Exhaustive over the sealed StorageDownloadResult: a new variant becomes a compile
        // error here rather than silently falling through to a 200 with a JSON body.
        return switch (response.getData()) {
            case StorageDownloadResult.RedirectResult redirect ->
                    ResponseEntity.status(HttpStatus.FOUND)
                            .header(HttpHeaders.LOCATION, redirect.url())
                            .build();
            case StorageDownloadResult.InlineResult inline -> toInlineResponse(inline);
        };
    }

    private ResponseEntity<byte[]> toInlineResponse(StorageDownloadResult.InlineResult inline) {
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(inline.contentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(inline.content().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + sanitizeForHeader(inline.filename()) + "\"")
                .body(inline.content());
    }

    private String sanitizeForHeader(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }
        return filename.replaceAll("[\\r\\n\"\\\\]", "_");
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Upload file",
            description = "Uploads a new file. Requires authentication.",
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            required = true,
                            content =
                                    @Content(
                                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                                            schema =
                                                    @Schema(
                                                            type = "object",
                                                            requiredProperties = {"file"}),
                                            schemaProperties = {
                                                @SchemaProperty(
                                                        name = "file",
                                                        schema =
                                                                @Schema(
                                                                        type = "string",
                                                                        format = "binary",
                                                                        description =
                                                                                "File to upload"))
                                            },
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
                                "code": 0,
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
            @RequestParam("file") MultipartFile file) {
        return send(UploadFileCommand.class, null, null, null, null, Map.of("file", file));
    }
}
