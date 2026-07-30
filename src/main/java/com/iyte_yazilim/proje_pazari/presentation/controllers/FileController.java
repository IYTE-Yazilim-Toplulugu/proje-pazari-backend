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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
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
    private static final String FALLBACK_FILE_NAME = "download";

    /**
     * Content types this endpoint will render in the browser. Anything else is served as an opaque
     * attachment, because a type the browser executes in a document context (text/html,
     * image/svg+xml, ...) must never be rendered from this origin: downloads are public, and the
     * bytes originate from user uploads.
     *
     * <p>Listed explicitly rather than read from {@code storage.allowed-content-types} so that
     * widening what may be uploaded cannot silently widen what a browser will execute.
     */
    private static final Set<MediaType> INLINE_SAFE_TYPES =
            Set.of(
                    MediaType.IMAGE_JPEG,
                    MediaType.IMAGE_PNG,
                    MediaType.IMAGE_GIF,
                    new MediaType("image", "webp"),
                    MediaType.APPLICATION_PDF);

    private static final String CONTENT_TYPE_OPTIONS_HEADER = "X-Content-Type-Options";
    private static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";

    /**
     * Applied to served file content so that a document rendered from these bytes can load nothing
     * and submit nowhere. The CSP {@code sandbox} directive is deliberately omitted: it would also
     * disable the browsers' built-in PDF viewer, and PDFs are served inline.
     */
    private static final String FILE_CONTENT_SECURITY_POLICY =
            "default-src 'none'; base-uri 'none'; form-action 'none'";

    @GetMapping("/{*path}")
    @PreAuthorize("permitAll()")
    @Operation(
            summary = "Download file",
            description =
                    "Redirects to a presigned URL when the storage provider supports it (e.g. "
                            + "MinIO/S3), or streams the file content for providers that don't "
                            + "(e.g. local disk) — rendered inline for image and PDF types, and "
                            + "served as an attachment otherwise. Public access.")
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
        MediaType mediaType = resolveServableType(inline.contentType());
        boolean renderInline = INLINE_SAFE_TYPES.contains(mediaType);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(inline.content().length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition(inline.filename(), renderInline))
                // Without nosniff, a mislabeled payload can still be sniffed into an executable
                // type, which would defeat the downgrade above.
                .header(CONTENT_TYPE_OPTIONS_HEADER, "nosniff")
                .header(CONTENT_SECURITY_POLICY_HEADER, FILE_CONTENT_SECURITY_POLICY)
                .body(inline.content());
    }

    /**
     * Parses the stored content type and downgrades anything outside {@link #INLINE_SAFE_TYPES} to
     * application/octet-stream, so a stored value that is unexpected, stale, or hostile cannot
     * become the response type. Parameters are dropped because they are not part of the type's
     * identity and would defeat the comparison.
     */
    private MediaType resolveServableType(String storedContentType) {
        try {
            MediaType parsed = MediaType.parseMediaType(storedContentType);
            MediaType bareType = new MediaType(parsed.getType(), parsed.getSubtype());
            return INLINE_SAFE_TYPES.contains(bareType)
                    ? bareType
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * Builds the Content-Disposition header, adding the RFC 5987 {@code filename*} form only when
     * the name actually needs it. Non-ASCII names (e.g. "özgeçmiş.pdf") would otherwise be mangled
     * by the ISO-8859-1 encoding the servlet layer applies to raw header strings. The charset is
     * passed conditionally because Spring also MIME-encodes the plain {@code filename} parameter
     * whenever a charset is present, which needlessly obscures ordinary ASCII names.
     */
    private String contentDisposition(String rawFileName, boolean renderInline) {
        String safeName = sanitizeForHeader(rawFileName);
        boolean asciiOnly = StandardCharsets.US_ASCII.newEncoder().canEncode(safeName);

        ContentDisposition.Builder builder =
                renderInline ? ContentDisposition.inline() : ContentDisposition.attachment();

        ContentDisposition disposition =
                asciiOnly
                        ? builder.filename(safeName).build()
                        : builder.filename(safeName, StandardCharsets.UTF_8).build();

        return disposition.toString();
    }

    /**
     * Drops path separators and control characters before the name reaches the header builder.
     * {@link ContentDisposition} handles quoting and encoding, but not header injection or a name
     * that smuggles in a directory component.
     */
    private String sanitizeForHeader(String filename) {
        if (filename == null || filename.isBlank()) {
            return FALLBACK_FILE_NAME;
        }
        String cleaned = filename.replaceAll("[\\p{Cntrl}/\\\\]", "").trim();
        return cleaned.isEmpty() ? FALLBACK_FILE_NAME : cleaned;
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
