package com.iyte_yazilim.proje_pazari.presentation.controllers;

import com.iyte_yazilim.proje_pazari.application.queries.downloadFile.DownloadFileQuery;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "Files", description = "File serving endpoints")
public class FileController extends BaseController {

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
        ApiResponse<String> response = mediator.send(new DownloadFileQuery(path));
        HttpStatus status = resolveHttpStatus(response.getCode());

        if (status.is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, response.getData())
                    .build();
        }

        return ResponseEntity.status(status).body(response);
    }
}
